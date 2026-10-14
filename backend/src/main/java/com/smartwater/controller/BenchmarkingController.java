package com.smartwater.controller;

import com.smartwater.dto.BenchmarkingDto;
import com.smartwater.service.BenchmarkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/benchmarking")
@RequiredArgsConstructor
public class BenchmarkingController {

    private final BenchmarkingService benchmarkingService;

    @GetMapping("/household/{householdId}")
    public ResponseEntity<BenchmarkingDto> getHouseholdBenchmarking(@PathVariable Long householdId) {
        BenchmarkingDto benchmarking = benchmarkingService.getHouseholdBenchmarking(householdId);
        return ResponseEntity.ok(benchmarking);
    }
}
