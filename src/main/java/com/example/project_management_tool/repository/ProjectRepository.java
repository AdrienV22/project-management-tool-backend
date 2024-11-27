package com.example.project_management_tool.repository;

import com.example.project_management_tool.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
