package com.example.project_management_tool.controller;

import com.example.project_management_tool.model.Task;
import com.example.project_management_tool.repository.TaskRepository;
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
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // Endpoint pour créer une tâche
    @PostMapping
    public Task createTask(@RequestBody Task task) {
        return taskRepository.save(task);
    }

    // Endpoint pour récupérer une tâche par son ID
    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id) {
        return taskRepository.findById(id).orElse(null);
    }
}
