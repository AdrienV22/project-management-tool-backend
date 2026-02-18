package com.example.project_management_tool.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProjectMemberRequest {

    @NotBlank
    @Email
    private String email;

    @NotNull
    private String role; // "ADMIN" | "MEMBRE" | "OBSERVATEUR"

    public ProjectMemberRequest() {}

    public ProjectMemberRequest(String email, String role) {
        this.email = email;
        this.role = role;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
