package com.example.taskmanager.repository;

import com.example.taskmanager.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Repositório de Category. Bem mais enxuto que o de Task porque categoria é
// uma entidade simples e o CRUD herdado de JpaRepository cobre quase tudo.
// Os dois métodos abaixo existem por causa da unicidade do campo 'name'.
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Buscar categoria pelo nome ignorando maiúsculas/minúsculas.
    // IgnoreCase faz o Spring gerar LOWER(name) = LOWER(?) na query.
    // Optional porque pode não achar.
    Optional<Category> findByNameIgnoreCase(String name);

    // existsBy é mais barato que findBy + isPresent: faz SELECT COUNT > 0 no banco
    // e o banco pode parar no primeiro match. Uso no service pra validar duplicidade
    // antes de tentar salvar.
    boolean existsByNameIgnoreCase(String name);
}
