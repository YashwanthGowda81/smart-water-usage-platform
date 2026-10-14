package com.smartwater.controller;

import com.smartwater.dto.AnomalyEventDto;
import com.smartwater.entity.Role;
import com.smartwater.entity.User;
import com.smartwater.exception.UnauthorizedException;
import com.smartwater.repository.UserRepository;
import com.smartwater.service.AnomalyDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anomalies")
@RequiredArgsConstructor
@Tag(name = "Anomalies", description = "Endpoints for Statistical Water Leak & Anomaly Detection")
public class AnomalyEventController {

    private final AnomalyDetectionService anomalyDetectionService;
    private final UserRepository userRepository;

    @GetMapping("/open")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all open anomalies across all households (Admin only)")
    public ResponseEntity<List<AnomalyEventDto>> getOpenAnomalies() {
        List<AnomalyEventDto> anomalies = anomalyDetectionService.getAllOpenAnomalies();
        return ResponseEntity.ok(anomalies);
    }

    @GetMapping("/household/{householdId}")
    @Operation(summary = "Get anomaly history for a household (Admins or assigned Resident only)")
    public ResponseEntity<List<AnomalyEventDto>> getHouseholdAnomalies(
            @PathVariable Long householdId,
            Authentication authentication) {
        verifyHouseholdAccess(householdId, authentication);
        List<AnomalyEventDto> anomalies = anomalyDetectionService.getAnomaliesByHousehold(householdId);
        return ResponseEntity.ok(anomalies);
    }

    @PostMapping({"/evaluate/{householdId}", "/check/{householdId}"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Trigger statistical anomaly leak evaluation for a household (Admin only)")
    public ResponseEntity<List<AnomalyEventDto>> evaluateHouseholdAnomalies(@PathVariable Long householdId) {
        List<AnomalyEventDto> anomalies = anomalyDetectionService.evaluateHouseholdAnomalies(householdId);
        return ResponseEntity.ok(anomalies);
    }

    @PutMapping("/{anomalyId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update anomaly resolution status (Admin only)")
    public ResponseEntity<AnomalyEventDto> updateAnomalyStatus(
            @PathVariable Long anomalyId,
            @RequestParam(required = false, defaultValue = "RESOLVED") String status) {
        AnomalyEventDto updated = anomalyDetectionService.updateAnomalyStatus(anomalyId, status);
        return ResponseEntity.ok(updated);
    }

    private User getAuthenticatedUser(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return null;
        }
        return userRepository.findByEmail(auth.getName().trim().toLowerCase()).orElse(null);
    }

    private void verifyHouseholdAccess(Long targetHouseholdId, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        if (user == null) {
            throw new UnauthorizedException("Authentication required.");
        }
        if (user.getRole() == Role.RESIDENT) {
            if (user.getHouseholdId() == null || !user.getHouseholdId().equals(targetHouseholdId)) {
                throw new UnauthorizedException("Access denied: Residents can only access their own household's anomaly alerts.");
            }
        }
    }
}

