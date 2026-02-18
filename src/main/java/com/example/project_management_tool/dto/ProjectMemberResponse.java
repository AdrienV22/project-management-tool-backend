package com.example.project_management_tool.dto;

import com.example.project_management_tool.entity.ProjectMember;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ProjectMemberResponse {
    private Long userId;
    private String username;
    private String email;
    private ProjectMember.ProjectRole role;
    private LocalDateTime joinedAt;
}
