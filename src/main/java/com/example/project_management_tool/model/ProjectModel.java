package com.example.project_management_tool.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;
import java.time.LocalDate;

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

    @NotNull(message = "Start date cannot be null")
    private LocalDate startDate;
}
