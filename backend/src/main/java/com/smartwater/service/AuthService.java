package com.smartwater.service;

import com.smartwater.dto.*;
import com.smartwater.entity.Role;
import com.smartwater.entity.User;
import com.smartwater.entity.UserStatus;
import com.smartwater.exception.DuplicateResourceException;
import com.smartwater.exception.ResourceNotFoundException;
import com.smartwater.exception.UnauthorizedException;
import com.smartwater.repository.UserRepository;
import com.smartwater.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("Email already registered: " + normalizedEmail);
        }

        Role userRole = Role.RESIDENT;
        if (request.getRole() == Role.ADMIN) {
            // Secure Owner Bootstrap Process:
            // The first owner may be created ONLY when no administrator currently exists in the system.
            // Once an administrator exists, public registration cannot create additional ADMIN accounts.
            boolean adminExists = userRepository.existsByRole(Role.ADMIN);
            if (!adminExists) {
                userRole = Role.ADMIN;
            } else {
                throw new UnauthorizedException("An administrator already exists. Public registration of additional administrator accounts is disabled. Please contact your system administrator.");
            }
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        String successMessage = userRole == Role.ADMIN
                ? "Owner account registered successfully. Please proceed to the Owner Login page to sign in."
                : "Resident account registered successfully. Please proceed to login.";

        return AuthResponse.builder()
                .message(successMessage)
                .user(mapToUserDto(savedUser))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("User account is inactive");
        }

        // Server-side role separation enforcement
        if (request.getRole() != null) {
            if (request.getRole() == Role.ADMIN && user.getRole() != Role.ADMIN) {
                throw new UnauthorizedException("Access denied: This account is registered as a Resident. Please sign in via the Resident Portal.");
            }
            if (request.getRole() == Role.RESIDENT && user.getRole() != Role.RESIDENT) {
                throw new UnauthorizedException("Access denied: This portal is for Residents only. Apartment Owners/Administrators must use the Owner Login page.");
            }
        }

        String token = tokenProvider.generateToken(user.getEmail(), user.getRole());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(mapToUserDto(user))
                .build();
    }

    public UserDto getCurrentUser(String email) {
        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + normalizedEmail));
        return mapToUserDto(user);
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .apartmentId(user.getApartmentId())
                .householdId(user.getHouseholdId())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
