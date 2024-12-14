package com.example.project_management_tool.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Table(name = "users")  // Vérifie bien que la table s'appelle "users"
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // ID auto-généré par la base de données

    @NotNull
    @Size(min = 5, max = 25, message = "User username can't exceed size limit (5-25 characters)")
    private String username;  // Vérifie que ce champ correspond à la colonne de la table

    @Size(min = 5, max = 50, message = "User email can't exceed size limit (5-50 characters)")
    private String email;     // Vérifie que ce champ correspond à la colonne de la table

    @NotNull
    @Size(min = 8, max = 50, message = "User password can't exceed size limit (8-50 characters)")
    private String password;  // Vérifie que ce champ correspond à la colonne de la table
}