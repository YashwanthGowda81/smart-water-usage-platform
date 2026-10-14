package com.smartwater.controller;

import com.smartwater.dto.*;
import com.smartwater.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for User Registration and JWT Authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user (Admin or Resident)")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register/resident")
    @Operation(summary = "Register a new resident (Always assigns RESIDENT role on backend)")
    public ResponseEntity<AuthResponse> registerResident(@Valid @RequestBody RegisterRequest request) {
        // Enforce that resident registration always assigns RESIDENT role, never trusting public input
        request.setRole(com.smartwater.entity.Role.RESIDENT);
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register/owner")
    @Operation(summary = "Register initial apartment owner (Initial bootstrap only)")
    public ResponseEntity<AuthResponse> registerOwner(@Valid @RequestBody RegisterRequest request) {
        request.setRole(com.smartwater.entity.Role.ADMIN);
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and receive JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/resident")
    @Operation(summary = "Authenticate resident credentials (rejects non-resident accounts)")
    public ResponseEntity<AuthResponse> loginResident(@Valid @RequestBody LoginRequest request) {
        request.setRole(com.smartwater.entity.Role.RESIDENT);
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/owner")
    @Operation(summary = "Authenticate owner credentials (rejects non-admin accounts)")
    public ResponseEntity<AuthResponse> loginOwner(@Valid @RequestBody LoginRequest request) {
        request.setRole(com.smartwater.entity.Role.ADMIN);
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get currently authenticated user details")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        UserDto user = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(user);
    }
}
