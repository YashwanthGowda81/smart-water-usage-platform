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
    @Operation(summary = "Get all water usage logs")
    public ResponseEntity<List<WaterUsageLogDto>> getAllUsageLogs() {
        return ResponseEntity.ok(waterUsageService.getAllUsageLogs());
    }

    @GetMapping("/template")
    @Operation(summary = "Download standard CSV template for bulk water usage upload")
    public ResponseEntity<byte[]> downloadCsvTemplate() {
        String template = "meterNumber,readingDate,currentReading\nMTR-1001,2026-10-01,1250.00\nMTR-1002,2026-10-01,1540.50\n";
        byte[] bytes = template.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=water_meter_readings_template.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    @GetMapping("/meter/{meterNumber}")
    @Operation(summary = "Get water usage history for a specific meter number")
    public ResponseEntity<List<WaterUsageLogDto>> getUsageByMeter(@PathVariable String meterNumber) {
        return ResponseEntity.ok(waterUsageService.getUsageByMeter(meterNumber));
    }
}
