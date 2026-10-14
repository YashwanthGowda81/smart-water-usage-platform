package com.smartwater.controller;

import com.smartwater.dto.AnomalyEventDto;
import com.smartwater.service.AnomalyDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anomalies")
@RequiredArgsConstructor
public class AnomalyEventController {

    private final AnomalyDetectionService anomalyDetectionService;

    @GetMapping("/open")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AnomalyEventDto>> getOpenAnomalies() {
        List<AnomalyEventDto> anomalies = anomalyDetectionService.getAllOpenAnomalies();
        return ResponseEntity.ok(anomalies);
    }

    @GetMapping("/household/{householdId}")
    public ResponseEntity<List<AnomalyEventDto>> getHouseholdAnomalies(@PathVariable Long householdId) {
        List<AnomalyEventDto> anomalies = anomalyDetectionService.getAnomaliesByHousehold(householdId);
        return ResponseEntity.ok(anomalies);
    }

    @PostMapping("/evaluate/{householdId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AnomalyEventDto>> evaluateHouseholdAnomalies(@PathVariable Long householdId) {
        List<AnomalyEventDto> anomalies = anomalyDetectionService.evaluateHouseholdAnomalies(householdId);
        return ResponseEntity.ok(anomalies);
    }
}
