package com.example.taskmanager.service;

import com.example.taskmanager.dto.MetricsResponse;
import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.enums.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class MetricsService {

    private final TaskRepository taskRepository;

    public MetricsService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public MetricsResponse getMetrics() {
        long total = taskRepository.count();
        long pending = taskRepository.countByStatus(TaskStatus.PENDING);
        long inProgress = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        long done = taskRepository.countByStatus(TaskStatus.DONE);
        long cancelled = taskRepository.countByStatus(TaskStatus.CANCELLED);
        long overdue = taskRepository.findByDueDateBeforeAndStatusNot(LocalDate.now(), TaskStatus.DONE).size();

        double completionRate = total == 0 ? 0.0 : round((double) done / total * 100.0);

        Map<String, Long> byPriority = new LinkedHashMap<>();
        for (Priority p : Priority.values()) {
            byPriority.put(p.name(), taskRepository.countByPriority(p));
        }

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (TaskStatus s : TaskStatus.values()) {
            byStatus.put(s.name(), taskRepository.countByStatus(s));
        }

        return new MetricsResponse(
                total, pending, inProgress, done, cancelled,
                overdue, completionRate, byPriority, byStatus
        );
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
