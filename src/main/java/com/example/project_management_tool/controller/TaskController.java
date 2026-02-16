package com.example.project_management_tool.controller;

import com.example.project_management_tool.entity.User;
import com.example.project_management_tool.model.ProjectModel;
import com.example.project_management_tool.model.TaskModel;
import com.example.project_management_tool.repository.ProjectRepository;
import com.example.project_management_tool.repository.TaskRepository;
import com.example.project_management_tool.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskController(TaskRepository taskRepository,
                          ProjectRepository projectRepository,
                          UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    // Tous authentifiés
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<TaskModel> getAllTasks() {
        return taskRepository.findAll();
    }

    // Création tâche → ADMIN ou MEMBER
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public TaskModel createTask(@RequestBody TaskModel task,
                                Authentication authentication) {

        String email = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return null;

        if (task.getProject() == null || task.getProject().getId() == null)
            return null;

        ProjectModel managedProject = projectRepository
                .findById(task.getProject().getId())
                .orElse(null);

        if (managedProject == null) return null;

        task.setProject(managedProject);
        return taskRepository.save(task);
    }

    // Update → ADMIN ou MEMBER
    @PutMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public TaskModel updateTask(@PathVariable Long taskId,
                                @RequestBody TaskModel updatedTask) {

        TaskModel existing = taskRepository.findById(taskId).orElse(null);
        if (existing == null) return null;

        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        existing.setDueDate(updatedTask.getDueDate());
        existing.setStatus(updatedTask.getStatus());
        existing.setPriority(updatedTask.getPriority());

        return taskRepository.save(existing);
    }

    // Delete → ADMIN uniquement
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteTask(@PathVariable Long taskId) {
        taskRepository.deleteById(taskId);
    }
}
