package com.example.project_management_tool.controller;

import com.example.project_management_tool.model.ProjectModel;
import com.example.project_management_tool.model.TaskModel;
import com.example.project_management_tool.repository.ProjectRepository;
import com.example.project_management_tool.repository.TaskRepository;
import com.example.project_management_tool.repository.UserRepository;
import com.example.project_management_tool.service.EmailService;
import com.example.project_management_tool.service.TaskHistoryService;
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
    private final TaskHistoryService taskHistoryService;

    public TaskController(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            EmailService emailService,
            TaskHistoryService taskHistoryService
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.taskHistoryService = taskHistoryService;
    }

    /**
     * US11 - Visualiser les tâches selon les statuts (dashboard)
     * + filtre optionnel par projectId
     */
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

    /**
     * US9 - Visualiser une tâche unitaire
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskModel> getTaskById(@PathVariable Long taskId) {
        return taskRepository.findById(taskId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * US6 - Créer une tâche pour un projet
     * + US12 (partiel) notification email à l'assignation
     */
    @PostMapping
    public ResponseEntity<?> createTask(@Valid @RequestBody TaskModel task) {

        if (task.getProject() == null || task.getProject().getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("project.id is required");
        }

        ProjectModel managedProject = projectRepository.findById(task.getProject().getId()).orElse(null);
        if (managedProject == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("project not found");
        }

        // Rattacher au Project managé JPA
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

    /**
     * US8 - Mettre à jour une tâche
     * + US13 Historique des modifications
     * + US12 (partiel) notification email si assignation/re-assignation
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<?> updateTask(@PathVariable Long taskId,
                                        @Valid @RequestBody TaskModel updatedTask) {

        TaskModel existing = taskRepository.findById(taskId).orElse(null);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        String modifiedBy = "API"; // tu peux laisser comme ça pour le livrable

        Long oldTargetUserId = existing.getTargetUserId();
        Long oldProjectId = (existing.getProject() != null) ? existing.getProject().getId() : null;

        // ✅ Historique AVANT modification
        taskHistoryService.recordChange(existing, "title", existing.getTitle(), updatedTask.getTitle(), modifiedBy);
        taskHistoryService.recordChange(existing, "description", existing.getDescription(), updatedTask.getDescription(), modifiedBy);
        taskHistoryService.recordChange(existing, "dueDate", existing.getDueDate(), updatedTask.getDueDate(), modifiedBy);
        taskHistoryService.recordChange(existing, "status", existing.getStatus(), updatedTask.getStatus(), modifiedBy);
        taskHistoryService.recordChange(existing, "priority", existing.getPriority(), updatedTask.getPriority(), modifiedBy);
        taskHistoryService.recordChange(existing, "targetUserId", existing.getTargetUserId(), updatedTask.getTargetUserId(), modifiedBy);

        // Projet (optionnel)
        if (updatedTask.getProject() != null && updatedTask.getProject().getId() != null) {
            Long newProjectId = updatedTask.getProject().getId();
            taskHistoryService.recordChange(existing, "projectId", oldProjectId, newProjectId, modifiedBy);

            ProjectModel managedProject = projectRepository.findById(newProjectId).orElse(null);
            if (managedProject == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("project not found");
            }
            existing.setProject(managedProject);
        }

        // ✅ Mise à jour
        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        existing.setDueDate(updatedTask.getDueDate());
        existing.setStatus(updatedTask.getStatus());
        existing.setPriority(updatedTask.getPriority());
        existing.setTargetUserId(updatedTask.getTargetUserId());

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
