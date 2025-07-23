package com.example.profilemanagement_service.controller;

import com.example.profilemanagement_service.dto.LoginDTO;
import com.example.profilemanagement_service.dto.RegisterDTO;
import com.example.profilemanagement_service.dto.ResponseDTO;
import com.example.profilemanagement_service.model.Role;
import com.example.profilemanagement_service.model.User;
import com.example.profilemanagement_service.repository.UserRepository;
import com.example.profilemanagement_service.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    private RegisterDTO registerDTO;
    private LoginDTO loginDTO;
    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        registerDTO = new RegisterDTO();
        registerDTO.setUsername("Riya");
        registerDTO.setName("Riya Saxena");
        registerDTO.setEmail("Riya@gmail.com");
        registerDTO.setPassword("testpassword");
        registerDTO.setRole(Role.USER);

        loginDTO = new LoginDTO();
        loginDTO.setUsername("Riya");
        loginDTO.setPassword("testpassword");

        mockUser = new User(1L, "Riya", "Riya Saxena", "Riya@gmail.com", "encodedPass", Role.USER);
    }

    @Test
    void registerUsernameAlreadyExists() {
        when(userRepository.findByUsername("Riya")).thenReturn(Optional.of(mockUser));

        ResponseEntity<?> response = authController.register(registerDTO);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Username already registered", response.getBody());
    }

    @Test
    void registerSuccess() {
        when(userRepository.findByUsername("Riya")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        ResponseEntity<?> response = authController.register(registerDTO);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User registered successfully", response.getBody());
    }

    @Test
    void loginSuccess() {
        when(userRepository.findByUsername("Riya")).thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateToken("Riya", Role.USER, 1L)).thenReturn("mock-jwt-token");

        ResponseEntity<?> response = authController.login(loginDTO);

        assertEquals(200, response.getStatusCodeValue());

        ResponseDTO body = (ResponseDTO) response.getBody();
        assertEquals("mock-jwt-token", body.getToken());
        assertEquals("USER", body.getRole());
    }

    @Test
    void loginInvalidCredentials() {
        doThrow(new AuthenticationServiceException("Invalid")).when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        ResponseEntity<?> response = authController.login(loginDTO);

        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Invalid Credentials", response.getBody());
    }
}