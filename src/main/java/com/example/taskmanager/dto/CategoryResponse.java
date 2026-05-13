package com.example.taskmanager.dto;

// DTO de saída da categoria. Bem simples: só os três campos que o frontend precisa.
// Não exponho o Set<Task> da entidade aqui pra evitar referência circular na
// serialização (Task -> Category -> Task -> ...) e pra não trazer um monte de
// tarefa toda vez que alguém pede uma categoria.
public record CategoryResponse(
        Long id,
        String name,
        String description
) {
}
