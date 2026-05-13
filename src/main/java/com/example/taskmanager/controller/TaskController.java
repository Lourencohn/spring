package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.enums.TaskStatus;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

// Controller principal da API.
// Aqui só cuido de HTTP: recebo o request, chamo o service e devolvo a resposta.
// Nenhuma regra de negócio mora aqui, isso fica no TaskService.
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    // Injeção por construtor pra deixar a dependência explícita e poder marcar como final.
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // GET /api/tasks
    // Lista paginada com filtros opcionais. Os três @RequestParam podem vir ou não,
    // o service decide qual query usar conforme o que foi passado.
    // O @PageableDefault define o default de paginação: 20 itens, ordenado pela
    // data de criação em ordem decrescente (mais novas primeiro).
    @GetMapping
    public Page<TaskResponse> list(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return taskService.findAll(status, priority, categoryId, pageable);
    }

    // GET /api/tasks/overdue
    // Endpoint dedicado pra trazer as tarefas vencidas que ainda não foram concluídas.
    // Útil pra dashboards e pra cobrar quem está atrasado.
    @GetMapping("/overdue")
    public List<TaskResponse> overdue() {
        return taskService.findOverdue();
    }

    // GET /api/tasks/{id}
    // Se o id não existir, o service lança ResourceNotFoundException e o handler global
    // transforma em 404. Por isso aqui não preciso de if/else nem de Optional.
    @GetMapping("/{id}")
    public TaskResponse get(@PathVariable Long id) {
        return taskService.findById(id);
    }

    // POST /api/tasks
    // @Valid dispara a Bean Validation no DTO (@NotBlank, @Size, etc).
    // Se falhar, o GlobalExceptionHandler retorna 400 com a lista de campos inválidos.
    // Devolvo 201 Created com o header Location apontando pro recurso recém criado,
    // que é o padrão REST pra POST de criação.
    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        TaskResponse created = taskService.create(request);
        return ResponseEntity
                .created(URI.create("/api/tasks/" + created.id()))
                .body(created);
    }

    // PUT /api/tasks/{id}
    // Atualiza a tarefa inteira. Como PUT semanticamente é replace, o cliente
    // precisa mandar todos os campos que quer manter.
    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return taskService.update(id, request);
    }

    // PATCH /api/tasks/{id}/complete
    // Mudança de estado específica, virou um endpoint próprio em vez de forçar
    // o cliente a mandar um PUT inteiro só pra trocar o status.
    // PATCH porque é atualização parcial.
    @PatchMapping("/{id}/complete")
    public TaskResponse complete(@PathVariable Long id) {
        return taskService.complete(id);
    }

    // DELETE /api/tasks/{id}
    // 204 No Content é o status correto pra delete bem sucedido (sem corpo de resposta).
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        taskService.delete(id);
    }
}
