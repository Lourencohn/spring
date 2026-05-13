package com.example.taskmanager.enums;

// Prioridade da tarefa.
// Ordem segue a severidade crescente (LOW < MEDIUM < HIGH < URGENT),
// mas no banco grava como String, então a ordem do enum não afeta os dados.
public enum Priority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}
