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

    // Inscription d'un utilisateur
    public ResponseEntity<?> register(String username, String email, String password, User.UserRole userRole) {
        // Vérification si le nom d'utilisateur existe déjà
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(createResponse("Cet nom d'utilisateur est déjà utilisé", "error"));
        }

        // Vérification si l'email existe déjà
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(createResponse("Cet email est déjà utilisé", "error"));
        }

        // Création de l'utilisateur
        User newUser = new User(username, email, passwordEncoder.encode(password), userRole);
        userRepository.save(newUser);

        // Réponse avec message de succès
        return ResponseEntity.ok(createResponse("Utilisateur inscrit avec succès!", "success"));
    }

    // Connexion d'un utilisateur
    public ResponseEntity<?> login(String email, String password) {
        User user = userRepository.findByEmail(email).orElse(null);

        // Vérification des identifiants
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.badRequest().body(createResponse("Email ou mot de passe incorrect!", "error"));
        }

        // Réponse avec message de succès
        return ResponseEntity.ok(createResponse("Utilisateur connecté avec succès!", "success"));
    }

    // Fonction utilitaire pour créer une réponse structurée
    private Map<String, String> createResponse(String message, String status) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        response.put("status", status);
        return response;
    }
}
