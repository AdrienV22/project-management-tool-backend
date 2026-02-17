package com.example.project_management_tool.service;

import com.example.project_management_tool.config.JwtService;
import com.example.project_management_tool.entity.User;
import com.example.project_management_tool.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_shouldReturn400_whenUsernameAlreadyExists() {
        when(userRepository.existsByUsername("john")).thenReturn(true);

        ResponseEntity<?> response =
                authService.register("john", "john@test.com", "pwd", User.UserRole.ADMIN);

        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void register_shouldReturn400_whenEmailAlreadyExists() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(true);

        ResponseEntity<?> response =
                authService.register("john", "john@test.com", "pwd", User.UserRole.ADMIN);

        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void login_shouldReturn401_whenUserNotFound() {
        when(userRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response =
                authService.login("john@test.com", "pwd");

        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void login_shouldReturn401_whenPasswordIncorrect() {
        User user = new User();
        user.setEmail("john@test.com");
        user.setPassword("encoded");

        when(userRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("pwd", "encoded"))
                .thenReturn(false);

        ResponseEntity<?> response =
                authService.login("john@test.com", "pwd");

        assertEquals(401, response.getStatusCodeValue());
    }
}
