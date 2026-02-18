package com.example.project_management_tool.entity;

import com.example.project_management_tool.model.ProjectModel;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 3, max = 50)
    @Column(nullable = false, length = 50)
    private String username;

    @NotNull
    @Email
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @NotNull
    @Size(min = 5)
    @Column(nullable = false)
    private String password;

    /**
     * Stockage en STRING (ADMIN/MEMBRE/OBSERVATEUR)
     * - robuste (pas dépendant de l'ordre de l'enum)
     * - compatible H2/Postgres
     * - évite le problème TINYINT
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 20)
    private UserRole userRole;

    @ManyToMany
    @JoinTable(
            name = "user_projects",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    private List<ProjectModel> projectList = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_tasks", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "task_id")
    private List<Long> tasks = new ArrayList<>();

    public User() {}

    public User(String username, String email, String password, UserRole role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.userRole = role;
    }

    @Getter
    public enum UserRole {
        ADMIN(0),
        MEMBRE(1),
        OBSERVATEUR(2);

        private final int value;

        UserRole(int value) {
            this.value = value;
        }


        @JsonValue
        public int getValue() {
            return value;
        }


        @JsonCreator
        public static UserRole forValue(int value) {
            for (UserRole role : UserRole.values()) {
                if (role.value == value) return role;
            }
            throw new IllegalArgumentException("Rôle non valide: " + value);
        }
    }
}
