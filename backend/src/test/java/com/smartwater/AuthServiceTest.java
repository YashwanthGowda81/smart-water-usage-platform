package com.smartwater;

import com.smartwater.dto.AuthResponse;
import com.smartwater.dto.LoginRequest;
import com.smartwater.dto.RegisterRequest;
import com.smartwater.entity.Role;
import com.smartwater.entity.User;
import com.smartwater.entity.UserStatus;
import com.smartwater.exception.DuplicateResourceException;
import com.smartwater.exception.UnauthorizedException;
import com.smartwater.repository.UserRepository;
import com.smartwater.security.JwtTokenProvider;
import com.smartwater.service.AuthService;
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
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .name("Admin User")
                .email("admin@smartwater.com")
                .password("password123")
                .role(Role.ADMIN)
                .build();

        loginRequest = LoginRequest.builder()
                .email("admin@smartwater.com")
                .password("password123")
                .build();

        user = User.builder()
                .id(1L)
                .name("Admin User")
                .email("admin@smartwater.com")
                .passwordHash("encoded_hash")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void register_FirstOwner_Success_ReturnsMessageAndNoToken() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_hash");
        when(userRepository.save(any(User.class))).thenReturn(user);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertNull(response.getToken(), "Registration must not issue token or auto-login");
        assertNotNull(response.getMessage());
        assertTrue(response.getMessage().contains("Owner account registered successfully"));
        assertEquals("admin@smartwater.com", response.getUser().getEmail());
        assertEquals(Role.ADMIN, response.getUser().getRole());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_SecondAdmin_WhenAdminAlreadyExists_ThrowsUnauthorizedException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(true);

        RegisterRequest secondAdminReq = RegisterRequest.builder()
                .name("Second Owner Attempt")
                .email("second@smartwater.com")
                .password("password123")
                .role(Role.ADMIN)
                .build();

        assertThrows(UnauthorizedException.class, () -> authService.register(secondAdminReq));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_Resident_Success_WhenAdminAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_hash");

        User residentUser = User.builder()
                .id(2L)
                .name("Resident User")
                .email("resident@smartwater.com")
                .passwordHash("encoded_hash")
                .role(Role.RESIDENT)
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(residentUser);

        RegisterRequest residentReq = RegisterRequest.builder()
                .name("Resident User")
                .email("resident@smartwater.com")
                .password("password123")
                .role(Role.RESIDENT)
                .build();

        AuthResponse response = authService.register(residentReq);

        assertNotNull(response);
        assertNull(response.getToken(), "Registration must not auto-login");
        assertEquals(Role.RESIDENT, response.getUser().getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void login_Success() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())).thenReturn(true);
        when(tokenProvider.generateToken(user.getEmail(), user.getRole())).thenReturn("jwt_mock_token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt_mock_token", response.getToken());
        assertEquals("admin@smartwater.com", response.getUser().getEmail());
    }

    @Test
    void login_InvalidPassword_ThrowsUnauthorizedException() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(loginRequest));
    }
}
