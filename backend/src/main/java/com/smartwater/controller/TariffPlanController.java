package com.smartwater.controller;

import com.smartwater.dto.CreateTariffPlanRequest;
import com.smartwater.dto.TariffPlanDto;
import com.smartwater.service.TariffPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tariffs")
@RequiredArgsConstructor
public class TariffPlanController {

    private final TariffPlanService tariffPlanService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TariffPlanDto> createTariffPlan(@Valid @RequestBody CreateTariffPlanRequest request) {
        TariffPlanDto created = tariffPlanService.createTariffPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/apartment/{apartmentId}")
    public ResponseEntity<List<TariffPlanDto>> getTariffPlans(@PathVariable Long apartmentId) {
        List<TariffPlanDto> plans = tariffPlanService.getTariffPlansByApartment(apartmentId);
        return ResponseEntity.ok(plans);
    }
}
