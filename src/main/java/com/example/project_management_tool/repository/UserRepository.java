package com.example.project_management_tool.repository;

import com.example.project_management_tool.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username); // Vérifie si un utilisateur existe avec ce nom d'utilisateur
    boolean existsByEmail(String email);       // Vérifie si un utilisateur existe avec cet email
    Optional<User> findByEmail(String email);  // Trouve un utilisateur par son email
}
