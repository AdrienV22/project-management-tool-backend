package com.example.project_management_tool.repository;

import com.example.project_management_tool.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    // navigation property: project.id
    List<ProjectMember> findByProject_Id(Long projectId);

    // navigation properties: project.id + user.id
    Optional<ProjectMember> findByProject_IdAndUser_Id(Long projectId, Long userId);

    boolean existsByProject_IdAndUser_Id(Long projectId, Long userId);
}
