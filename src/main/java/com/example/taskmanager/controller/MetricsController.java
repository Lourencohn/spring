package com.example.taskmanager.controller;

import com.example.taskmanager.dto.MetricsResponse;
import com.example.taskmanager.service.MetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Endpoint só de leitura pra alimentar o dashboard de métricas.
// Mantive separado do TaskController porque é uma responsabilidade diferente:
// não é CRUD de tarefa, é uma visão agregada.
@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    // GET /api/metrics
    // Retorna totais por status, por prioridade, taxa de conclusão e tarefas atrasadas.
    @GetMapping
    public MetricsResponse getMetrics() {
        return metricsService.getMetrics();
    }
}
