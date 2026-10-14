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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Configure and assign a new meter to a household (Admin only)")
    public ResponseEntity<MeterDto> createMeter(@Valid @RequestBody CreateMeterRequest request) {
        MeterDto meter = meterService.createMeter(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(meter);
    }

    @GetMapping
    @Operation(summary = "Get list of all meters (optional filter by householdId)")
    public ResponseEntity<List<MeterDto>> getAllMeters(@RequestParam(required = false) Long householdId) {
        if (householdId != null) {
            return ResponseEntity.ok(meterService.getMetersByHousehold(householdId));
        }
        return ResponseEntity.ok(meterService.getAllMeters());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get meter details by ID")
    public ResponseEntity<MeterDto> getMeterById(@PathVariable Long id) {
        return ResponseEntity.ok(meterService.getMeterById(id));
    }

    @GetMapping("/number/{meterNumber}")
    @Operation(summary = "Get meter details by unique meter number")
    public ResponseEntity<MeterDto> getMeterByNumber(@PathVariable String meterNumber) {
        return ResponseEntity.ok(meterService.getMeterByMeterNumber(meterNumber));
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
}
