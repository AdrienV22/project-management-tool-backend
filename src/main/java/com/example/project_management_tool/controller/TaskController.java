package com.example.project_management_tool.controller;

import com.example.project_management_tool.model.ProjectModel;
import com.example.project_management_tool.model.TaskModel;
import com.example.project_management_tool.repository.ProjectRepository;
import com.example.project_management_tool.repository.TaskRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.project_management_tool.entity.User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    public TaskModel createTask(@Valid ProjectModel project,  @RequestParam User user, @RequestParam TaskModel task) {
        if (project.getId() == null) {
            return null;
        }
        if ((user.getUserRole().equals(User.UserRole.ADMIN) && project.getAdminId().contains(user.getId())
                || (user.getUserRole().equals(User.UserRole.MEMBRE) && project.getUserList().contains(user))))
        {
            project.getTaskList().add(task);
        }
        return taskRepository.save(task);
    }

    public TaskModel InitiateTask(String name, String description, LocalDate date, String status, TaskModel.Priority priority,
                                  @RequestParam User user, @Valid ProjectModel project) {
        if (project.getId() == null) {
            return null;
        }
        TaskModel task = new TaskModel(name, description, date, project, status, priority);
        return createTask(project, user, task);
    }
}
