package com.example.project_management_tool.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Entity(name = "TaskModel")
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

    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectModel ParentProject;

    @NotNull
    @Pattern(regexp = "En cours|Terminé|En attente", message = "Status must be one of: En cours, Terminé, En attente")
    private String status; // Exemple : "En cours", "Terminé", "En attente"
}
