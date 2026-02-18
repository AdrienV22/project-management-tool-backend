package com.example.project_management_tool.controller;

import com.example.project_management_tool.entity.User;
import com.example.project_management_tool.model.ProjectModel;
import com.example.project_management_tool.model.TaskModel;
import com.example.project_management_tool.repository.ProjectRepository;
import com.example.project_management_tool.repository.TaskRepository;
import com.example.project_management_tool.repository.UserRepository;
import com.example.project_management_tool.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public TaskController(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            EmailService emailService
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @GetMapping
    public ResponseEntity<List<TaskModel>> getTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long projectId
    ) {
        if (status != null && projectId != null) {
            return ResponseEntity.ok(taskRepository.findByProjectIdAndStatus(projectId, status));
        }
        if (status != null) {
            return ResponseEntity.ok(taskRepository.findByStatus(status));
        }
        if (projectId != null) {
            return ResponseEntity.ok(taskRepository.findByProjectId(projectId));
        }
        return ResponseEntity.ok(taskRepository.findAll());
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskModel> getTaskById(@PathVariable Long taskId) {
        return taskRepository.findById(taskId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createTask(@Valid @RequestBody TaskModel task) {

        if (task.getProject() == null || task.getProject().getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("project.id is required");
        }

        ProjectModel managedProject = projectRepository.findById(task.getProject().getId()).orElse(null);
        if (managedProject == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("project not found");
        }

        task.setProject(managedProject);

        TaskModel saved = taskRepository.save(task);

        // 📧 Notification à l’assignation (si targetUserId présent)
        if (saved.getTargetUserId() != null) {
            userRepository.findById(saved.getTargetUserId()).ifPresent(user -> {
                emailService.sendTaskAssignedEmail(
                        user.getEmail(),
                        managedProject.getName(),
                        saved.getTitle()
                );
            });
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<?> updateTask(@PathVariable Long taskId,
                                        @Valid @RequestBody TaskModel updatedTask) {

        TaskModel existing = taskRepository.findById(taskId).orElse(null);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        Long oldTargetUserId = existing.getTargetUserId();

        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        existing.setDueDate(updatedTask.getDueDate());
        existing.setStatus(updatedTask.getStatus());
        existing.setPriority(updatedTask.getPriority());
        existing.setTargetUserId(updatedTask.getTargetUserId());

        if (updatedTask.getProject() != null && updatedTask.getProject().getId() != null) {
            ProjectModel managedProject = projectRepository.findById(updatedTask.getProject().getId()).orElse(null);
            if (managedProject == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("project not found");
            }
            existing.setProject(managedProject);
        }

        TaskModel saved = taskRepository.save(existing);

        // 📧 Notification si assignation/re-assignation (targetUserId change)
        Long newTargetUserId = saved.getTargetUserId();
        boolean changed = (oldTargetUserId == null && newTargetUserId != null)
                || (oldTargetUserId != null && !oldTargetUserId.equals(newTargetUserId));

        if (changed && newTargetUserId != null) {
            userRepository.findById(newTargetUserId).ifPresent(user -> {
                emailService.sendTaskAssignedEmail(
                        user.getEmail(),
                        saved.getProject().getName(),
                        saved.getTitle()
                );
            });
        }

        return ResponseEntity.ok(saved);
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
