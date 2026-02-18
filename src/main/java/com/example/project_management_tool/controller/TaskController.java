package com.example.project_management_tool.controller;

import com.example.project_management_tool.model.ProjectModel;
import com.example.project_management_tool.model.TaskModel;
import com.example.project_management_tool.repository.ProjectRepository;
import com.example.project_management_tool.repository.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public TaskController(TaskRepository taskRepository,
                          ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    @GetMapping
    public ResponseEntity<List<TaskModel>> getAllTasks() {
        return ResponseEntity.ok(taskRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskModel task) {
        if (task.getProject() == null || task.getProject().getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("project.id is required");
        }

        ProjectModel managedProject = projectRepository.findById(task.getProject().getId()).orElse(null);
        if (managedProject == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("project not found");
        }

        task.setProject(managedProject);
        TaskModel saved = taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskModel> updateTask(@PathVariable Long taskId,
                                                @RequestBody TaskModel updatedTask) {

        TaskModel existing = taskRepository.findById(taskId).orElse(null);
        if (existing == null) return ResponseEntity.notFound().build();

        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        existing.setDueDate(updatedTask.getDueDate());
        existing.setStatus(updatedTask.getStatus());
        existing.setPriority(updatedTask.getPriority());

        return ResponseEntity.ok(taskRepository.save(existing));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            return ResponseEntity.notFound().build();
        }
        taskRepository.deleteById(taskId);
        return ResponseEntity.noContent().build();
    }
}
