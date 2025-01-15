package com.example.project_management_tool.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.example.project_management_tool.entity.User;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
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
    private List<Long> adminId = new ArrayList<>();

    @NotNull(message = "Start date cannot be null")
    private LocalDate startDate;

    @ManyToMany
    private List<User> userList = new ArrayList<>();

    @OneToMany
    private List<TaskModel> taskList = new ArrayList<>();

    @Transient
    private String client;

    @Transient
    private String statut = "Non défini";

    public ProjectModel() {}

    public ProjectModel(String name, String description, LocalDate startDate, String statut) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.statut = statut;
    }

    public ProjectModel(String name, String description, LocalDate startDate) {
        this(name, description, startDate, "Non défini");
    }
}
