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

    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessException("Já existe uma categoria com o nome '" + request.name() + "'");
        }
        Category category = new Category(request.name(), request.description());
        return toResponse(categoryRepository.save(category));
    }

    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getCategory(id);
        categoryRepository.findByNameIgnoreCase(request.name())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException("Já existe outra categoria com o nome '" + request.name() + "'");
                });
        category.setName(request.name());
        category.setDescription(request.description());
        return toResponse(category);
    }

    public void delete(Long id) {
        Category category = getCategory(id);
        if (!category.getTasks().isEmpty()) {
            throw new BusinessException("Não é possível remover uma categoria que possui tarefas vinculadas");
        }
        categoryRepository.delete(category);
    }

    public Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", id));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }
}
