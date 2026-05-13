package com.example.taskmanager.dto;

import java.util.Map;

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
