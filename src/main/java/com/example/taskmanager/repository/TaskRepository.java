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

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByPriority(Priority priority, Pageable pageable);

    Page<Task> findByCategoryId(Long categoryId, Pageable pageable);

    long countByStatus(TaskStatus status);

    long countByPriority(Priority priority);

    List<Task> findByDueDateBeforeAndStatusNot(LocalDate date, TaskStatus status);

    @Query("SELECT t.priority, COUNT(t) FROM Task t GROUP BY t.priority")
    List<Object[]> countGroupedByPriority();

    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countGroupedByStatus();
}
