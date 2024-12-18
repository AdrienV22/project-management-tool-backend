package com.example.project_management_tool.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.example.project_management_tool.entity.User;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "ProjectModel")
@Data
public class ProjectModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Project name cannot be null")
    @Size(min = 1, max = 100, message = "Project name must respect size limit (1-100 characters)")
    private String name;

    @Size(max = 500, message = "Description must respect size limit (500 characters)")
    private String description;

    @ElementCollection
    private List<Long> adminId;

    @NotNull(message = "Start date cannot be null")
    private LocalDate startDate;

    @ManyToMany
    private List<User> userList;

    @OneToMany
    private List<TaskModel> taskList;

    // Constructeur par défaut
    public ProjectModel() {
        this.adminId = new ArrayList<>(); // Initialisation des adminId
        this.userList = new ArrayList<>(); // Initialisation des userList
        this.taskList = new ArrayList<>(); // Initialisation des taskList
    }

    // Constructeur avec arguments
    public ProjectModel(String name, String description, LocalDate startDate) {
        this(); // Appel au constructeur par défaut pour initialiser les listes
        this.name = name;
        this.description = description;
        this.startDate = startDate;
    }
}
