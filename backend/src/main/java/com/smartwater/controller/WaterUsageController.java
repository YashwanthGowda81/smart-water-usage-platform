package com.smartwater.controller;

import com.smartwater.dto.CreateWaterUsageRequest;
import com.smartwater.dto.CsvUploadSummaryDto;
import com.smartwater.dto.WaterUsageLogDto;
import com.smartwater.service.CsvUploadService;
import com.smartwater.service.WaterUsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
@Tag(name = "Water Usage", description = "Endpoints for Water Meter Reading Logging and CSV Bulk Upload")
public class WaterUsageController {

    private final WaterUsageService waterUsageService;
    private final CsvUploadService csvUploadService;
    private final com.smartwater.service.MeterService meterService;
    private final com.smartwater.repository.UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Record a single manual or simulated water reading (Admin only)")
    public ResponseEntity<WaterUsageLogDto> recordWaterUsage(@Valid @RequestBody CreateWaterUsageRequest request) {
        WaterUsageLogDto log = waterUsageService.recordWaterUsage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(log);
    }

    @PostMapping(value = "/upload-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Upload water meter readings in bulk via CSV (Admin only)")
    public ResponseEntity<CsvUploadSummaryDto> uploadCsvReadings(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("CSV file cannot be empty");
        }
        CsvUploadSummaryDto summary = csvUploadService.processCsvFile(file);
        return ResponseEntity.ok(summary);
    }

    @GetMapping
    @Operation(summary = "Get water usage logs (Filtered to resident household for residents; all logs for admin)")
    public ResponseEntity<List<WaterUsageLogDto>> getAllUsageLogs(org.springframework.security.core.Authentication authentication) {
        com.smartwater.entity.User user = getAuthenticatedUser(authentication);
        if (user != null && user.getRole() == com.smartwater.entity.Role.RESIDENT) {
            if (user.getHouseholdId() == null) {
                return ResponseEntity.ok(java.util.Collections.emptyList());
            }
            return ResponseEntity.ok(waterUsageService.getUsageByHousehold(user.getHouseholdId()));
        }
        return ResponseEntity.ok(waterUsageService.getAllUsageLogs());
    }

    @GetMapping("/template")
    @Operation(summary = "Download standard CSV template for bulk water usage upload")
    public ResponseEntity<byte[]> downloadCsvTemplate() {
        String template = "meter_number,reading_date,current_reading\nMTR-1001,2026-10-01,1250.00\nMTR-1002,2026-10-01,1540.50\n";
        byte[] bytes = template.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=water_meter_readings_template.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    @GetMapping("/meter/{meterNumber}")
    @Operation(summary = "Get water usage history for a specific meter number (Admins or assigned Resident only)")
    public ResponseEntity<List<WaterUsageLogDto>> getUsageByMeter(
            @PathVariable String meterNumber,
            org.springframework.security.core.Authentication authentication) {
        com.smartwater.entity.User user = getAuthenticatedUser(authentication);
        if (user != null && user.getRole() == com.smartwater.entity.Role.RESIDENT) {
            com.smartwater.dto.MeterDto meter = meterService.getMeterByMeterNumber(meterNumber);
            if (user.getHouseholdId() == null || !user.getHouseholdId().equals(meter.getHouseholdId())) {
                throw new com.smartwater.exception.UnauthorizedException("Access denied: Residents can only access their own household's meter history.");
            }
        }
        return ResponseEntity.ok(waterUsageService.getUsageByMeter(meterNumber));
    }

    private com.smartwater.entity.User getAuthenticatedUser(org.springframework.security.core.Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return null;
        }
        return userRepository.findByEmail(auth.getName().trim().toLowerCase()).orElse(null);
    }
}
