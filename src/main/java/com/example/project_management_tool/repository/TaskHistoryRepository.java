package com.example.project_management_tool.repository;

import com.example.project_management_tool.entity.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {
}