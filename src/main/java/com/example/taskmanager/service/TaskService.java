package com.example.taskmanager.service;

import com.example.taskmanager.dto.CategoryResponse;
import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.enums.TaskStatus;
import com.example.taskmanager.exception.BusinessException;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.model.Category;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

// Service da Task. É aqui que mora a regra de negócio.
// @Transactional na classe inteira garante que todo método público roda dentro
// de uma transação. Os métodos de leitura sobrescrevem com readOnly = true pra
// ganhar performance (Hibernate desliga dirty checking e flush automático).
@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryService categoryService;

    // Injeção por construtor. Quem olha o construtor já sabe o que essa classe
    // precisa pra funcionar. Campos final = imutabilidade.
    public TaskService(TaskRepository taskRepository, CategoryService categoryService) {
        this.taskRepository = taskRepository;
        this.categoryService = categoryService;
    }

    // Lista paginada com filtros mutuamente exclusivos.
    // A ordem dos ifs define a prioridade do filtro caso o cliente mande mais de um.
    // Optei por essa abordagem ao invés de Specifications pra manter simples,
    // já que os filtros são poucos. Se crescer, vale migrar pra Specification.
    @Transactional(readOnly = true)
    public Page<TaskResponse> findAll(TaskStatus status, Priority priority, Long categoryId, Pageable pageable) {
        Page<Task> page;
        if (status != null) {
            page = taskRepository.findByStatus(status, pageable);
        } else if (priority != null) {
            page = taskRepository.findByPriority(priority, pageable);
        } else if (categoryId != null) {
            page = taskRepository.findByCategoryId(categoryId, pageable);
        } else {
            page = taskRepository.findAll(pageable);
        }
        // page.map preserva a paginação, só troca o tipo do conteúdo.
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(Long id) {
        return toResponse(getTask(id));
    }

    // Tarefas atrasadas: due_date < hoje E status diferente de DONE.
    // O método do repositório usa derivation pelo nome (findByDueDateBeforeAndStatusNot).
    @Transactional(readOnly = true)
    public List<TaskResponse> findOverdue() {
        return taskRepository.findByDueDateBeforeAndStatusNot(LocalDate.now(), TaskStatus.DONE).stream()
                .map(this::toResponse)
                .toList();
    }

    // Cria uma nova tarefa.
    // Aqui é OBRIGATÓRIO chamar save() porque a entidade é nova (estado transient).
    // O @PrePersist na entidade preenche createdAt e os defaults de status/priority.
    public TaskResponse create(TaskRequest request) {
        Task task = new Task();
        applyRequest(task, request);
        return toResponse(taskRepository.save(task));
    }

    // Atualiza uma tarefa existente.
    // Repare que NÃO chamo save() aqui. Isso é dirty checking do Hibernate:
    // como a entidade foi carregada dentro da transação, ela está managed,
    // e qualquer alteração é detectada e persistida automaticamente no commit.
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = getTask(id);
        applyRequest(task, request);
        return toResponse(task);
    }

    // Conclui uma tarefa. Mesma lógica do update: sem save() porque já está managed.
    // As duas validações garantem que a transição de estado faz sentido.
    public TaskResponse complete(Long id) {
        Task task = getTask(id);
        if (task.getStatus() == TaskStatus.DONE) {
            throw new BusinessException("A tarefa já está marcada como concluída");
        }
        if (task.getStatus() == TaskStatus.CANCELLED) {
            throw new BusinessException("Não é possível concluir uma tarefa cancelada");
        }
        // markAsDone() encapsula a regra: muda status e preenche completedAt no mesmo átomo.
        task.markAsDone();
        return toResponse(task);
    }

    public void delete(Long id) {
        Task task = getTask(id);
        taskRepository.delete(task);
    }

    // Método auxiliar pra centralizar o "buscar ou 404".
    // Toda vez que preciso de uma tarefa pelo id, passo por aqui.
    private Task getTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", id));
    }

    // Aplica os campos do DTO na entidade.
    // Uso o mesmo método pra create e update pra evitar duplicação.
    // Os null checks em priority e status existem pra preservar o que o @PrePersist
    // já definiu como default quando o cliente não manda esses campos.
    private void applyRequest(Task task, TaskRequest request) {
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());

        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        if (request.status() != null) {
            task.setStatus(request.status());
            // Atalho: se o cliente mandou status = DONE, preencho completedAt também.
            // Sem isso, a tarefa ficaria DONE sem data de conclusão, o que é inconsistente.
            if (request.status() == TaskStatus.DONE && task.getCompletedAt() == null) {
                task.markAsDone();
            }
        }
        if (request.categoryId() != null) {
            // Usando o categoryService pra garantir que a categoria existe (senão lança 404).
            Category category = categoryService.getCategory(request.categoryId());
            task.setCategory(category);
        } else {
            // Permitindo desvincular a categoria mandando null no request.
            task.setCategory(null);
        }
    }

    // Converte a entidade pro DTO de resposta.
    // Aqui é a fronteira: depois desse método, ninguém de fora vê a entidade.
    // Isso protege contra lazy loading explodindo na serialização do Jackson.
    private TaskResponse toResponse(Task task) {
        CategoryResponse categoryResponse = null;
        if (task.getCategory() != null) {
            Category c = task.getCategory();
            categoryResponse = new CategoryResponse(c.getId(), c.getName(), c.getDescription());
        }
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getStatus(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getCompletedAt(),
                categoryResponse
        );
    }
}
