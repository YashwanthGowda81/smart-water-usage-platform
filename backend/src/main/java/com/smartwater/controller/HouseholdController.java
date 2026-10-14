package com.smartwater.controller;

import com.smartwater.dto.CreateHouseholdRequest;
import com.smartwater.dto.HouseholdDto;
import com.smartwater.service.HouseholdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/households")
@RequiredArgsConstructor
@Tag(name = "Households", description = "Endpoints for Household & Unit Management")
public class HouseholdController {

    private final HouseholdService householdService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register a household in an apartment (Admin only)")
    public ResponseEntity<HouseholdDto> createHousehold(@Valid @RequestBody CreateHouseholdRequest request) {
        HouseholdDto household = householdService.createHousehold(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(household);
    }

    @GetMapping
    @Operation(summary = "Get list of all households (optional filter by apartmentId)")
    public ResponseEntity<List<HouseholdDto>> getAllHouseholds(@RequestParam(required = false) Long apartmentId) {
        if (apartmentId != null) {
            return ResponseEntity.ok(householdService.getHouseholdsByApartment(apartmentId));
        }
        return ResponseEntity.ok(householdService.getAllHouseholds());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get household details by ID")
    public ResponseEntity<HouseholdDto> getHouseholdById(@PathVariable Long id) {
        return ResponseEntity.ok(householdService.getHouseholdById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update household details (Admin only)")
    public ResponseEntity<HouseholdDto> updateHousehold(@PathVariable Long id, @Valid @RequestBody CreateHouseholdRequest request) {
        return ResponseEntity.ok(householdService.updateHousehold(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete household (Admin only)")
    public ResponseEntity<Void> deleteHousehold(@PathVariable Long id) {
        householdService.deleteHousehold(id);
        return ResponseEntity.noContent().build();
    }
}
