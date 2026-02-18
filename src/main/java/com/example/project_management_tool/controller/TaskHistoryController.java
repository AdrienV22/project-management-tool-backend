package com.example.project_management_tool.controller;

import com.example.project_management_tool.entity.TaskHistory;
import com.example.project_management_tool.repository.TaskHistoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/tasks")
public class TaskHistoryController {

    private final TaskHistoryRepository taskHistoryRepository;

    public TaskHistoryController(TaskHistoryRepository taskHistoryRepository) {
        this.taskHistoryRepository = taskHistoryRepository;
    }

    @GetMapping("/{taskId}/history")
    public ResponseEntity<List<TaskHistory>> getTaskHistory(@PathVariable Long taskId) {
        List<TaskHistory> history = taskHistoryRepository.findByTask_IdOrderByModifiedAtDesc(taskId);
        if (history.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(history);
    }
}
