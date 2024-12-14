package com.example.project_management_tool.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.*;

import static java.lang.Boolean.FALSE;
@Getter
@Setter
@Entity(name = "UserEntity")
@Table(name="users")
public class User {

    // Getters et setters pour chaque propriété
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // identifiant unique pour chaque utilisateur
    @NotNull
    @Size(min = 3, max = 50)
    private String username;

    @NotNull
    private String email;

    @NotNull
    @Size(min = 5)
    private String password;

    @NotNull
    UserRole UserRole;

    @ElementCollection
    private List<Long> tasks;

    // Constructeur par défaut
    public User() {}

    public User(String username, String email, String password, UserRole role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.UserRole = role;
    }

    public enum UserRole {
        ADMIN,
        MEMBRE,
        OBSERVATEUR
    };
}