package com.example.project_management_tool.controller;

import com.example.project_management_tool.entity.User;
import com.example.project_management_tool.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;



    @PostMapping
    public User createUser(@RequestBody User user) {
        // Log détaillé pour vérifier ce qui est reçu
        System.out.println("Données reçues dans createUser : " + user);

        // Si l'objet est bien reçu, on va l'enregistrer dans la base de données
        return userRepository.save(user);  // Sauvegarde l'utilisateur dans la base de données
    }
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.internalServerError().body("An error occurred: " + e.getMessage());
    }
}