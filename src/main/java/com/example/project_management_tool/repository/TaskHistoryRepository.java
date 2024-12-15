package com.example.project_management_tool.repository;

import com.example.project_management_tool.entity.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {
    List<TaskHistory> findByTaskId(Long taskId);
}