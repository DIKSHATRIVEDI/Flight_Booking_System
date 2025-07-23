package com.example.profilemanagement_service.controller;

import com.example.profilemanagement_service.dto.UserDTO;
import com.example.profilemanagement_service.model.Role;
import com.example.profilemanagement_service.model.User;
import com.example.profilemanagement_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class UserControllerTest {
    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    private User mockUser;
    private UserDTO mockDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("Riya");
        mockUser.setPassword("pass");
        mockUser.setRole(Role.USER);

        mockDTO = new UserDTO();
        mockDTO.setId(1L);
        mockDTO.setUsername("Riya");
    }

    @Test
    void getAllUsers() {
        when(userService.getAllUsers()).thenReturn(List.of(mockUser));

        ResponseEntity<List<User>> response = userController.getAllUsers();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("Riya", response.getBody().get(0).getUsername());
    }

    @Test
    void getUsersByRole() {
        when(userService.getUsersByRole(Role.USER)).thenReturn(List.of(mockUser));

        ResponseEntity<List<User>> response = userController.getUsersByRole(Role.USER);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Riya", response.getBody().get(0).getUsername());
    }

    @Test
    void getUserByUserIdSuccess() {
        when(userService.getUserByUserId(1L)).thenReturn(mockDTO);

        ResponseEntity<UserDTO> response = userController.getUserByUserId(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Riya", response.getBody().getUsername());
    }

    @Test
    void getUserByUserIdNotFound() {
        when(userService.getUserByUserId(99L)).thenThrow(new UsernameNotFoundException("User not found"));

        ResponseEntity<UserDTO> response = userController.getUserByUserId(99L);

        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
    }
}