package com.example.project_management_tool.repository;

import com.example.project_management_tool.entity.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {

    // Le nom le plus fiable avec relation ManyToOne : task.id
    List<TaskHistory> findByTask_IdOrderByModifiedAtDesc(Long taskId);
}
