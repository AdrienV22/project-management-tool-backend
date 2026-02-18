package com.example.project_management_tool.service;

import com.example.project_management_tool.entity.User;
import com.example.project_management_tool.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Inscription
    public ResponseEntity<?> register(String username, String email, String password, User.UserRole userRole) {

        if (username == null || username.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()
                || userRole == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createBasicResponse("Champs requis manquants.", "error"));
        }

        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createBasicResponse("Ce nom d'utilisateur est déjà utilisé.", "error"));
        }

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createBasicResponse("Cet email est déjà utilisé.", "error"));
        }

        String hashedPassword = passwordEncoder.encode(password);
        User newUser = new User(username, email, hashedPassword, userRole);
        User saved = userRepository.save(newUser);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Utilisateur inscrit avec succès !");
        response.put("status", "success");
        response.put("userId", saved.getId());
        response.put("username", saved.getUsername());
        response.put("email", saved.getEmail());
        response.put("role", saved.getUserRole().name());

        // Sécurité non requise par la consigne : pas de JWT ici.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Connexion
    public ResponseEntity<?> login(String email, String password) {

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createBasicResponse("Email et mot de passe requis.", "error"));
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createBasicResponse("Identifiants invalides.", "error"));
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createBasicResponse("Identifiants invalides.", "error"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Connexion réussie !");
        response.put("status", "success");
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("role", user.getUserRole().name());

        // Sécurité non requise par la consigne : pas de JWT ici.
        return ResponseEntity.ok(response);
    }

    private Map<String, String> createBasicResponse(String message, String status) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        response.put("status", status);
        return response;
    }
}
