package com.example.project_management_tool.repository;

import com.example.project_management_tool.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
