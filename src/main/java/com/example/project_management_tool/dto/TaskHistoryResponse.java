package com.example.project_management_tool.dto;

import java.time.LocalDateTime;

public record TaskHistoryResponse(
        Long id,
        Long taskId,
        String modifiedBy,
        LocalDateTime modifiedAt,
        String fieldName,
        String oldValue,
        String newValue
) {}
