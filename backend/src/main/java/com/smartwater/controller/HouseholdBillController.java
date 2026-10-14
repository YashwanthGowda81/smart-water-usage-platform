package com.smartwater.controller;

import com.smartwater.dto.HouseholdBillDto;
import com.smartwater.entity.Role;
import com.smartwater.entity.User;
import com.smartwater.exception.UnauthorizedException;
import com.smartwater.repository.UserRepository;
import com.smartwater.service.BillingCycleService;
import com.smartwater.service.PdfInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@Tag(name = "Household Bills", description = "Endpoints for Household Water Bills and PDF Invoices")
public class HouseholdBillController {

    private final BillingCycleService billingCycleService;
    private final PdfInvoiceService pdfInvoiceService;
    private final UserRepository userRepository;

    @GetMapping("/my-bills")
    @Operation(summary = "Get billing statements for the currently authenticated resident")
    public ResponseEntity<List<HouseholdBillDto>> getMyBills(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (user == null || user.getHouseholdId() == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(billingCycleService.getBillsByHousehold(user.getHouseholdId()));
    }

    @GetMapping("/cycle/{cycleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all bills for a specific cycle (Admin only)")
    public ResponseEntity<List<HouseholdBillDto>> getBillsByCycle(@PathVariable Long cycleId) {
        List<HouseholdBillDto> bills = billingCycleService.getBillsByCycle(cycleId);
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/household/{householdId}")
    @Operation(summary = "Get all bills for a household (Admins or assigned Resident only)")
    public ResponseEntity<List<HouseholdBillDto>> getBillsByHousehold(
            @PathVariable Long householdId,
            Authentication authentication) {
        verifyHouseholdAccess(householdId, authentication);
        List<HouseholdBillDto> bills = billingCycleService.getBillsByHousehold(householdId);
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/{billId}")
    @Operation(summary = "Get bill details by ID (Admins or assigned Resident only)")
    public ResponseEntity<HouseholdBillDto> getBillById(
            @PathVariable Long billId,
            Authentication authentication) {
        HouseholdBillDto bill = billingCycleService.getBillById(billId);
        verifyHouseholdAccess(bill.getHouseholdId(), authentication);
        return ResponseEntity.ok(bill);
    }

    @GetMapping("/{billId}/pdf")
    @Operation(summary = "Download official itemized PDF invoice (Admins or assigned Resident only)")
    public ResponseEntity<byte[]> downloadPdfInvoice(
            @PathVariable Long billId,
            Authentication authentication) throws IOException {
        HouseholdBillDto bill = billingCycleService.getBillById(billId);
        verifyHouseholdAccess(bill.getHouseholdId(), authentication);

        byte[] pdfBytes = pdfInvoiceService.generatePdfInvoice(billId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice_bill_" + billId + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    private User getAuthenticatedUser(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return null;
        }
        return userRepository.findByEmail(auth.getName().trim().toLowerCase()).orElse(null);
    }

    private void verifyHouseholdAccess(Long targetHouseholdId, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        if (user == null) {
            throw new UnauthorizedException("Authentication required.");
        }
        if (user.getRole() == Role.RESIDENT) {
            if (user.getHouseholdId() == null || !user.getHouseholdId().equals(targetHouseholdId)) {
                throw new UnauthorizedException("Access denied: Residents can only access their own household's billing statements.");
            }
        }
    }
}

