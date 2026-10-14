package com.smartwater.controller;

import com.smartwater.dto.BillingCycleDto;
import com.smartwater.dto.CreateBillingCycleRequest;
import com.smartwater.service.BillingCycleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing-cycles")
@RequiredArgsConstructor
public class BillingCycleController {

    private final BillingCycleService billingCycleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BillingCycleDto> createBillingCycle(@Valid @RequestBody CreateBillingCycleRequest request) {
        BillingCycleDto created = billingCycleService.createBillingCycle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/apartment/{apartmentId}")
    public ResponseEntity<List<BillingCycleDto>> getBillingCycles(@PathVariable Long apartmentId) {
        List<BillingCycleDto> cycles = billingCycleService.getBillingCyclesByApartment(apartmentId);
        return ResponseEntity.ok(cycles);
    }

    @PostMapping("/{cycleId}/finalize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BillingCycleDto> finalizeBillingCycle(@PathVariable Long cycleId) {
        BillingCycleDto finalized = billingCycleService.finalizeBillingCycle(cycleId);
        return ResponseEntity.ok(finalized);
    }
}
