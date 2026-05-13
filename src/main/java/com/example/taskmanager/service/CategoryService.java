package com.example.taskmanager.service;

import com.example.taskmanager.dto.CategoryRequest;
import com.example.taskmanager.dto.CategoryResponse;
import com.example.taskmanager.exception.BusinessException;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.model.Category;
import com.example.taskmanager.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Service de Category. Mesmo padrão do TaskService: @Transactional na classe
// e readOnly = true nos métodos de leitura.
@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        return toResponse(getCategory(id));
    }

    // Cria uma nova categoria.
    // Valido pelo nome antes de salvar pra dar uma mensagem amigável,
    // ao invés de deixar o banco lançar uma exception de constraint violation feia.
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessException("Já existe uma categoria com o nome '" + request.name() + "'");
        }
        Category category = new Category(request.name(), request.description());
        return toResponse(categoryRepository.save(category));
    }

    // Update tem uma sutileza: preciso verificar duplicidade só se o nome
    // estiver sendo trocado por um nome de OUTRA categoria. O filter() garante
    // que se eu estiver atualizando a própria categoria com o mesmo nome, passa.
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getCategory(id);
        categoryRepository.findByNameIgnoreCase(request.name())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException("Já existe outra categoria com o nome '" + request.name() + "'");
                });
        category.setName(request.name());
        category.setDescription(request.description());
        // Sem save() de novo, dirty checking cuida disso no commit da transação.
        return toResponse(category);
    }

    // Delete protegido: bloqueia se ainda houver tarefas vinculadas.
    // Evita deixar tarefas órfãs ou um ON DELETE CASCADE acidental no banco.
    public void delete(Long id) {
        Category category = getCategory(id);
        if (!category.getTasks().isEmpty()) {
            throw new BusinessException("Não é possível remover uma categoria que possui tarefas vinculadas");
        }
        categoryRepository.delete(category);
    }

    // Método público porque o TaskService precisa dele pra resolver a categoria
    // ao criar/atualizar uma tarefa (mantém a regra "categoria precisa existir"
    // num lugar só).
    public Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", id));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }
}
