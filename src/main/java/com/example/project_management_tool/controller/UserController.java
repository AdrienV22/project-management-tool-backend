package com.example.project_management_tool.controller;

import com.example.project_management_tool.entity.User;
import com.example.project_management_tool.model.TaskModel;
import com.example.project_management_tool.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")  // Permet au frontend de se connecter depuis ce port
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;



    @PostMapping
    public User createUser(@RequestBody User user) {
        System.out.println("Données reçues dans createUser : " + user);
        return userRepository.save(user);
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

    @PostMapping("/users/{userId}/tasks")
    public User addTask(
            @PathVariable Long userId,
            @Valid @RequestBody TaskModel task
    ) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (!task.getParentProject().getUserList().contains(user) || user.getUserRole().equals(User.UserRole.OBSERVATEUR)) {
            throw new RuntimeException("L'utilisateur n'est pas autorisé à ajouter cette tâche");
        }

        User target = userRepository.findById(task.getTargetUserId()).orElseThrow(() -> new RuntimeException("Target user not found"));
        target.getProjectList().add(task.getParentProject());
        target.getTasks().add(task.getId());

        return userRepository.save(target);
    }

}