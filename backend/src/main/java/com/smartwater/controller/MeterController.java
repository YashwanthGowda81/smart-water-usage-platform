package com.smartwater.controller;

import com.smartwater.dto.CreateMeterRequest;
import com.smartwater.dto.MeterDto;
import com.smartwater.service.MeterService;
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
@RequestMapping("/api/meters")
@RequiredArgsConstructor
@Tag(name = "Meters", description = "Endpoints for Water Meter Configuration")
public class MeterController {

    private final MeterService meterService;
    private final com.smartwater.repository.UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Configure and assign a new meter to a household (Admin only)")
    public ResponseEntity<MeterDto> createMeter(@Valid @RequestBody CreateMeterRequest request) {
        MeterDto meter = meterService.createMeter(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(meter);
    }

    @GetMapping
    @Operation(summary = "Get list of all meters (optional filter by householdId; filtered to household for residents)")
    public ResponseEntity<List<MeterDto>> getAllMeters(
            @RequestParam(required = false) Long householdId,
            org.springframework.security.core.Authentication authentication) {
        com.smartwater.entity.User user = getAuthenticatedUser(authentication);
        if (user != null && user.getRole() == com.smartwater.entity.Role.RESIDENT) {
            if (user.getHouseholdId() == null) {
                return ResponseEntity.ok(java.util.Collections.emptyList());
            }
            return ResponseEntity.ok(meterService.getMetersByHousehold(user.getHouseholdId()));
        }
        if (householdId != null) {
            return ResponseEntity.ok(meterService.getMetersByHousehold(householdId));
        }
        return ResponseEntity.ok(meterService.getAllMeters());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get meter details by ID (Admins or assigned Resident only)")
    public ResponseEntity<MeterDto> getMeterById(
            @PathVariable Long id,
            org.springframework.security.core.Authentication authentication) {
        MeterDto meter = meterService.getMeterById(id);
        verifyMeterAccess(meter, authentication);
        return ResponseEntity.ok(meter);
    }

    @GetMapping("/number/{meterNumber}")
    @Operation(summary = "Get meter details by unique meter number (Admins or assigned Resident only)")
    public ResponseEntity<MeterDto> getMeterByNumber(
            @PathVariable String meterNumber,
            org.springframework.security.core.Authentication authentication) {
        MeterDto meter = meterService.getMeterByMeterNumber(meterNumber);
        verifyMeterAccess(meter, authentication);
        return ResponseEntity.ok(meter);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update meter configuration (Admin only)")
    public ResponseEntity<MeterDto> updateMeter(@PathVariable Long id, @Valid @RequestBody CreateMeterRequest request) {
        return ResponseEntity.ok(meterService.updateMeter(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete meter configuration (Admin only)")
    public ResponseEntity<Void> deleteMeter(@PathVariable Long id) {
        meterService.deleteMeter(id);
        return ResponseEntity.noContent().build();
    }

    private com.smartwater.entity.User getAuthenticatedUser(org.springframework.security.core.Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return null;
        }
        return userRepository.findByEmail(auth.getName().trim().toLowerCase()).orElse(null);
    }

    private void verifyMeterAccess(MeterDto meter, org.springframework.security.core.Authentication auth) {
        com.smartwater.entity.User user = getAuthenticatedUser(auth);
        if (user == null) {
            throw new com.smartwater.exception.UnauthorizedException("Authentication required.");
        }
        if (user.getRole() == com.smartwater.entity.Role.RESIDENT) {
            if (user.getHouseholdId() == null || !user.getHouseholdId().equals(meter.getHouseholdId())) {
                throw new com.smartwater.exception.UnauthorizedException("Access denied: Residents can only access their own household's meters.");
            }
        }
    }
}
