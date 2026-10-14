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

    @GetMapping("/{cycleId}")
    public ResponseEntity<BillingCycleDto> getBillingCycleById(@PathVariable Long cycleId) {
        BillingCycleDto cycle = billingCycleService.getBillingCycle(cycleId);
        return ResponseEntity.ok(cycle);
    }

    @PostMapping("/{cycleId}/calculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BillingCycleDto> calculateBills(@PathVariable Long cycleId) {
        BillingCycleDto calculated = billingCycleService.calculateBills(cycleId);
        return ResponseEntity.ok(calculated);
    }

    @PostMapping("/{cycleId}/finalize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BillingCycleDto> finalizeBillingCycle(@PathVariable Long cycleId) {
        BillingCycleDto finalized = billingCycleService.finalizeBillingCycle(cycleId);
        return ResponseEntity.ok(finalized);
    }

    @PostMapping("/{cycleId}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BillingCycleDto> archiveBillingCycle(@PathVariable Long cycleId) {
        BillingCycleDto archived = billingCycleService.archiveBillingCycle(cycleId);
        return ResponseEntity.ok(archived);
    }
}
