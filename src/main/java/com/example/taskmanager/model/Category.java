package com.example.taskmanager.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

// Entidade Category. Cada tarefa pode pertencer a uma categoria (relacionamento N:1).
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // unique = true cria uma constraint UNIQUE no banco impedindo nomes repetidos.
    // Mesmo assim, valido antes no service pra dar uma mensagem amigável ao usuário.
    @Column(nullable = false, unique = true, length = 60)
    private String name;

    @Column(length = 200)
    private String description;

    // Lado inverso do relacionamento.
    // mappedBy = "category" diz: "essa relação já está mapeada do outro lado
    // (em Task.category), não cria tabela de junção".
    // Set ao invés de List pra evitar duplicatas e porque a ordem não importa aqui.
    @OneToMany(mappedBy = "category")
    private Set<Task> tasks = new HashSet<>();

    // Construtor sem args é exigido pelo JPA.
    public Category() {
    }

    // Construtor de conveniência pra facilitar a criação no service e no seed.
    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }
}
