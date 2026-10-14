package com.smartwater.controller;

import com.smartwater.dto.ApartmentDto;
import com.smartwater.dto.CreateApartmentRequest;
import com.smartwater.service.ApartmentService;
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
@RequestMapping("/api/apartments")
@RequiredArgsConstructor
@Tag(name = "Apartments", description = "Endpoints for Apartment Community Management")
public class ApartmentController {

    private final ApartmentService apartmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new apartment community (Admin only)")
    public ResponseEntity<ApartmentDto> createApartment(@Valid @RequestBody CreateApartmentRequest request) {
        ApartmentDto apartment = apartmentService.createApartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(apartment);
    }

    @GetMapping
    @Operation(summary = "Get list of all apartments")
    public ResponseEntity<List<ApartmentDto>> getAllApartments() {
        return ResponseEntity.ok(apartmentService.getAllApartments());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get apartment details by ID")
    public ResponseEntity<ApartmentDto> getApartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(apartmentService.getApartmentById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update apartment details (Admin only)")
    public ResponseEntity<ApartmentDto> updateApartment(@PathVariable Long id, @Valid @RequestBody CreateApartmentRequest request) {
        return ResponseEntity.ok(apartmentService.updateApartment(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete apartment (Admin only)")
    public ResponseEntity<Void> deleteApartment(@PathVariable Long id) {
        apartmentService.deleteApartment(id);
        return ResponseEntity.noContent().build();
    }
}
