package com.smartwater.service;

import com.smartwater.entity.BillingCycle;
import com.smartwater.entity.Household;
import com.smartwater.entity.SharedCostAllocation;
import com.smartwater.repository.HouseholdRepository;
import com.smartwater.repository.SharedCostAllocationRepository;
import com.smartwater.repository.WaterUsageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SharedCostAllocationService {

    private final SharedCostAllocationRepository allocationRepository;
    private final HouseholdRepository householdRepository;
    private final WaterUsageLogRepository waterUsageLogRepository;

    @Transactional
    public List<SharedCostAllocation> allocateSharedWaterCosts(BillingCycle cycle, BigDecimal totalProcuredCost) {
        allocationRepository.deleteByBillingCycleId(cycle.getId());

        if (totalProcuredCost == null || totalProcuredCost.compareTo(BigDecimal.ZERO) <= 0) {
            return Collections.emptyList();
        }

        List<Household> households = householdRepository.findByApartmentId(cycle.getApartmentId());
        if (households.isEmpty()) {
            return Collections.emptyList();
        }

        // Map consumption per household during billing period
        Map<Long, BigDecimal> hhConsumptionMap = new HashMap<>();
        BigDecimal totalMeteredConsumption = BigDecimal.ZERO;

        for (Household hh : households) {
            BigDecimal consumption = waterUsageLogRepository.calculateConsumptionByHouseholdAndDateRange(
                    hh.getId(), cycle.getPeriodStart(), cycle.getPeriodEnd()
            );
            if (consumption == null) {
                consumption = BigDecimal.ZERO;
            }
            hhConsumptionMap.put(hh.getId(), consumption);
            totalMeteredConsumption = totalMeteredConsumption.add(consumption);
        }

        List<SharedCostAllocation> allocations = new ArrayList<>();
        BigDecimal allocatedSum = BigDecimal.ZERO;

        boolean isMeteredConsumptionAvailable = totalMeteredConsumption.compareTo(BigDecimal.ZERO) > 0;

        if (isMeteredConsumptionAvailable) {
            // Proportional allocation based on consumption
            for (Household hh : households) {
                BigDecimal consumption = hhConsumptionMap.get(hh.getId());
                BigDecimal percentage = consumption.divide(totalMeteredConsumption, 6, RoundingMode.HALF_UP);
                BigDecimal amount = totalProcuredCost.multiply(percentage).setScale(2, RoundingMode.HALF_UP);

                SharedCostAllocation alloc = SharedCostAllocation.builder()
                        .billingCycleId(cycle.getId())
                        .householdId(hh.getId())
                        .allocationBasis("CONSUMPTION_PROPORTIONAL")
                        .allocationPercentage(percentage.multiply(BigDecimal.valueOf(100)).setScale(4, RoundingMode.HALF_UP))
                        .allocatedAmount(amount)
                        .calculationInputsJson(String.format("{\"consumptionKl\": %s, \"totalMeteredKl\": %s}", consumption, totalMeteredConsumption))
                        .build();

                allocations.add(alloc);
                allocatedSum = allocatedSum.add(amount);
            }
        } else {
            // Fallback allocation based on household square footage
            BigDecimal totalArea = households.stream()
                    .map(h -> h.getAreaSqft() != null ? h.getAreaSqft() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalArea.compareTo(BigDecimal.ZERO) <= 0) {
                totalArea = BigDecimal.valueOf(households.size()); // equal division fallback
            }

            for (Household hh : households) {
                BigDecimal area = hh.getAreaSqft() != null ? hh.getAreaSqft() : BigDecimal.ONE;
                BigDecimal percentage = area.divide(totalArea, 6, RoundingMode.HALF_UP);
                BigDecimal amount = totalProcuredCost.multiply(percentage).setScale(2, RoundingMode.HALF_UP);

                SharedCostAllocation alloc = SharedCostAllocation.builder()
                        .billingCycleId(cycle.getId())
                        .householdId(hh.getId())
                        .allocationBasis("AREA_FALLBACK")
                        .allocationPercentage(percentage.multiply(BigDecimal.valueOf(100)).setScale(4, RoundingMode.HALF_UP))
                        .allocatedAmount(amount)
                        .calculationInputsJson(String.format("{\"areaSqft\": %s, \"totalAreaSqft\": %s}", area, totalArea))
                        .build();

                allocations.add(alloc);
                allocatedSum = allocatedSum.add(amount);
            }
        }

        // Deterministically reconcile rounding differences (assign remainder to first household)
        BigDecimal remainder = totalProcuredCost.subtract(allocatedSum);
        if (!allocations.isEmpty() && remainder.compareTo(BigDecimal.ZERO) != 0) {
            SharedCostAllocation first = allocations.get(0);
            first.setAllocatedAmount(first.getAllocatedAmount().add(remainder));
        }

        return allocationRepository.saveAll(allocations);
    }
}
