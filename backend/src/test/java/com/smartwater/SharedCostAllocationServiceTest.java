package com.smartwater;

import com.smartwater.entity.BillingCycle;
import com.smartwater.entity.Household;
import com.smartwater.entity.SharedCostAllocation;
import com.smartwater.repository.HouseholdRepository;
import com.smartwater.repository.SharedCostAllocationRepository;
import com.smartwater.repository.WaterUsageLogRepository;
import com.smartwater.service.SharedCostAllocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SharedCostAllocationServiceTest {

    @Mock
    private SharedCostAllocationRepository sharedCostAllocationRepository;

    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private WaterUsageLogRepository waterUsageLogRepository;

    @InjectMocks
    private SharedCostAllocationService sharedCostAllocationService;

    private BillingCycle billingCycle;
    private Household hh1;
    private Household hh2;

    @BeforeEach
    void setUp() {
        billingCycle = BillingCycle.builder()
                .id(1L)
                .apartmentId(10L)
                .periodStart(LocalDate.of(2026, 3, 1))
                .periodEnd(LocalDate.of(2026, 3, 31))
                .build();

        hh1 = Household.builder().id(101L).flatNumber("A-101").areaSqft(new BigDecimal("1000")).build();
        hh2 = Household.builder().id(102L).flatNumber("A-102").areaSqft(new BigDecimal("1000")).build();
    }

    @Test
    void allocateSharedWaterCosts_ConsumptionProportional_SplitsEqually() {
        when(householdRepository.findByApartmentId(10L)).thenReturn(List.of(hh1, hh2));
        when(waterUsageLogRepository.calculateConsumptionByHouseholdAndDateRange(101L, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
                .thenReturn(new BigDecimal("50.00"));
        when(waterUsageLogRepository.calculateConsumptionByHouseholdAndDateRange(102L, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
                .thenReturn(new BigDecimal("50.00"));

        when(sharedCostAllocationRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<SharedCostAllocation> allocations = sharedCostAllocationService.allocateSharedWaterCosts(billingCycle, new BigDecimal("1000.00"));

        assertNotNull(allocations);
        assertEquals(2, allocations.size());
        assertEquals(new BigDecimal("500.00"), allocations.get(0).getAllocatedAmount());
        assertEquals("CONSUMPTION_PROPORTIONAL", allocations.get(0).getAllocationBasis());
    }

    @Test
    void allocateSharedWaterCosts_NoMeteredConsumption_UsesAreaFallback() {
        when(householdRepository.findByApartmentId(10L)).thenReturn(List.of(hh1, hh2));
        when(waterUsageLogRepository.calculateConsumptionByHouseholdAndDateRange(anyLong(), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        when(sharedCostAllocationRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<SharedCostAllocation> allocations = sharedCostAllocationService.allocateSharedWaterCosts(billingCycle, new BigDecimal("1000.00"));

        assertNotNull(allocations);
        assertEquals(2, allocations.size());
        assertEquals(new BigDecimal("500.00"), allocations.get(0).getAllocatedAmount());
        assertEquals("AREA_FALLBACK", allocations.get(0).getAllocationBasis());
    }
}
