package com.smartwater.service;

import com.smartwater.dto.UpdateProfileRequest;
import com.smartwater.dto.UserProfileDto;
import com.smartwater.entity.User;
import com.smartwater.exception.ResourceNotFoundException;
import com.smartwater.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToDto(user);
    }

    @Transactional
    public UserProfileDto updateProfile(String currentEmail, UpdateProfileRequest request) {
        String normalizedCurrent = currentEmail != null ? currentEmail.trim().toLowerCase() : "";
        User user = userRepository.findByEmail(normalizedCurrent)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + normalizedCurrent));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String normalizedNewEmail = request.getEmail().trim().toLowerCase();
            if (!normalizedNewEmail.equalsIgnoreCase(user.getEmail())) {
                if (userRepository.existsByEmail(normalizedNewEmail)) {
                    throw new com.smartwater.exception.DuplicateResourceException("Email already in use: " + normalizedNewEmail);
                }
                user.setEmail(normalizedNewEmail);
            }
        }

        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword().trim()));
        }

        User saved = userRepository.save(user);
        return mapToDto(saved);
    }

    public java.util.List<UserProfileDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public UserProfileDto assignUserHousehold(Long userId, com.smartwater.dto.AssignUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setApartmentId(request.getApartmentId());
        user.setHouseholdId(request.getHouseholdId());
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            try {
                user.setStatus(com.smartwater.entity.UserStatus.valueOf(request.getStatus().trim().toUpperCase()));
            } catch (Exception ignored) {}
        }

        User updated = userRepository.save(user);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    private UserProfileDto mapToDto(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .apartmentId(user.getApartmentId())
                .householdId(user.getHouseholdId())
                .build();
    }
}
