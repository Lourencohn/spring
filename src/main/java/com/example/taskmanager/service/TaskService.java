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

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryService categoryService;

    public TaskService(TaskRepository taskRepository, CategoryService categoryService) {
        this.taskRepository = taskRepository;
        this.categoryService = categoryService;
    }

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
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(Long id) {
        return toResponse(getTask(id));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findOverdue() {
        return taskRepository.findByDueDateBeforeAndStatusNot(LocalDate.now(), TaskStatus.DONE).stream()
                .map(this::toResponse)
                .toList();
    }

    public TaskResponse create(TaskRequest request) {
        Task task = new Task();
        applyRequest(task, request);
        return toResponse(taskRepository.save(task));
    }

    public TaskResponse update(Long id, TaskRequest request) {
        Task task = getTask(id);
        applyRequest(task, request);
        return toResponse(task);
    }

    public TaskResponse complete(Long id) {
        Task task = getTask(id);
        if (task.getStatus() == TaskStatus.DONE) {
            throw new BusinessException("A tarefa já está marcada como concluída");
        }
        if (task.getStatus() == TaskStatus.CANCELLED) {
            throw new BusinessException("Não é possível concluir uma tarefa cancelada");
        }
        task.markAsDone();
        return toResponse(task);
    }

    public void delete(Long id) {
        Task task = getTask(id);
        taskRepository.delete(task);
    }

    private Task getTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", id));
    }

    private void applyRequest(Task task, TaskRequest request) {
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());

        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        if (request.status() != null) {
            task.setStatus(request.status());
            if (request.status() == TaskStatus.DONE && task.getCompletedAt() == null) {
                task.markAsDone();
            }
        }
        if (request.categoryId() != null) {
            Category category = categoryService.getCategory(request.categoryId());
            task.setCategory(category);
        } else {
            task.setCategory(null);
        }
    }

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
