package com.example.project_management_tool.entity;

import com.example.project_management_tool.model.TaskModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "task_history")
public class TaskHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // LAZY + ignore pour éviter les boucles JSON (Task -> Project -> tasks -> ...)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnoreProperties({"project"})
    private TaskModel task;

    @Column(length = 255)
    private String modifiedBy;

    @Column(nullable = false)
    private LocalDateTime modifiedAt;

    @Column(nullable = false, length = 60)
    private String fieldName;

    @Column(length = 1000)
    private String oldValue;

    @Column(length = 1000)
    private String newValue;
}
