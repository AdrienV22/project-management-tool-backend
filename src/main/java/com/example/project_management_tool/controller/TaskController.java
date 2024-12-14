package com.example.project_management_tool.controller;

import com.example.project_management_tool.model.TaskModel;
import com.example.project_management_tool.repository.TaskRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

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

    // Endpoint pour créer une tâche
    @PostMapping
    public TaskModel createTask(@Valid @RequestBody TaskModel task) {
        return taskRepository.save(task);
    }
}
