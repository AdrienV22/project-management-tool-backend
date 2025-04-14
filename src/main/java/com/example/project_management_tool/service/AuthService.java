package com.example.project_management_tool.service;

import com.example.project_management_tool.entity.User;
import com.example.project_management_tool.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Inscription
    public ResponseEntity<?> register(String username, String email, String password, User.UserRole userRole) {
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(createBasicResponse("Ce nom d'utilisateur est déjà utilisé.", "error"));
        }

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(createBasicResponse("Cet email est déjà utilisé.", "error"));
        }

        String hashedPassword = passwordEncoder.encode(password);
        User newUser = new User(username, email, hashedPassword, userRole);
        userRepository.save(newUser);

        return ResponseEntity.ok(createBasicResponse("Utilisateur inscrit avec succès !", "success"));
    }

    // Connexion
    public ResponseEntity<?> login(String email, String password) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body(createBasicResponse("Utilisateur introuvable.", "error"));
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.badRequest().body(createBasicResponse("Mot de passe incorrect.", "error"));
        }

        // ✅ Réponse complète avec ID, username et email
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Connexion réussie !");
        response.put("status", "success");
        response.put("userId", user.getId());         // requis par le frontend
        response.put("username", user.getUsername()); // facultatif mais utile
        response.put("email", user.getEmail());

        return ResponseEntity.ok(response);
    }

    // Pour les messages d'erreur ou simples réponses
    private Map<String, String> createBasicResponse(String message, String status) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        response.put("status", status);
        return response;
    }
}
