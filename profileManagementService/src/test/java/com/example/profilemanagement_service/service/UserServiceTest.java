package com.example.profilemanagement_service.service;

import com.example.profilemanagement_service.dto.UserDTO;
import com.example.profilemanagement_service.model.Role;
import com.example.profilemanagement_service.model.User;
import com.example.profilemanagement_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    private User mockUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("Riya");
        mockUser.setPassword("testpassword");
        mockUser.setRole(Role.USER);
    }
    @Test
    void loadUserByUsernameSuccess() {
        when(userRepository.findByUsername("Riya")).thenReturn(Optional.of(mockUser));

        UserDetails userDetails = userService.loadUserByUsername("Riya");

        assertEquals("Riya", userDetails.getUsername());
        assertEquals("testpassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsernameUserNotFound() {
        when(userRepository.findByUsername("invalid")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("invalid");
        });
    }

    @Test
    void registerUserSuccess() {
        when(userRepository.findByUsername("Riya")).thenReturn(Optional.empty());
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        User savedUser = userService.registerUser(mockUser);

        assertNotNull(savedUser);
        assertEquals("Riya", savedUser.getUsername());
    }

    @Test
    void registerUserUsernameExists() {
        when(userRepository.findByUsername("Riya")).thenReturn(Optional.of(mockUser));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(mockUser);
        });

        assertEquals("Username already exists", ex.getMessage());
    }

    @Test
    void getAllUsers() {
        List<User> userList = List.of(mockUser);
        when(userRepository.findAll()).thenReturn(userList);

        List<User> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("Riya", result.get(0).getUsername());
    }

    @Test
    void getUsersByRole() {
        List<User> userList = List.of(mockUser);
        when(userRepository.findByRole(Role.USER)).thenReturn(userList);

        List<User> result = userService.getUsersByRole(Role.USER);

        assertEquals(1, result.size());
        assertEquals(Role.USER, result.get(0).getRole());
    }

    @Test
    void getUserByUserId() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        UserDTO dto = userService.getUserByUserId(1L);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Riya", dto.getUsername());
    }
}