package com.example.project_management_tool.controller;

import com.example.project_management_tool.model.Project;
import com.example.project_management_tool.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    // Endpoint pour récupérer tous les projets
    @GetMapping
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    // Endpoint pour créer un projet
    @PostMapping
    public Project createProject(@RequestBody Project project) {
        return projectRepository.save(project);
    }
}
