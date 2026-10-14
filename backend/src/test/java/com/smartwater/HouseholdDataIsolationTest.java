package com.smartwater;

import com.smartwater.controller.HouseholdBillController;
import com.smartwater.dto.HouseholdBillDto;
import com.smartwater.entity.Role;
import com.smartwater.entity.User;
import com.smartwater.exception.UnauthorizedException;
import com.smartwater.repository.UserRepository;
import com.smartwater.service.BillingCycleService;
import com.smartwater.service.PdfInvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HouseholdDataIsolationTest {

    @Mock
    private BillingCycleService billingCycleService;

    @Mock
    private PdfInvoiceService pdfInvoiceService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private HouseholdBillController householdBillController;

    private User residentUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        residentUser = User.builder()
                .id(1L)
                .email("resident@smartwater.local")
                .role(Role.RESIDENT)
                .householdId(101L)
                .build();

        adminUser = User.builder()
                .id(99L)
                .email("admin@smartwater.local")
                .role(Role.ADMIN)
                .build();
    }

    @Test
    void residentAccessingOwnHouseholdBills_Allowed() {
        when(authentication.getName()).thenReturn("resident@smartwater.local");
        when(userRepository.findByEmail("resident@smartwater.local")).thenReturn(Optional.of(residentUser));
        when(billingCycleService.getBillsByHousehold(101L)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> {
            var response = householdBillController.getBillsByHousehold(101L, authentication);
            assertEquals(200, response.getStatusCode().value());
        });
    }

    @Test
    void residentAccessingOtherHouseholdBills_ThrowsUnauthorizedException() {
        when(authentication.getName()).thenReturn("resident@smartwater.local");
        when(userRepository.findByEmail("resident@smartwater.local")).thenReturn(Optional.of(residentUser));

        assertThrows(UnauthorizedException.class, () -> {
            householdBillController.getBillsByHousehold(102L, authentication);
        });
    }

    @Test
    void adminAccessingAnyHouseholdBills_Allowed() {
        when(authentication.getName()).thenReturn("admin@smartwater.local");
        when(userRepository.findByEmail("admin@smartwater.local")).thenReturn(Optional.of(adminUser));
        when(billingCycleService.getBillsByHousehold(102L)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> {
            var response = householdBillController.getBillsByHousehold(102L, authentication);
            assertEquals(200, response.getStatusCode().value());
        });
    }

    @Test
    void residentAccessingOtherHouseholdBillById_ThrowsUnauthorizedException() {
        HouseholdBillDto otherBill = HouseholdBillDto.builder()
                .id(50L)
                .householdId(102L)
                .totalPayable(BigDecimal.valueOf(120.50))
                .build();

        when(authentication.getName()).thenReturn("resident@smartwater.local");
        when(userRepository.findByEmail("resident@smartwater.local")).thenReturn(Optional.of(residentUser));
        when(billingCycleService.getBillById(50L)).thenReturn(otherBill);

        assertThrows(UnauthorizedException.class, () -> {
            householdBillController.getBillById(50L, authentication);
        });
    }
}
