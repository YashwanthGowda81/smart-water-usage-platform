package com.smartwater;

import com.smartwater.controller.AuthController;
import com.smartwater.dto.AuthResponse;
import com.smartwater.dto.LoginRequest;
import com.smartwater.dto.RegisterRequest;
import com.smartwater.dto.UserDto;
import com.smartwater.entity.Role;
import com.smartwater.entity.UserStatus;
import com.smartwater.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .name("Test User")
                .email("test@smartwater.com")
                .password("Password123")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@smartwater.com")
                .password("Password123")
                .build();

        authResponse = AuthResponse.builder()
                .message("Success")
                .user(UserDto.builder()
                        .id(1L)
                        .name("Test User")
                        .email("test@smartwater.com")
                        .role(Role.RESIDENT)
                        .status(UserStatus.ACTIVE)
                        .build())
                .build();
    }

    @Test
    void testRegisterResidentEndpoint_AlwaysForcesResidentRole() {
        // Even if client passed ADMIN in request, endpoint must force RESIDENT
        registerRequest.setRole(Role.ADMIN);
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.registerResident(registerRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        ArgumentCaptor<RegisterRequest> captor = ArgumentCaptor.forClass(RegisterRequest.class);
        verify(authService).register(captor.capture());
        assertEquals(Role.RESIDENT, captor.getValue().getRole(), "Resident registration must ALWAYS assign RESIDENT role");
    }

    @Test
    void testRegisterOwnerEndpoint_ForcesAdminRole() {
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.registerOwner(registerRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        ArgumentCaptor<RegisterRequest> captor = ArgumentCaptor.forClass(RegisterRequest.class);
        verify(authService).register(captor.capture());
        assertEquals(Role.ADMIN, captor.getValue().getRole(), "Owner registration must assign ADMIN role for bootstrap check");
    }

    @Test
    void testLoginResidentEndpoint_ForcesResidentRole() {
        AuthResponse loginAuthResponse = AuthResponse.builder()
                .token("jwt_resident_token")
                .tokenType("Bearer")
                .build();
        when(authService.login(any(LoginRequest.class))).thenReturn(loginAuthResponse);

        ResponseEntity<AuthResponse> response = authController.loginResident(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt_resident_token", response.getBody().getToken());

        ArgumentCaptor<LoginRequest> captor = ArgumentCaptor.forClass(LoginRequest.class);
        verify(authService).login(captor.capture());
        assertEquals(Role.RESIDENT, captor.getValue().getRole(), "Resident login must enforce RESIDENT role check");
    }

    @Test
    void testLoginOwnerEndpoint_ForcesAdminRole() {
        AuthResponse loginAuthResponse = AuthResponse.builder()
                .token("jwt_admin_token")
                .tokenType("Bearer")
                .build();
        when(authService.login(any(LoginRequest.class))).thenReturn(loginAuthResponse);

        ResponseEntity<AuthResponse> response = authController.loginOwner(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt_admin_token", response.getBody().getToken());

        ArgumentCaptor<LoginRequest> captor = ArgumentCaptor.forClass(LoginRequest.class);
        verify(authService).login(captor.capture());
        assertEquals(Role.ADMIN, captor.getValue().getRole(), "Owner login must enforce ADMIN role check");
    }

    @Test
    void testGetCurrentUserEndpoint_ReturnsUserDto() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("Test User")
                .email("test@smartwater.com")
                .role(Role.RESIDENT)
                .build();

        when(authentication.getName()).thenReturn("test@smartwater.com");
        when(authService.getCurrentUser("test@smartwater.com")).thenReturn(userDto);

        ResponseEntity<UserDto> response = authController.getCurrentUser(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("test@smartwater.com", response.getBody().getEmail());
    }
}
