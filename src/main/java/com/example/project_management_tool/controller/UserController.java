package com.example.project_management_tool.controller;

import com.example.project_management_tool.model.User;
import com.example.project_management_tool.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}
