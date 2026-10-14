package com.smartwater.controller;

import com.smartwater.dto.BulkWaterPurchaseDto;
import com.smartwater.dto.CreateBulkPurchaseRequest;
import com.smartwater.service.BulkWaterPurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class BulkWaterPurchaseController {

    private final BulkWaterPurchaseService purchaseService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkWaterPurchaseDto> createPurchase(
            @Valid @RequestBody CreateBulkPurchaseRequest request,
            Authentication authentication) {
        BulkWaterPurchaseDto created = purchaseService.createPurchase(request, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/apartment/{apartmentId}")
    public ResponseEntity<List<BulkWaterPurchaseDto>> getPurchasesByApartment(@PathVariable Long apartmentId) {
        List<BulkWaterPurchaseDto> purchases = purchaseService.getPurchasesByApartment(apartmentId);
        return ResponseEntity.ok(purchases);
    }
}
