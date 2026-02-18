package com.example.project_management_tool.dto;

import java.time.LocalDateTime;

public class ProjectMemberResponse {

    private Long userId;
    private String email;
    private String username;
    private String role;
    private LocalDateTime joinedAt;

    public ProjectMemberResponse() {}

    public ProjectMemberResponse(Long userId, String email, String username, String role, LocalDateTime joinedAt) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
