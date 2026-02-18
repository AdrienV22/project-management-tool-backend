package com.example.project_management_tool.dto;

import com.example.project_management_tool.entity.ProjectMember;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectMemberRequest {

    @NotBlank
    @Email
    private String email;

    // optionnel : si null -> MEMBRE
    private ProjectMember.ProjectRole role;
}
