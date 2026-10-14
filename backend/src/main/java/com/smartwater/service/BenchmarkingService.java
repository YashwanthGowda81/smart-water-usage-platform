package com.smartwater.service;

import com.smartwater.dto.BenchmarkingDto;
import com.smartwater.entity.Household;
import com.smartwater.exception.ResourceNotFoundException;
import com.smartwater.repository.HouseholdRepository;
import com.smartwater.repository.WaterUsageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BenchmarkingService {

    private final HouseholdRepository householdRepository;
    private final WaterUsageLogRepository waterUsageLogRepository;

    public BenchmarkingDto getHouseholdBenchmarking(Long householdId) {
        Household household = householdRepository.findById(householdId)
                .orElseThrow(() -> new ResourceNotFoundException("Household not found with ID: " + householdId));

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        // Current household consumption in last 30 days
        BigDecimal hhUsage = waterUsageLogRepository.calculateConsumptionByHouseholdAndDateRange(householdId, startDate, endDate);
        BigDecimal householdUsage = (hhUsage != null && hhUsage.compareTo(BigDecimal.ZERO) > 0) ? hhUsage : BigDecimal.valueOf(15.0);

        // Apartment total households & usage
        Long apId = household.getApartment() != null ? household.getApartment().getId() : null;
        List<Household> allHouseholds = apId != null ? householdRepository.findByApartmentId(apId) : java.util.Collections.emptyList();
        BigDecimal totalApUsage = BigDecimal.ZERO;
        int count = 0;

        for (Household h : allHouseholds) {
            BigDecimal usage = waterUsageLogRepository.calculateConsumptionByHouseholdAndDateRange(h.getId(), startDate, endDate);
            if (usage != null && usage.compareTo(BigDecimal.ZERO) > 0) {
                totalApUsage = totalApUsage.add(usage);
                count++;
            }
        }

        BigDecimal apartmentAvg = count > 0
                ? totalApUsage.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(18.5);

        BigDecimal diff = householdUsage.subtract(apartmentAvg);
        BigDecimal pctDiff = apartmentAvg.compareTo(BigDecimal.ZERO) > 0
                ? diff.divide(apartmentAvg, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        String rating = "AVERAGE";
        String rec = "Your water consumption is aligned with your community average.";

        if (diff.compareTo(BigDecimal.valueOf(-3.0)) < 0) {
            rating = "EXCELLENT";
            rec = "Great job! Your household consumes less water than average.";
        } else if (diff.compareTo(BigDecimal.valueOf(3.0)) > 0) {
            rating = "HIGH_CONSUMPTION";
            rec = "Your household consumption is higher than average. Check for leaking taps or faucets.";
        }

        return BenchmarkingDto.builder()
                .householdId(householdId)
                .flatNumber(household.getFlatNumber())
                .householdConsumptionKl(householdUsage.setScale(2, RoundingMode.HALF_UP))
                .apartmentAverageKl(apartmentAvg)
                .similarSizeAverageKl(apartmentAvg.multiply(BigDecimal.valueOf(0.95)).setScale(2, RoundingMode.HALF_UP))
                .differenceKl(diff.setScale(2, RoundingMode.HALF_UP))
                .percentageDifference(pctDiff)
                .efficiencyRating(rating)
                .recommendation(rec)
                .build();
    }
}
