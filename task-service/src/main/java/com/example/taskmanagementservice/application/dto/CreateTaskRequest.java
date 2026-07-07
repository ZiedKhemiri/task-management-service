package com.example.taskmanagementservice.application.dto;

import com.example.taskmanagementservice.domain.model.Task;

import java.time.LocalDate;

public record CreateTaskRequest(
        String title,
        String description,
        Task.Priority priority,
        LocalDate dueDate
) {}
