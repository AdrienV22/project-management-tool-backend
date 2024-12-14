package com.example.project_management_tool.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class TaskModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 5, max = 50, message = "Project Title can't exceed size limit (5-50 characters)")
    private String title;

    @Size(min = 5, max =250, message = "Project Title can't exceed size limit (5-250 characters)")
    private String description;

    private LocalDate dueDate;

    @NotNull
    @Pattern(regexp = "En cours|Terminé|En attente", message = "Status must be one of: En cours, Terminé, En attente")
    private String status; // Exemple : "En cours", "Terminé", "En attente"
}