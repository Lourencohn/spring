package com.example.taskmanager.dto;

import java.util.Map;

// DTO de saída do dashboard de métricas.
// Tem dois "blocos": contadores fixos (total, pending, etc) e dois mapas com
// a quebra por prioridade e por status.
// Uso Map<String, Long> ao invés de Map<Priority, Long> pra o JSON sair com
// as chaves como string ("HIGH", "URGENT"), que é o que o frontend espera.
public record MetricsResponse(
        long totalTasks,
        long pendingTasks,
        long inProgressTasks,
        long doneTasks,
        long cancelledTasks,
        long overdueTasks,
        double completionRate,
        Map<String, Long> tasksByPriority,
        Map<String, Long> tasksByStatus
) {
}
