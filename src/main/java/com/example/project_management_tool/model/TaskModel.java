package com.example.project_management_tool.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity(name = "TaskModel")
@Data
public class TaskModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 5, max = 50, message = "Project Title can't exceed size limit (5-50 characters)")
    private String title;

    @Size(min = 5, max = 250, message = "Project Title can't exceed size limit (5-250 characters)")
    private String description;

    private LocalDate dueDate;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectModel parentProject;

    @NotNull
    @Pattern(regexp = "En cours|Terminé|En attente", message = "Status must be one of: En cours, Terminé, En attente")
    private String status; // Exemple : "En cours", "Terminé", "En attente"

    @NotNull
    // Si la priorité est 1, alors la priorité est HAUTE, si elle est de 2, alors MOYENNE, si elle est de 3, alors BASSE
    private Priority priority;

    // Nouvelle propriété pour l'utilisateur cible
    @NotNull
    private Long targetUserId;

    public TaskModel() {}

    // Constructeur avec targetUserId
    public TaskModel(String title, String description, LocalDate dueDate, ProjectModel parentProject, String status, Priority priority, Long targetUserId) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.parentProject = parentProject;
        this.status = status;
        this.priority = priority;
        this.targetUserId = targetUserId;
    }

    // Nouveau constructeur sans targetUserId
    public TaskModel(String title, String description, LocalDate dueDate, ProjectModel parentProject, String status, Priority priority) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.parentProject = parentProject;
        this.status = status;
        this.priority = priority;
        this.targetUserId = null;
    }

    public enum Priority {
        BASSE,
        MOYENNE,
        HAUTE
    }
}
