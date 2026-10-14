package com.smartwater;

import com.smartwater.entity.*;
import com.smartwater.repository.*;
import com.smartwater.service.SharedCostAllocationService;
import com.smartwater.service.TariffPlanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EndToEndWorkflowTest {

    @Mock
    private SharedCostAllocationRepository allocationRepository;

    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private WaterUsageLogRepository waterUsageLogRepository;

    @Mock
    private TariffPlanRepository tariffPlanRepository;

    @InjectMocks
    private TariffPlanService tariffPlanService;

    @InjectMocks
    private SharedCostAllocationService sharedCostAllocationService;

    @Test
    void testEndToEndCalculationFlow() {
        // Step 1: Simulate Water Readings & Consumption Calculation
        BigDecimal previousReading = new BigDecimal("120.000");
        BigDecimal currentReading = new BigDecimal("135.500");
        assertTrue(currentReading.compareTo(previousReading) >= 0, "Continuity check: current reading must be >= previous");
        BigDecimal meteredConsumptionKl = currentReading.subtract(previousReading); // 15.500 kL
        assertEquals(new BigDecimal("15.500"), meteredConsumptionKl);

        // Step 2: Progressive Tiered Tariff Calculation
        // Tier 1: 0 - 10 kL @ $20.00/kL
        // Tier 2: > 10 kL @ $30.00/kL
        TariffTier tier1 = TariffTier.builder().tierNumber(1).minKl(BigDecimal.ZERO).maxKl(new BigDecimal("10.000")).ratePerKl(new BigDecimal("20.00")).build();
        TariffTier tier2 = TariffTier.builder().tierNumber(2).minKl(new BigDecimal("10.000")).maxKl(null).ratePerKl(new BigDecimal("30.00")).build();

        TariffPlan plan = TariffPlan.builder()
                .id(1L)
                .apartmentId(1L)
                .name("Standard Progressive")
                .effectiveStartDate(LocalDate.of(2026, 1, 1))
                .active(true)
                .tiers(List.of(tier1, tier2))
                .build();

        when(tariffPlanRepository.findFirstByApartmentIdAndActiveTrueAndEffectiveStartDateLessThanEqualOrderByEffectiveStartDateDesc(1L, LocalDate.of(2026, 3, 31)))
                .thenReturn(Optional.of(plan));

        // 10 kL @ 20.00 + 5.5 kL @ 30.00 = 200.00 + 165.00 = 365.00
        BigDecimal meteredTariff = tariffPlanService.calculateProgressiveTariffCharge(1L, LocalDate.of(2026, 3, 31), meteredConsumptionKl);
        assertEquals(new BigDecimal("365.00"), meteredTariff);

        // Step 3: Shared Bulk Water Cost Allocation
        // Household 1: 15.5 kL
        // Household 2: 24.5 kL
        // Total metered = 40.0 kL. Total Tanker purchase = $200.00
        BillingCycle cycle = BillingCycle.builder()
                .id(5L)
                .apartmentId(1L)
                .periodStart(LocalDate.of(2026, 3, 1))
                .periodEnd(LocalDate.of(2026, 3, 31))
                .build();

        Household h1 = Household.builder().id(101L).flatNumber("A-101").occupancy(3).areaSqft(new BigDecimal("1200")).metered(true).status("ACTIVE").build();
        Household h2 = Household.builder().id(102L).flatNumber("A-102").occupancy(4).areaSqft(new BigDecimal("1500")).metered(true).status("ACTIVE").build();

        when(householdRepository.findByApartmentId(1L)).thenReturn(Arrays.asList(h1, h2));
        when(waterUsageLogRepository.calculateConsumptionByHouseholdAndDateRange(101L, cycle.getPeriodStart(), cycle.getPeriodEnd()))
                .thenReturn(meteredConsumptionKl);
        when(waterUsageLogRepository.calculateConsumptionByHouseholdAndDateRange(102L, cycle.getPeriodStart(), cycle.getPeriodEnd()))
                .thenReturn(new BigDecimal("24.500"));
        when(allocationRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        BigDecimal sharedTotalCost = new BigDecimal("200.00");
        List<SharedCostAllocation> allocations = sharedCostAllocationService.allocateSharedWaterCosts(cycle, sharedTotalCost);

        assertEquals(2, allocations.size());
        SharedCostAllocation alloc1 = allocations.stream().filter(a -> a.getHouseholdId().equals(101L)).findFirst().orElseThrow();
        SharedCostAllocation alloc2 = allocations.stream().filter(a -> a.getHouseholdId().equals(102L)).findFirst().orElseThrow();

        // 15.5 / 40.0 * 200 = 77.50
        assertEquals(new BigDecimal("77.50"), alloc1.getAllocatedAmount());
        // 24.5 / 40.0 * 200 = 122.50
        assertEquals(new BigDecimal("122.50"), alloc2.getAllocatedAmount());

        // Step 4: Reconcile sum of allocations
        BigDecimal sumAllocated = alloc1.getAllocatedAmount().add(alloc2.getAllocatedAmount());
        assertEquals(sharedTotalCost, sumAllocated);

        // Step 5: Bill Generation & Itemization
        BigDecimal fixedCharge = new BigDecimal("15.00");
        BigDecimal totalPayable = meteredTariff.add(fixedCharge).add(alloc1.getAllocatedAmount());
        // 365.00 + 15.00 + 77.50 = 457.50
        assertEquals(new BigDecimal("457.50"), totalPayable);
    }
}
