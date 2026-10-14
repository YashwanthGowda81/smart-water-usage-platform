package com.smartwater;

import com.smartwater.dto.AssignUserRequest;
import com.smartwater.dto.UpdateProfileRequest;
import com.smartwater.dto.UserProfileDto;
import com.smartwater.entity.Apartment;
import com.smartwater.entity.Household;
import com.smartwater.entity.Role;
import com.smartwater.entity.User;
import com.smartwater.entity.UserStatus;
import com.smartwater.exception.InvalidReadingException;
import com.smartwater.exception.ResourceNotFoundException;
import com.smartwater.repository.ApartmentRepository;
import com.smartwater.repository.HouseholdRepository;
import com.smartwater.repository.UserRepository;
import com.smartwater.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApartmentRepository apartmentRepository;

    @Mock
    private HouseholdRepository householdRepository;

    @InjectMocks
    private UserService userService;

    private User residentUser;
    private Apartment apartment;
    private Household household;

    @BeforeEach
    void setUp() {
        residentUser = User.builder()
                .id(5L)
                .name("John Doe")
                .email("john@example.com")
                .passwordHash("hashed_pw")
                .role(Role.RESIDENT)
                .status(UserStatus.ACTIVE)
                .build();

        apartment = Apartment.builder()
                .id(1L)
                .name("Skyline Heights")
                .build();

        household = Household.builder()
                .id(10L)
                .apartment(apartment)
                .flatNumber("A-101")
                .build();
    }

    @Test
    void assignUserHousehold_Success() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(residentUser));
        when(apartmentRepository.existsById(1L)).thenReturn(true);
        when(householdRepository.findById(10L)).thenReturn(Optional.of(household));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        AssignUserRequest request = AssignUserRequest.builder()
                .apartmentId(1L)
                .householdId(10L)
                .status("ACTIVE")
                .build();

        UserProfileDto result = userService.assignUserHousehold(5L, request);

        assertNotNull(result);
        assertEquals(1L, result.getApartmentId());
        assertEquals(10L, result.getHouseholdId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void assignUserHousehold_ApartmentNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(residentUser));
        when(apartmentRepository.existsById(99L)).thenReturn(false);

        AssignUserRequest request = AssignUserRequest.builder()
                .apartmentId(99L)
                .householdId(10L)
                .build();

        assertThrows(ResourceNotFoundException.class, () -> userService.assignUserHousehold(5L, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void assignUserHousehold_HouseholdNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(residentUser));
        when(apartmentRepository.existsById(1L)).thenReturn(true);
        when(householdRepository.findById(99L)).thenReturn(Optional.empty());

        AssignUserRequest request = AssignUserRequest.builder()
                .apartmentId(1L)
                .householdId(99L)
                .build();

        assertThrows(ResourceNotFoundException.class, () -> userService.assignUserHousehold(5L, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void assignUserHousehold_HouseholdApartmentMismatch_ThrowsInvalidReadingException() {
        Apartment otherApartment = Apartment.builder().id(2L).name("Other Towers").build();
        Household mismatchedHousehold = Household.builder().id(20L).apartment(otherApartment).flatNumber("B-201").build();

        when(userRepository.findById(5L)).thenReturn(Optional.of(residentUser));
        when(apartmentRepository.existsById(1L)).thenReturn(true);
        when(householdRepository.findById(20L)).thenReturn(Optional.of(mismatchedHousehold));

        AssignUserRequest request = AssignUserRequest.builder()
                .apartmentId(1L)
                .householdId(20L)
                .build();

        assertThrows(InvalidReadingException.class, () -> userService.assignUserHousehold(5L, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_Success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(residentUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name("Johnathan Doe")
                .build();

        UserProfileDto result = userService.updateProfile("john@example.com", request);

        assertNotNull(result);
        assertEquals("Johnathan Doe", result.getName());
    }
}
