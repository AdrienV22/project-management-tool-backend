package com.example.project_management_tool.service;

import com.example.project_management_tool.config.JwtService;
import com.example.project_management_tool.entity.User;
import com.example.project_management_tool.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks private AuthService authService;

    private final String username = "john";
    private final String email = "john@example.com";
    private final String password = "secret";
    private final String hashed = "hashed";
    private final String token = "jwt-token";

    @BeforeEach
    void setup() {
        // nothing
    }

    // -------- register --------

    @Test
    void register_shouldReturn400_whenMissingFields() {
        ResponseEntity<?> res = authService.register("", email, password, User.UserRole.ADMIN);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertTrue(res.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) res.getBody();
        assertEquals("error", body.get("status"));
        assertEquals("Champs requis manquants.", body.get("message"));

        verifyNoInteractions(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void register_shouldReturn400_whenUsernameAlreadyExists() {
        when(userRepository.existsByUsername(username)).thenReturn(true);

        ResponseEntity<?> res = authService.register(username, email, password, User.UserRole.ADMIN);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) res.getBody();
        assertEquals("error", body.get("status"));
        assertEquals("Ce nom d'utilisateur est déjà utilisé.", body.get("message"));

        verify(userRepository).existsByUsername(username);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void register_shouldReturn400_whenEmailAlreadyExists() {
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        ResponseEntity<?> res = authService.register(username, email, password, User.UserRole.ADMIN);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) res.getBody();
        assertEquals("error", body.get("status"));
        assertEquals("Cet email est déjà utilisé.", body.get("message"));

        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByEmail(email);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void register_shouldReturn201_whenOk() {
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(hashed);

        User saved = new User(username, email, hashed, User.UserRole.ADMIN);
        // si ton entity a setId() : sinon, laisse userId non vérifié
        // saved.setId(1L);

        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtService.generateToken(email, "ADMIN")).thenReturn(token);

        ResponseEntity<?> res = authService.register(username, email, password, User.UserRole.ADMIN);

        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        assertTrue(res.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertEquals("success", body.get("status"));
        assertEquals("Utilisateur inscrit avec succès !", body.get("message"));
        assertEquals(username, body.get("username"));
        assertEquals(email, body.get("email"));
        assertEquals("ADMIN", body.get("role"));
        assertEquals(token, body.get("token"));

        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(email, "ADMIN");
    }

    // -------- login --------

    @Test
    void login_shouldReturn400_whenMissingFields() {
        ResponseEntity<?> res = authService.login(" ", "");

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) res.getBody();
        assertEquals("error", body.get("status"));
        assertEquals("Email et mot de passe requis.", body.get("message"));

        verifyNoInteractions(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void login_shouldReturn401_whenUserNotFound() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResponseEntity<?> res = authService.login(email, password);

        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) res.getBody();
        assertEquals("error", body.get("status"));
        assertEquals("Identifiants invalides.", body.get("message"));

        verify(userRepository).findByEmail(email);
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void login_shouldReturn401_whenPasswordDoesNotMatch() {
        User user = new User(username, email, hashed, User.UserRole.ADMIN);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, hashed)).thenReturn(false);

        ResponseEntity<?> res = authService.login(email, password);

        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) res.getBody();
        assertEquals("error", body.get("status"));
        assertEquals("Identifiants invalides.", body.get("message"));

        verify(passwordEncoder).matches(password, hashed);
        verifyNoInteractions(jwtService);
    }

    @Test
    void login_shouldReturn200_whenOk() {
        User user = new User(username, email, hashed, User.UserRole.ADMIN);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, hashed)).thenReturn(true);
        when(jwtService.generateToken(email, "ADMIN")).thenReturn(token);

        ResponseEntity<?> res = authService.login(email, password);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertTrue(res.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertEquals("success", body.get("status"));
        assertEquals("Connexion réussie !", body.get("message"));
        assertEquals(username, body.get("username"));
        assertEquals(email, body.get("email"));
        assertEquals("ADMIN", body.get("role"));
        assertEquals(token, body.get("token"));

        verify(jwtService).generateToken(email, "ADMIN");
    }
}
