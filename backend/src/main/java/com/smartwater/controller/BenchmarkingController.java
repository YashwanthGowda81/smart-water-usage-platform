package com.smartwater.controller;

import com.smartwater.dto.BenchmarkingDto;
import com.smartwater.entity.Role;
import com.smartwater.entity.User;
import com.smartwater.exception.ResourceNotFoundException;
import com.smartwater.exception.UnauthorizedException;
import com.smartwater.repository.UserRepository;
import com.smartwater.service.BenchmarkingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/benchmarking")
@RequiredArgsConstructor
@Tag(name = "Benchmarking", description = "Endpoints for Community Water Consumption Benchmarking and Efficiency Scores")
public class BenchmarkingController {

    private final BenchmarkingService benchmarkingService;
    private final UserRepository userRepository;

    @GetMapping("/my-household")
    @Operation(summary = "Get benchmarking efficiency scores for the authenticated resident's household")
    public ResponseEntity<BenchmarkingDto> getMyHouseholdBenchmarking(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (user == null || user.getHouseholdId() == null) {
            throw new ResourceNotFoundException("No assigned household found for current user.");
        }
        BenchmarkingDto benchmarking = benchmarkingService.getHouseholdBenchmarking(user.getHouseholdId());
        return ResponseEntity.ok(benchmarking);
    }

    @GetMapping("/household/{householdId}")
    @Operation(summary = "Get benchmarking comparison for a household (Admins or assigned Resident only)")
    public ResponseEntity<BenchmarkingDto> getHouseholdBenchmarking(
            @PathVariable Long householdId,
            Authentication authentication) {
        verifyHouseholdAccess(householdId, authentication);
        BenchmarkingDto benchmarking = benchmarkingService.getHouseholdBenchmarking(householdId);
        return ResponseEntity.ok(benchmarking);
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
                throw new UnauthorizedException("Access denied: Residents can only access their own household's benchmarking score.");
            }
        }
    }
}

