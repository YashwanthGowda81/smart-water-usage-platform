package com.smartwater;

import com.smartwater.dto.BillingCycleDto;
import com.smartwater.entity.BillingCycle;
import com.smartwater.entity.BillingCycleStatus;
import com.smartwater.exception.ResourceNotFoundException;
import com.smartwater.repository.*;
import com.smartwater.service.BillingCycleService;
import com.smartwater.service.BulkWaterPurchaseService;
import com.smartwater.service.SharedCostAllocationService;
import com.smartwater.service.TariffPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingCycleServiceTest {

    @Mock
    private BillingCycleRepository billingCycleRepository;

    @Mock
    private ApartmentRepository apartmentRepository;

    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private WaterUsageLogRepository waterUsageLogRepository;

    @Mock
    private BulkWaterPurchaseRepository purchaseRepository;

    @Mock
    private SharedCostAllocationService sharedCostAllocationService;

    @Mock
    private TariffPlanService tariffPlanService;

    @Mock
    private HouseholdBillRepository householdBillRepository;

    @Mock
    private BillItemRepository billItemRepository;

    @InjectMocks
    private BillingCycleService billingCycleService;

    private BillingCycle openCycle;

    @BeforeEach
    void setUp() {
        openCycle = BillingCycle.builder()
                .id(1L)
                .apartmentId(10L)
                .periodStart(LocalDate.of(2026, 3, 1))
                .periodEnd(LocalDate.of(2026, 3, 31))
                .status(BillingCycleStatus.OPEN)
                .build();
    }

    @Test
    void finalizeBillingCycle_ChangesStatusToFinalized() {
        when(billingCycleRepository.findById(1L)).thenReturn(Optional.of(openCycle));
        when(purchaseRepository.findByApartmentIdAndPurchaseDateBetween(any(), any(), any())).thenReturn(java.util.Collections.emptyList());
        when(sharedCostAllocationService.allocateSharedWaterCosts(any(), any())).thenReturn(java.util.Collections.emptyList());
        when(householdRepository.findByApartmentId(any())).thenReturn(java.util.Collections.emptyList());
        when(billingCycleRepository.save(any(BillingCycle.class))).thenAnswer(i -> i.getArgument(0));

        BillingCycleDto result = billingCycleService.finalizeBillingCycle(1L);

        assertNotNull(result);
        assertEquals(BillingCycleStatus.FINALIZED, result.getStatus());
        verify(billingCycleRepository, times(2)).save(any(BillingCycle.class));
    }

    @Test
    void getBillingCycle_NotFound_ThrowsResourceNotFoundException() {
        when(billingCycleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> billingCycleService.getBillingCycle(99L));
    }
}
