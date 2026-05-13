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

// Service que monta a resposta do dashboard de métricas.
// @Transactional(readOnly = true) na classe inteira porque tudo aqui é leitura.
@Service
@Transactional(readOnly = true)
public class MetricsService {

    private final TaskRepository taskRepository;

    public MetricsService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // Monta o objeto de métricas com várias queries pequenas.
    // Daria pra otimizar usando GROUP BY (countGroupedByPriority / countGroupedByStatus
    // já existem no repositório), mas pro volume desse projeto, esse approach é
    // mais legível e o ganho de performance seria irrelevante.
    public MetricsResponse getMetrics() {
        long total = taskRepository.count();
        long pending = taskRepository.countByStatus(TaskStatus.PENDING);
        long inProgress = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        long done = taskRepository.countByStatus(TaskStatus.DONE);
        long cancelled = taskRepository.countByStatus(TaskStatus.CANCELLED);
        long overdue = taskRepository.findByDueDateBeforeAndStatusNot(LocalDate.now(), TaskStatus.DONE).size();

        // Cuidado com divisão por zero: se não tem tarefa, taxa de conclusão é zero.
        double completionRate = total == 0 ? 0.0 : round((double) done / total * 100.0);

        // LinkedHashMap pra preservar a ordem do enum no JSON de resposta
        // (LOW, MEDIUM, HIGH, URGENT). Se fosse HashMap, a ordem seria aleatória.
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

    // Arredondamento pra duas casas decimais, pra a taxa de conclusão não vir
    // com 16.666666... no JSON.
    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
