package com.example.taskmanager.dto;

import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskRequest(
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 120, message = "O título deve ter no máximo 120 caracteres")
        String title,

        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
        String description,

        Priority priority,

        TaskStatus status,

        LocalDate dueDate,

        Long categoryId
) {
}
