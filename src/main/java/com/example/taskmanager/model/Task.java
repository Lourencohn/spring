package com.example.taskmanager.model;

import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.enums.TaskStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Entidade principal do domínio.
// @Table(name = "tasks") fixa o nome da tabela como 'tasks' (no plural).
// Sem isso, o Hibernate usaria o naming strategy padrão e a tabela seria 'task'.
@Entity
@Table(name = "tasks")
public class Task {

    // IDENTITY deixa o banco gerar o id (AUTO_INCREMENT no H2/MySQL, SERIAL no Postgres).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // length define o VARCHAR no banco. Mantenho coerente com o @Size no DTO.
    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 1000)
    private String description;

    // EnumType.STRING grava o nome do enum no banco ('HIGH', 'URGENT') ao invés
    // do ordinal (0, 1, 2). É mais seguro: se eu reordenar o enum, os dados antigos
    // não viram outra coisa silenciosamente.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;

    private LocalDate dueDate;

    // updatable = false impede que esse campo seja alterado em um UPDATE.
    // Garante que ninguém vai sobrescrever a data de criação por acidente.
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    // FetchType.LAZY porque o default do @ManyToOne é EAGER, e eu não quero
    // que TODA consulta a Task traga a categoria junto. Quando precisar da categoria,
    // chamo task.getCategory() e o Hibernate faz a query lazy.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // Hook do JPA que roda ANTES do INSERT.
    // Garanto que createdAt e os defaults de status/priority estão preenchidos
    // mesmo que o caller esqueça de setar. Isso protege a invariante da entidade
    // independente de quem criou.
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = TaskStatus.PENDING;
        }
        if (this.priority == null) {
            this.priority = Priority.MEDIUM;
        }
    }

    // Método de domínio. Conclui a tarefa setando status E completedAt no mesmo átomo.
    // Coloquei aqui (em vez de no service) pra encapsular a regra "concluir significa
    // mudar o status E marcar a data". Impossível esquecer um dos dois.
    public void markAsDone() {
        this.status = TaskStatus.DONE;
        this.completedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
