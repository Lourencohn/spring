package com.example.taskmanager.dto;

import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        Priority priority,
        TaskStatus status,
        LocalDate dueDate,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        CategoryResponse category
) {
}
