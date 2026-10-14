package com.smartwater.controller;

import com.smartwater.dto.HouseholdBillDto;
import com.smartwater.service.BillingCycleService;
import com.smartwater.service.PdfInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class HouseholdBillController {

    private final BillingCycleService billingCycleService;
    private final PdfInvoiceService pdfInvoiceService;

    @GetMapping("/cycle/{cycleId}")
    public ResponseEntity<List<HouseholdBillDto>> getBillsByCycle(@PathVariable Long cycleId) {
        List<HouseholdBillDto> bills = billingCycleService.getBillsByCycle(cycleId);
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/household/{householdId}")
    public ResponseEntity<List<HouseholdBillDto>> getBillsByHousehold(@PathVariable Long householdId) {
        List<HouseholdBillDto> bills = billingCycleService.getBillsByHousehold(householdId);
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/{billId}")
    public ResponseEntity<HouseholdBillDto> getBillById(@PathVariable Long billId) {
        HouseholdBillDto bill = billingCycleService.getBillById(billId);
        return ResponseEntity.ok(bill);
    }

    @GetMapping("/{billId}/pdf")
    public ResponseEntity<byte[]> downloadPdfInvoice(@PathVariable Long billId) throws IOException {
        byte[] pdfBytes = pdfInvoiceService.generatePdfInvoice(billId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice_bill_" + billId + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
