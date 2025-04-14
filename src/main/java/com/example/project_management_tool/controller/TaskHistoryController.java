package com.example.project_management_tool.controller;

import com.example.project_management_tool.entity.TaskHistory;
import com.example.project_management_tool.model.TaskModel;
import com.example.project_management_tool.repository.TaskHistoryRepository;
import com.example.project_management_tool.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
public class TaskHistoryController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

    // Méthode pour enregistrer une modification dans l'historique
    void recordHistory(TaskModel task, String fieldName, String oldValue, String newValue, String modifiedBy) {
        TaskHistory history = new TaskHistory();
        history.setTask(task);
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setModifiedBy(modifiedBy);
        history.setModifiedAt(LocalDateTime.now());
        taskHistoryRepository.save(history);
    }

    // Endpoint pour récupérer l'historique des modifications d'une tâche
    @GetMapping("/tasks/{taskId}/history")
    public ResponseEntity<List<TaskHistory>> getTaskHistory(@PathVariable Long taskId) {
        List<TaskHistory> history = taskHistoryRepository.findByTaskId(taskId);
        if (history.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(history);
    }
}
