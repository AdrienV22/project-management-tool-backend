package com.example.project_management_tool.service;

import com.example.project_management_tool.entity.TaskHistory;
import com.example.project_management_tool.model.TaskModel;
import com.example.project_management_tool.repository.TaskHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class TaskHistoryService {

    private final TaskHistoryRepository taskHistoryRepository;

    public TaskHistoryService(TaskHistoryRepository taskHistoryRepository) {
        this.taskHistoryRepository = taskHistoryRepository;
    }

    public void recordChange(TaskModel task, String fieldName, Object oldValue, Object newValue, String modifiedBy) {
        String oldStr = (oldValue == null) ? null : String.valueOf(oldValue);
        String newStr = (newValue == null) ? null : String.valueOf(newValue);

        // Si identique -> on ne pollue pas l'historique
        if (Objects.equals(oldStr, newStr)) return;

        TaskHistory history = new TaskHistory();
        history.setTask(task);
        history.setFieldName(fieldName);
        history.setOldValue(oldStr);
        history.setNewValue(newStr);
        history.setModifiedBy((modifiedBy == null || modifiedBy.isBlank()) ? "SYSTEM" : modifiedBy);
        history.setModifiedAt(LocalDateTime.now());

        taskHistoryRepository.save(history);
    }
}
