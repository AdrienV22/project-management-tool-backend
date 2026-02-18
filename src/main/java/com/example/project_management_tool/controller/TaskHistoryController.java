package com.example.project_management_tool.controller;

import com.example.project_management_tool.dto.TaskHistoryResponse;
import com.example.project_management_tool.entity.TaskHistory;
import com.example.project_management_tool.repository.TaskHistoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
public class TaskHistoryController {

    private final TaskHistoryRepository taskHistoryRepository;

    public TaskHistoryController(TaskHistoryRepository taskHistoryRepository) {
        this.taskHistoryRepository = taskHistoryRepository;
    }

    @GetMapping("/tasks/{taskId}/history")
    public ResponseEntity<List<TaskHistoryResponse>> getTaskHistory(@PathVariable Long taskId) {

        List<TaskHistory> history = taskHistoryRepository.findByTask_IdOrderByModifiedAtDesc(taskId);

        if (history.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }

        List<TaskHistoryResponse> response = history.stream()
                .map(h -> new TaskHistoryResponse(
                        h.getId(),
                        h.getTask() != null ? h.getTask().getId() : taskId,
                        h.getModifiedBy(),
                        h.getModifiedAt(),
                        h.getFieldName(),
                        h.getOldValue(),
                        h.getNewValue()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }
}
