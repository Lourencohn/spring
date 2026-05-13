package com.example.taskmanager.dto;

import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// DTO de entrada pra criação e atualização de tarefa.
// Usei record porque é imutável, conciso e perfeito pra esse caso (só carrega dados).
// As anotações Bean Validation rodam quando o controller marca o parâmetro com @Valid.
// Faltar campo obrigatório ou estourar tamanho retorna 400 com a lista de erros
// montada pelo GlobalExceptionHandler.
public record TaskRequest(
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 120, message = "O título deve ter no máximo 120 caracteres")
        String title,

        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
        String description,

        // priority e status são opcionais. Se não vierem, o @PrePersist da entidade
        // aplica os defaults (MEDIUM e PENDING).
        Priority priority,

        TaskStatus status,

        LocalDate dueDate,

        // categoryId opcional: tarefa pode ficar sem categoria.
        Long categoryId
) {
}
