package com.example.project_management_tool.model;

import com.example.project_management_tool.entity.ProjectMember;
import com.example.project_management_tool.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project_model")
@Data
public class ProjectModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Project name cannot be null")
    @Size(min = 1, max = 100, message = "Project name must respect size limit (1-100 characters)")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Description must respect size limit (500 characters)")
    @Column(length = 500)
    private String description;


    @ElementCollection
    @CollectionTable(name = "project_admin_ids", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "admin_id")
    private List<Long> adminId = new ArrayList<>();

    @NotNull(message = "Start date cannot be null")
    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;


    @ManyToMany
    @JoinTable(
            name = "project_users",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> userList = new ArrayList<>();


    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ProjectMember> members = new ArrayList<>();

    /**
     * RELATION CORRECTE AVEC TaskModel
     * On ignore taskList côté JSON pour éviter la boucle infinie :
     * Project -> taskList -> Task -> project -> taskList -> ...
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<TaskModel> taskList = new ArrayList<>();

    @NotNull(message = "Chef de projet (email) ne peut pas être null")
    @Size(max = 255, message = "Email du chef de projet trop long")
    @Column(nullable = false, length = 255)
    private String clientEmail;

    @Column(nullable = false, length = 30)
    private String statut = "Non défini";

    public ProjectModel() {}

    public ProjectModel(String name, String description, LocalDate startDate, String statut, LocalDate endDate, String clientEmail) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.statut = statut;
        this.endDate = endDate;
        this.clientEmail = clientEmail;
    }

    public ProjectModel(String name, String description, LocalDate startDate) {
        this(name, description, startDate, "Non défini", null, null);
    }
}
