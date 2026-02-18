package com.example.project_management_tool.controller;

import com.example.project_management_tool.model.ProjectModel;
import com.example.project_management_tool.model.TaskModel;
import com.example.project_management_tool.repository.ProjectRepository;
import com.example.project_management_tool.repository.TaskRepository;
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
    private final TaskHistoryService taskHistoryService;

    public TaskController(TaskRepository taskRepository,
                          ProjectRepository projectRepository,
                          TaskHistoryService taskHistoryService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.taskHistoryService = taskHistoryService;
    }

    /**
     * US11 - Visualiser les tâches selon les statuts (dashboard)
     * + filtre optionnel par projectId
     *
     * Exemples :
     *  GET /api/tasks
     *  GET /api/tasks?status=En%20attente
     *  GET /api/tasks?projectId=5
     *  GET /api/tasks?projectId=5&status=En%20attente
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

        // rattacher au Project managé JPA
        task.setProject(managedProject);

        TaskModel saved = taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * US8 - Mettre à jour une tâche (changer n’importe quelle info)
     * + US12 - Historiser les modifications
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<?> updateTask(@PathVariable Long taskId,
                                        @Valid @RequestBody TaskModel updatedTask) {

        TaskModel existing = taskRepository.findById(taskId).orElse(null);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        // Pas de sécurité => on met une valeur simple, constante (acceptable pour l'étude de cas)
        String modifiedBy = "API";

        // 1) Historique AVANT modification
        taskHistoryService.recordChange(existing, "title", existing.getTitle(), updatedTask.getTitle(), modifiedBy);
        taskHistoryService.recordChange(existing, "description", existing.getDescription(), updatedTask.getDescription(), modifiedBy);
        taskHistoryService.recordChange(existing, "dueDate", existing.getDueDate(), updatedTask.getDueDate(), modifiedBy);
        taskHistoryService.recordChange(existing, "status", existing.getStatus(), updatedTask.getStatus(), modifiedBy);
        taskHistoryService.recordChange(existing, "priority", existing.getPriority(), updatedTask.getPriority(), modifiedBy);
        taskHistoryService.recordChange(existing, "targetUserId", existing.getTargetUserId(), updatedTask.getTargetUserId(), modifiedBy);

        // 2) Optionnel : changement de projet (si fourni)
        if (updatedTask.getProject() != null && updatedTask.getProject().getId() != null) {
            Long newProjectId = updatedTask.getProject().getId();
            Long oldProjectId = (existing.getProject() != null) ? existing.getProject().getId() : null;

            if (oldProjectId == null || !oldProjectId.equals(newProjectId)) {
                ProjectModel managedProject = projectRepository.findById(newProjectId).orElse(null);
                if (managedProject == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("project not found");
                }

                taskHistoryService.recordChange(existing, "projectId", oldProjectId, newProjectId, modifiedBy);
                existing.setProject(managedProject);
            }
        }

        // 3) Update effectif
        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        existing.setDueDate(updatedTask.getDueDate());
        existing.setStatus(updatedTask.getStatus());
        existing.setPriority(updatedTask.getPriority());
        existing.setTargetUserId(updatedTask.getTargetUserId());

        return ResponseEntity.ok(taskRepository.save(existing));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            return ResponseEntity.notFound().build();
        }
        taskRepository.deleteById(taskId);
        return ResponseEntity.noContent().build(); // 204
    }
}
