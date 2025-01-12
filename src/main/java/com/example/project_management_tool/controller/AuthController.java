package com.example.project_management_tool.controller;

import com.example.project_management_tool.entity.User;
import com.example.project_management_tool.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")  // Permet au frontend de se connecter depuis ce port
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

        // Conversion du rôle en UserRole à partir de l'entier
        Integer roleInt = (Integer) user.get("userRole");
        User.UserRole role = User.UserRole.forValue(roleInt); // Utilisation de la méthode pourValue pour convertir l'entier en UserRole

        return authService.register(username, email, password, role);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        return authService.login(email, password);
    }
}
