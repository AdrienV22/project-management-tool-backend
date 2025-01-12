package com.example.project_management_tool.entity;

import com.example.project_management_tool.model.ProjectModel;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
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
    @Enumerated(EnumType.ORDINAL)  // Utilisation de l'ordinal (int) pour le rôle (0 = ADMIN, 1 = MEMBRE, etc.)
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

        @JsonValue  // Utilise l'ordinal pour la sérialisation (envoi des entiers)
        public int getValue() {
            return value;
        }

        @JsonCreator  // Permet à Jackson de désérialiser l'ordinal lors de la réception des données
        public static UserRole forValue(int value) {
            for (UserRole role : UserRole.values()) {
                if (role.getValue() == value) {
                    return role;
                }
            }
            throw new IllegalArgumentException("Rôle non valide");
        }
    }
}
