package com.example.project_management_tool.repository;

import com.example.project_management_tool.model.TaskModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<TaskModel, Long> {

    // US11 - Filtrage par statut
    List<TaskModel> findByStatus(String status);

    // Filtrage par projet
    List<TaskModel> findByProjectId(Long projectId);

    // Filtrage combiné projet + statut
    List<TaskModel> findByProjectIdAndStatus(Long projectId, String status);
}
