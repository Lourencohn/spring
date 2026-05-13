package com.example.taskmanager.repository;

import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.enums.TaskStatus;
import com.example.taskmanager.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

// Repositório de Task.
// Só de estender JpaRepository<Task, Long> já ganho ~20 métodos prontos:
// findAll, findById, save, deleteById, count, etc. Não preciso implementar nada,
// o Spring Data gera um proxy em runtime.
// Os métodos declarados abaixo são as queries customizadas que o JpaRepository
// padrão não cobre.
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Query derivation: o Spring lê o nome do método e gera a query sozinho.
    // findByStatus vira SELECT * FROM tasks WHERE status = ?
    // O Pageable adiciona LIMIT/OFFSET e a contagem total pra paginação.
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByPriority(Priority priority, Pageable pageable);

    // findByCategoryId atravessa o relacionamento: vira WHERE category_id = ?
    Page<Task> findByCategoryId(Long categoryId, Pageable pageable);

    // count com filtros, usado pelas métricas. SELECT COUNT(*) WHERE status = ?
    long countByStatus(TaskStatus status);

    long countByPriority(Priority priority);

    // Tarefas atrasadas: vencidas (due_date antes da data passada) e que ainda
    // não foram concluídas. Esse é o método mais "espertinho" do Spring Data aqui:
    // ele entende Before, And e Not no nome e monta a query.
    List<Task> findByDueDateBeforeAndStatusNot(LocalDate date, TaskStatus status);

    // Aqui o nome do método não daria conta (GROUP BY não é expressável em derivation).
    // Por isso uso @Query com JPQL: opera em entidades (Task, t.priority), não em
    // tabelas (tasks, priority). O Hibernate traduz pro SQL do dialeto na hora.
    // Retorna Object[] porque a projeção tem dois campos (priority, count) sem DTO.
    @Query("SELECT t.priority, COUNT(t) FROM Task t GROUP BY t.priority")
    List<Object[]> countGroupedByPriority();

    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countGroupedByStatus();
}
