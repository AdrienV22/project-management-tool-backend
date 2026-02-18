package com.example.project_management_tool.controller;

import com.example.project_management_tool.entity.User;
import com.example.project_management_tool.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> user) {
        String username = (String) user.get("username");
        String email = (String) user.get("email");
        String password = (String) user.get("password");

        // ✅ validations basiques -> 400 propre
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body("username is required");
        }
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("email is required");
        }
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body("password is required");
        }

        // ✅ userRole optionnel (0/1/2). Défaut = MEMBRE
        User.UserRole role = User.UserRole.MEMBRE;

        Object roleObj = user.get("userRole");
        if (roleObj != null) {
            Integer roleInt = null;

            // Swagger / Jackson peuvent te donner Integer, String, etc.
            if (roleObj instanceof Integer i) {
                roleInt = i;
            } else if (roleObj instanceof Number n) {
                roleInt = n.intValue();
            } else if (roleObj instanceof String s && !s.isBlank()) {
                try {
                    roleInt = Integer.parseInt(s.trim());
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body("userRole must be an integer (0=ADMIN, 1=MEMBRE, 2=OBSERVATEUR)");
                }
            } else {
                return ResponseEntity.badRequest().body("userRole must be an integer (0=ADMIN, 1=MEMBRE, 2=OBSERVATEUR)");
            }

            try {
                role = User.UserRole.forValue(roleInt);
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body(ex.getMessage());
            }
        }

        return authService.register(username, email, password, role);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("email is required");
        }
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body("password is required");
        }

        return authService.login(email, password);
    }
}
