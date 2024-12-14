package com.example.project_management_tool.controller;

import com.example.project_management_tool.model.ProjectModel;
import com.example.project_management_tool.model.TaskModel;
import com.example.project_management_tool.repository.ProjectRepository;
import com.example.project_management_tool.repository.TaskRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.project_management_tool.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ProjectRepository projectRepository;

    // Endpoint pour récupérer toutes les tâches
    @GetMapping
    public List<TaskModel> getAllTasks() {
        return taskRepository.findAll();
    }

    // Endpoint pour récupérer une tâche par son ID
    @GetMapping("/{id}")
    public TaskModel getTaskById(@PathVariable Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    public TaskModel addProject(TaskModel task, ProjectModel project) {
        task.setParentProject(project);
        return taskRepository.save(task);
    }

    // Endpoint pour créer une tâche
    @PostMapping
    public ProjectModel createTask(@Valid ProjectModel project,  @RequestParam User user, @RequestParam TaskModel task) {
        if (project.getId() == null) {
            return null;
        }
        if (project.getAdminId().contains(user.getId()))
        {
            project.getTaskList().add(task);
        }
        return projectRepository.save(project);
    }
}