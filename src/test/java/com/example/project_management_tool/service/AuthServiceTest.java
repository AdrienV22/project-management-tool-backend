package com.example.project_management_tool.service;

import com.example.project_management_tool.entity.User;
import com.example.project_management_tool.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authService = new AuthService(userRepository, passwordEncoder);
    }

    @Test
    void register_shouldReturn400_whenMissingFields() {
        ResponseEntity<?> resp = authService.register("", "a@b.com", "pwd", User.UserRole.ADMIN);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void register_shouldReturn400_whenUsernameAlreadyExists() {
        when(userRepository.existsByUsername("john")).thenReturn(true);

        ResponseEntity<?> resp = authService.register("john", "john@x.com", "pwd", User.UserRole.MEMBRE);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        verify(userRepository).existsByUsername("john");
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldReturn201_andSaveUser_whenOk() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@x.com")).thenReturn(false);
        when(passwordEncoder.encode("pwd")).thenReturn("HASH");

        User saved = new User("john", "john@x.com", "HASH", User.UserRole.ADMIN);
        saved.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        ResponseEntity<?> resp = authService.register("john", "john@x.com", "pwd", User.UserRole.ADMIN);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("HASH", captor.getValue().getPassword());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) resp.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertEquals(1L, body.get("userId"));
        assertEquals("ADMIN", body.get("role"));
        assertFalse(body.containsKey("token"));
    }

    @Test
    void login_shouldReturn401_whenUserNotFound() {
        when(userRepository.findByEmail("x@x.com")).thenReturn(Optional.empty());

        ResponseEntity<?> resp = authService.login("x@x.com", "pwd");

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    void login_shouldReturn401_whenPasswordInvalid() {
        User user = new User("john", "john@x.com", "HASH", User.UserRole.MEMBRE);
        when(userRepository.findByEmail("john@x.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pwd", "HASH")).thenReturn(false);

        ResponseEntity<?> resp = authService.login("john@x.com", "pwd");

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    void login_shouldReturn200_whenOk() {
        User user = new User("john", "john@x.com", "HASH", User.UserRole.OBSERVATEUR);
        user.setId(7L);
        when(userRepository.findByEmail("john@x.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pwd", "HASH")).thenReturn(true);

        ResponseEntity<?> resp = authService.login("john@x.com", "pwd");

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) resp.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertEquals(7L, body.get("userId"));
        assertEquals("OBSERVATEUR", body.get("role"));
        assertFalse(body.containsKey("token"));
    }
}
