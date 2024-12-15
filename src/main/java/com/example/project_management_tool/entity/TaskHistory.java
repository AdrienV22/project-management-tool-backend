package com.example.project_management_tool.entity;

import com.example.project_management_tool.model.TaskModel;
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

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private TaskModel task;

    private String modifiedBy;

    private LocalDateTime modifiedAt;

    private String fieldName;

    private String oldValue;

    private String newValue;

}
