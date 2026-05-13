package com.example.taskmanager.dto;

import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

// DTO de saída da tarefa.
// Por que não expor a entidade Task direto?
// 1. A entidade tem lazy loading (categoria), que pode explodir na serialização.
// 2. Quero controlar exatamente quais campos vão pra resposta, sem vazar nada.
// 3. Se eu mexer na entidade, o contrato da API continua estável (DTO não muda).
// A categoria vem como objeto aninhado (CategoryResponse), não como id solto,
// pra o frontend não precisar fazer uma segunda chamada pra exibir o nome.
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
