package com.example.taskmanager.exception;

import java.time.LocalDateTime;
import java.util.List;

// Envelope padrão de erro da API.
// Todo erro tratado pelo GlobalExceptionHandler retorna esse mesmo formato,
// pra deixar a vida de quem consome a API mais previsível.
// fieldErrors fica preenchido só em erros de validação de payload (400).
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorDetail> fieldErrors
) {
    // Record aninhado pra detalhar qual campo falhou e por quê,
    // usado quando a Bean Validation reclama de algum @NotBlank, @Size, etc.
    public record FieldErrorDetail(String field, String message) {
    }
}
