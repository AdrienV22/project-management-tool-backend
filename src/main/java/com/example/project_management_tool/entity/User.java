package com.example.project_management_tool.entity;

import com.example.project_management_tool.converter.UserRoleConverter;
import com.example.project_management_tool.model.ProjectModel;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @Enumerated(EnumType.STRING)  // Utilise l'énumération sous forme de chaîne ("ADMIN", "MEMBRE", etc.)
    private UserRole userRole;

    @ManyToMany
    private List<ProjectModel> projectList;

    @ElementCollection
    private List<Long> tasks;

    // Constructeur par défaut
    public User() {}

    // Constructeur avec paramètres
    public User(String username, String email, String password, UserRole role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.userRole = role;
    }

    // Enum UserRole pour les rôles des utilisateurs
    @Getter
    public enum UserRole {
        ADMIN(0),
        MEMBRE(1),
        OBSERVATEUR(2);

        private final int value;

        UserRole(int value) {
            this.value = value;
        }

        // Méthode pour convertir la valeur (int) en UserRole
        @JsonCreator
        public static UserRole fromValue(int value) {
            for (UserRole role : UserRole.values()) {
                if (role.getValue() == value) {
                    return role;
                }
            }
            throw new IllegalArgumentException("Unknown role: " + value);
        }

        // Méthode pour obtenir le nom du rôle en tant que String (par exemple "ADMIN", "MEMBRE")
        @Override
        public String toString() {
            return this.name();
        }
    }
}
