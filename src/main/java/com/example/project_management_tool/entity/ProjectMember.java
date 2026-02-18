package com.example.project_management_tool.entity;

import com.example.project_management_tool.model.ProjectModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "project_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"})
)
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // On évite les boucles JSON : on ignore les listes côté ProjectModel et User
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnoreProperties({"userList", "taskList", "adminId"})
    private ProjectModel project;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"projectList", "tasks"})
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectRole role = ProjectRole.MEMBRE;

    @Column(nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    public ProjectMember() {}

    public ProjectMember(ProjectModel project, User user, ProjectRole role) {
        this.project = project;
        this.user = user;
        this.role = (role != null) ? role : ProjectRole.MEMBRE;
        this.joinedAt = LocalDateTime.now();
    }

    public enum ProjectRole {
        ADMIN,
        MEMBRE,
        OBSERVATEUR
    }
}
