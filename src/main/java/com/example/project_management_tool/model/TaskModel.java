package com.example.project_management_tool.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity(name = "TaskModel")
public class TaskModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 5, max = 50, message = "Le titre doit contenir entre 5 et 50 caractères")
    private String title;

    @Size(min = 5, max = 250, message = "La description doit contenir entre 5 et 250 caractères")
    private String description;

    private LocalDate dueDate;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectModel parentProject;

    @NotNull
    @Pattern(regexp = "En cours|Terminé|En attente", message = "Le statut doit être : En cours, Terminé ou En attente")
    private String status;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @NotNull
    private Long targetUserId;

    public TaskModel() {

    }

    public TaskModel(String title, String description, LocalDate dueDate, ProjectModel parentProject,
                     String status, Priority priority, Long targetUserId) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.parentProject = parentProject;
        this.status = status;
        this.priority = priority;
        this.targetUserId = targetUserId;
    }

    public enum Priority {
        BASSE,
        MOYENNE,
        HAUTE
    }
}
