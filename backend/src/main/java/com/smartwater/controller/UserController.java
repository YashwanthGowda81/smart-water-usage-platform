package com.smartwater.controller;

import com.smartwater.dto.UpdateProfileRequest;
import com.smartwater.dto.UserProfileDto;
import com.smartwater.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile(Authentication authentication) {
        UserProfileDto profile = userService.getUserProfile(authentication.getName());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileDto updated = userService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(updated);
    }
    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.List<UserProfileDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{id}/assign")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDto> assignUserHousehold(
            @PathVariable Long id,
            @RequestBody com.smartwater.dto.AssignUserRequest request) {
        return ResponseEntity.ok(userService.assignUserHousehold(id, request));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
