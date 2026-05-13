package com.example.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "O nome da categoria é obrigatório")
        @Size(max = 60, message = "O nome deve ter no máximo 60 caracteres")
        String name,

        @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres")
        String description
) {
}
