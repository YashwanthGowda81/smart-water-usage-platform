package com.smartwater.controller;

import com.smartwater.dto.BulkWaterPurchaseDto;
import com.smartwater.dto.CreateBulkPurchaseRequest;
import com.smartwater.entity.User;
import com.smartwater.repository.UserRepository;
import com.smartwater.service.BulkWaterPurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Bulk Water Purchases", description = "Endpoints for Municipal and Private Tanker Bulk Procurement")
public class BulkWaterPurchaseController {

    private final BulkWaterPurchaseService purchaseService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Record a bulk water procurement transaction (Admin only)")
    public ResponseEntity<BulkWaterPurchaseDto> createPurchase(
            @Valid @RequestBody CreateBulkPurchaseRequest request,
            Authentication authentication) {
        Long userId = null;
        if (authentication != null && authentication.getName() != null) {
            userId = userRepository.findByEmail(authentication.getName().trim().toLowerCase())
                    .map(User::getId)
                    .orElse(null);
        }
        BulkWaterPurchaseDto created = purchaseService.createPurchase(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/apartment/{apartmentId}")
    @Operation(summary = "Get all bulk purchases recorded for an apartment")
    public ResponseEntity<List<BulkWaterPurchaseDto>> getPurchasesByApartment(@PathVariable Long apartmentId) {
        List<BulkWaterPurchaseDto> purchases = purchaseService.getPurchasesByApartment(apartmentId);
        return ResponseEntity.ok(purchases);
    }
}

