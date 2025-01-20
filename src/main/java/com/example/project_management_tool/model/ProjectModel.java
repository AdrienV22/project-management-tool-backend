package com.example.project_management_tool.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.example.project_management_tool.entity.User;


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

    // Nouveau champ pour la date de fin
    private LocalDate endDate;

    @ManyToMany
    private List<User> userList = new ArrayList<>();

    @OneToMany
    private List<TaskModel> taskList = new ArrayList<>();

    // Remplacer le champ User par un email (String)
    @NotNull(message = "Chef de projet (email) ne peut pas être null")
    @Size(max = 255, message = "Email du chef de projet trop long")
    private String clientEmail;  // Email du chef de projet (anciennement 'client')

    // Nouveau champ pour le statut
    private String statut = "Non défini"; // Valeur par défaut


    // Constructeur par défaut
    public ProjectModel() {}

    // Constructeur avec statut et date de fin
    public ProjectModel(String name, String description, LocalDate startDate, String statut, LocalDate endDate, String clientEmail) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.statut = statut;
        this.endDate = endDate;
        this.clientEmail = clientEmail;
    }

    // Constructeur sans client et date de fin
    public ProjectModel(String name, String description, LocalDate startDate) {
        this(name, description, startDate, "Non défini", null, null);
    }

    // Getter et Setter pour clientEmail
    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }
}
