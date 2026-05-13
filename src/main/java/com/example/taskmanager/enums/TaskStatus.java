package com.example.taskmanager.enums;

// Estados possíveis de uma tarefa.
// PENDING e IN_PROGRESS são estados "ativos", DONE e CANCELLED são "finalizados".
// O service usa essa distinção pra bloquear transições inválidas (ex: não dá pra
// concluir uma tarefa cancelada).
public enum TaskStatus {
    PENDING,
    IN_PROGRESS,
    DONE,
    CANCELLED
}
