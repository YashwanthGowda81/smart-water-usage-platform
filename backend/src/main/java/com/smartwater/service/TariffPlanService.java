package com.smartwater.service;

import com.smartwater.dto.CreateTariffPlanRequest;
import com.smartwater.dto.TariffPlanDto;
import com.smartwater.dto.TariffTierDto;
import com.smartwater.entity.TariffPlan;
import com.smartwater.entity.TariffTier;
import com.smartwater.exception.ResourceNotFoundException;
import com.smartwater.repository.TariffPlanRepository;
import com.smartwater.repository.TariffTierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TariffPlanService {

    private final TariffPlanRepository tariffPlanRepository;
    private final TariffTierRepository tariffTierRepository;

    @Transactional
    public TariffPlanDto createTariffPlan(CreateTariffPlanRequest request) {
        TariffPlan plan = TariffPlan.builder()
                .apartmentId(request.getApartmentId())
                .name(request.getName())
                .effectiveStartDate(request.getEffectiveStartDate())
                .effectiveEndDate(request.getEffectiveEndDate())
                .baseRate(request.getBaseRate() != null ? request.getBaseRate() : BigDecimal.ZERO)
                .active(request.getActive() != null ? request.getActive() : true)
                .tiers(new ArrayList<>())
                .build();

        TariffPlan savedPlan = tariffPlanRepository.save(plan);

        List<TariffTier> tiers = new ArrayList<>();
        if (request.getTiers() != null) {
            int autoTierNum = 1;
            for (TariffTierDto tierDto : request.getTiers()) {
                TariffTier tier = TariffTier.builder()
                        .tariffPlan(savedPlan)
                        .tierNumber(tierDto.getTierNumber() != null ? tierDto.getTierNumber() : autoTierNum++)
                        .minKl(tierDto.getMinKl() != null ? tierDto.getMinKl() : BigDecimal.ZERO)
                        .maxKl(tierDto.getMaxKl())
                        .ratePerKl(tierDto.getRatePerKl() != null ? tierDto.getRatePerKl() : BigDecimal.ZERO)
                        .build();
                tiers.add(tier);
            }
            tariffTierRepository.saveAll(tiers);
            savedPlan.setTiers(tiers);
        }

        return mapToDto(savedPlan);
    }

    public List<TariffPlanDto> getTariffPlansByApartment(Long apartmentId) {
        return tariffPlanRepository.findByApartmentId(apartmentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Progressive Tier Calculation Algorithm
     * Example: Tier 1 (0-10 kL @ 20), Tier 2 (10-20 kL @ 30). Usage 18 kL -> 10*20 + 8*30 = 440.
     */
    public BigDecimal calculateProgressiveTariffCharge(Long apartmentId, LocalDate date, BigDecimal consumptionKl) {
        if (consumptionKl == null || consumptionKl.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        Optional<TariffPlan> planOpt = tariffPlanRepository
                .findFirstByApartmentIdAndActiveTrueAndEffectiveStartDateLessThanEqualOrderByEffectiveStartDateDesc(apartmentId, date);

        if (planOpt.isEmpty()) {
            // Fallback default rate if no plan configured
            return consumptionKl.multiply(BigDecimal.valueOf(25.00)).setScale(2, RoundingMode.HALF_UP);
        }

        TariffPlan plan = planOpt.get();
        BigDecimal totalCharge = plan.getBaseRate() != null ? plan.getBaseRate() : BigDecimal.ZERO;

        List<TariffTier> tiers = plan.getTiers();
        if (tiers == null || tiers.isEmpty()) {
            return totalCharge.setScale(2, RoundingMode.HALF_UP);
        }

        for (TariffTier tier : tiers) {
            BigDecimal minKl = tier.getMinKl();
            BigDecimal maxKl = tier.getMaxKl();
            BigDecimal rate = tier.getRatePerKl();

            if (consumptionKl.compareTo(minKl) > 0) {
                BigDecimal tierUpper = (maxKl != null && maxKl.compareTo(BigDecimal.ZERO) > 0) ? maxKl : consumptionKl;
                BigDecimal eligibleKlInTier = consumptionKl.min(tierUpper).subtract(minKl);
                if (eligibleKlInTier.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal tierCost = eligibleKlInTier.multiply(rate);
                    totalCharge = totalCharge.add(tierCost);
                }
            }
        }

        return totalCharge.setScale(2, RoundingMode.HALF_UP);
    }

    private TariffPlanDto mapToDto(TariffPlan plan) {
        List<TariffTierDto> tierDtos = plan.getTiers() != null
                ? plan.getTiers().stream().map(t -> TariffTierDto.builder()
                .id(t.getId())
                .tierNumber(t.getTierNumber())
                .minKl(t.getMinKl())
                .maxKl(t.getMaxKl())
                .ratePerKl(t.getRatePerKl())
                .build()).collect(Collectors.toList())
                : new ArrayList<>();

        return TariffPlanDto.builder()
                .id(plan.getId())
                .apartmentId(plan.getApartmentId())
                .name(plan.getName())
                .effectiveStartDate(plan.getEffectiveStartDate())
                .effectiveEndDate(plan.getEffectiveEndDate())
                .baseRate(plan.getBaseRate())
                .active(plan.getActive())
                .tiers(tierDtos)
                .build();
    }
}
