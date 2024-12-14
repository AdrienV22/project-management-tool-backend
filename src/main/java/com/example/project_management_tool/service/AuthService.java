package com.example.project_management_tool.service;

import com.example.project_management_tool.entity.User;
import com.example.project_management_tool.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<?> register(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body("Ce nom d'utilisateur est déjà utilisé!");
        }

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("Cette email est déjà utilisé!");
        }

        User newUser = new User(username, email, passwordEncoder.encode(password));
        userRepository.save(newUser);

        return ResponseEntity.ok("Utilisateur enregistré avec succès!");
    }

    public ResponseEntity<?> login(String email, String password) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid email or password!");
        }

        return ResponseEntity.ok("Utilisateur connecté avec succès!");
    }
}
