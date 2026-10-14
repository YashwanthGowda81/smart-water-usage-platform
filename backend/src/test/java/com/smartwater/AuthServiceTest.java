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
    void register_ConcurrentFirstAdminAttempts_OnlyOneSucceeds() throws Exception {
        java.util.concurrent.atomic.AtomicBoolean adminCreated = new java.util.concurrent.atomic.AtomicBoolean(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByRole(Role.ADMIN)).thenAnswer(inv -> adminCreated.get());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            adminCreated.set(true);
            return user;
        });

        int numThreads = 10;
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(numThreads);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicInteger successes = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger rejections = new java.util.concurrent.atomic.AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            final int id = i;
            executor.submit(() -> {
                try {
                    latch.await();
                    RegisterRequest req = RegisterRequest.builder()
                            .name("Admin Attempt " + id)
                            .email("admin" + id + "@smartwater.com")
                            .password("password123")
                            .role(Role.ADMIN)
                            .build();
                    authService.register(req);
                    successes.incrementAndGet();
                } catch (UnauthorizedException ex) {
                    rejections.incrementAndGet();
                } catch (Exception ignored) {
                }
            });
        }

        latch.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS));

        assertEquals(1, successes.get(), "Exactly one concurrent thread must succeed in bootstrapping the first admin");
        assertEquals(numThreads - 1, rejections.get(), "All other concurrent attempts must be rejected with UnauthorizedException");
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

    @Test
    void login_Owner_Success_WhenUserIsAdmin() {
        LoginRequest ownerReq = LoginRequest.builder()
                .email("admin@smartwater.com")
                .password("password123")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findByEmail("admin@smartwater.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", user.getPasswordHash())).thenReturn(true);
        when(tokenProvider.generateToken(user.getEmail(), user.getRole())).thenReturn("jwt_admin_token");

        AuthResponse response = authService.login(ownerReq);

        assertNotNull(response);
        assertEquals("jwt_admin_token", response.getToken());
        assertEquals(Role.ADMIN, response.getUser().getRole());
    }

    @Test
    void login_Owner_RejectsResidentAccount_ThrowsUnauthorizedException() {
        User residentUser = User.builder()
                .id(2L)
                .name("Resident User")
                .email("resident@smartwater.com")
                .passwordHash("encoded_hash")
                .role(Role.RESIDENT)
                .status(UserStatus.ACTIVE)
                .build();

        LoginRequest ownerAttempt = LoginRequest.builder()
                .email("resident@smartwater.com")
                .password("password123")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findByEmail("resident@smartwater.com")).thenReturn(Optional.of(residentUser));
        when(passwordEncoder.matches("password123", residentUser.getPasswordHash())).thenReturn(true);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> authService.login(ownerAttempt));
        assertTrue(ex.getMessage().contains("Access denied: This account is registered as a Resident"));
        verify(tokenProvider, never()).generateToken(any(), any());
    }

    @Test
    void login_Resident_Success_WhenUserIsResident() {
        User residentUser = User.builder()
                .id(2L)
                .name("Resident User")
                .email("resident@smartwater.com")
                .passwordHash("encoded_hash")
                .role(Role.RESIDENT)
                .status(UserStatus.ACTIVE)
                .build();

        LoginRequest residentReq = LoginRequest.builder()
                .email("resident@smartwater.com")
                .password("password123")
                .role(Role.RESIDENT)
                .build();

        when(userRepository.findByEmail("resident@smartwater.com")).thenReturn(Optional.of(residentUser));
        when(passwordEncoder.matches("password123", residentUser.getPasswordHash())).thenReturn(true);
        when(tokenProvider.generateToken(residentUser.getEmail(), residentUser.getRole())).thenReturn("jwt_resident_token");

        AuthResponse response = authService.login(residentReq);

        assertNotNull(response);
        assertEquals("jwt_resident_token", response.getToken());
        assertEquals(Role.RESIDENT, response.getUser().getRole());
    }

    @Test
    void login_Resident_RejectsAdminAccount_ThrowsUnauthorizedException() {
        LoginRequest residentAttempt = LoginRequest.builder()
                .email("admin@smartwater.com")
                .password("password123")
                .role(Role.RESIDENT)
                .build();

        when(userRepository.findByEmail("admin@smartwater.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", user.getPasswordHash())).thenReturn(true);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> authService.login(residentAttempt));
        assertTrue(ex.getMessage().contains("Access denied: This portal is for Residents only"));
        verify(tokenProvider, never()).generateToken(any(), any());
    }

    @Test
    void login_EmailNormalization_Success() {
        LoginRequest unnormalizedReq = LoginRequest.builder()
                .email("  ADMIN@SMARTWATER.COM  ")
                .password("password123")
                .build();

        when(userRepository.findByEmail("admin@smartwater.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", user.getPasswordHash())).thenReturn(true);
        when(tokenProvider.generateToken(user.getEmail(), user.getRole())).thenReturn("jwt_token");

        AuthResponse response = authService.login(unnormalizedReq);

        assertNotNull(response);
        assertEquals("admin@smartwater.com", response.getUser().getEmail());
        verify(userRepository).findByEmail("admin@smartwater.com");
    }

    @Test
    void login_InactiveUser_ThrowsUnauthorizedException() {
        User inactiveUser = User.builder()
                .id(3L)
                .name("Inactive User")
                .email("inactive@smartwater.com")
                .passwordHash("encoded_hash")
                .role(Role.RESIDENT)
                .status(UserStatus.INACTIVE)
                .build();

        LoginRequest inactiveReq = LoginRequest.builder()
                .email("inactive@smartwater.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail("inactive@smartwater.com")).thenReturn(Optional.of(inactiveUser));
        when(passwordEncoder.matches("password123", inactiveUser.getPasswordHash())).thenReturn(true);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> authService.login(inactiveReq));
        assertTrue(ex.getMessage().contains("inactive"));
        verify(tokenProvider, never()).generateToken(any(), any());
    }
}
