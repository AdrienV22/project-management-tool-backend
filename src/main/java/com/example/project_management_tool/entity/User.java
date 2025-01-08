package com.example.project_management_tool.entity;

import com.example.project_management_tool.converter.UserRoleConverter;
import com.example.project_management_tool.model.ProjectModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.*;

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
    @Enumerated(EnumType.ORDINAL)  // Utilise l'index ordinal de l'énumération
    private UserRole userRole;

    @ManyToMany
    private List<ProjectModel> projectList;

    @ElementCollection
    private List<Long> tasks;

    // Constructeur par défaut
    public User() {}

    public User(String username, String email, String password, UserRole role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.userRole = role;
    }

    @Getter
    public enum UserRole {
        // user_role = 0
        ADMIN(0),
        // user_role = 1
        MEMBRE(1),
        // user_role = 2
        OBSERVATEUR(2);

        private final int value;

        UserRole(int value) {
            this.value = value;
        }

    }
}
