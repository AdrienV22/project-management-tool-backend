package com.example.project_management_tool.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "users")  // Vérifie bien que la table s'appelle "users"
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // ID auto-généré par la base de données

    private String username;  // Vérifie que ce champ correspond à la colonne de la table
    private String email;     // Vérifie que ce champ correspond à la colonne de la table
    private String password;  // Vérifie que ce champ correspond à la colonne de la table
}

