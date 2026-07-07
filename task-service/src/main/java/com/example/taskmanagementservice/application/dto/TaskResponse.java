package com.example.taskmanagementservice.application.dto;

import com.example.taskmanagementservice.domain.model.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        Task.Status status,
        Task.Priority priority,
        LocalDate dueDate,
        String assigneeId,
        String assigneeUsername,
        Long enterpriseId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getAssigneeId(),
                task.getAssigneeUsername(),
                task.getEnterpriseId(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
