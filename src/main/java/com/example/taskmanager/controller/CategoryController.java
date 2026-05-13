package com.example.taskmanager.controller;

import com.example.taskmanager.dto.CategoryRequest;
import com.example.taskmanager.dto.CategoryResponse;
import com.example.taskmanager.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

// CRUD de categorias. Mesma estrutura do TaskController, só que mais enxuto
// porque categoria é uma entidade simples (sem filtros, sem paginação).
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // GET /api/categories
    // Sem paginação aqui de propósito: como o universo de categorias é pequeno,
    // listar tudo de uma vez é mais prático pro frontend popular dropdowns.
    @GetMapping
    public List<CategoryResponse> list() {
        return categoryService.findAll();
    }

    // GET /api/categories/{id}
    @GetMapping("/{id}")
    public CategoryResponse get(@PathVariable Long id) {
        return categoryService.findById(id);
    }

    // POST /api/categories
    // O service valida se já existe uma categoria com o mesmo nome (ignorando case)
    // antes de salvar, evitando bater na constraint UNIQUE do banco com erro feio.
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse created = categoryService.create(request);
        return ResponseEntity
                .created(URI.create("/api/categories/" + created.id()))
                .body(created);
    }

    // PUT /api/categories/{id}
    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(id, request);
    }

    // DELETE /api/categories/{id}
    // Se a categoria tiver tarefas vinculadas, o service bloqueia a remoção pra evitar
    // deixar tarefas órfãs (vira 422 Unprocessable Entity).
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}
