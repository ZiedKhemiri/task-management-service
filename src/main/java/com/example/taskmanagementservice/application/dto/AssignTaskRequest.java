package com.example.taskmanagementservice.application.dto;

public record AssignTaskRequest(
        String userId,
        String username
) {}
