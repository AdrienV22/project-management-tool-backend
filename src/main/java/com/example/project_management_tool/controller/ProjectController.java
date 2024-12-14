package com.example.project_management_tool.controller;

import com.example.project_management_tool.model.ProjectModel;
import com.example.project_management_tool.model.TaskModel;
import com.example.project_management_tool.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private final ProjectRepository projectRepository;

    public ProjectController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // Endpoint pour récupérer tous les projets
    @GetMapping
    public List<ProjectModel> getAllProjects() {
        return projectRepository.findAll();
    }

    // Endpoint pour créer un projet
    @PostMapping
    public ProjectModel createProject(@RequestBody ProjectModel project) {
        return projectRepository.save(project);
    }

    // Endpoint pour récupérer le projet en fonction de l'ID
    public ProjectModel getProjectById(Long id) {
        return projectRepository.findById(id).orElse(null);
    }

    public ProjectModel addTaskById(TaskModel task, ProjectModel project) {
        project.getTaskList().add(task);
        return projectRepository.save(project);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.internalServerError().body("Une erreur est survenue: " + e.getMessage());
    }
}