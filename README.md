# Task Manager API

API REST simples de gestão de tarefas pessoais, construída com Spring Boot 3 e Java 17.
Projeto criado como demonstração para entrevista técnica — foco em clareza, idiomas do framework e boas práticas, sem complexidade desnecessária.

## Stack

- **Java 17** + **Spring Boot 3.3**
- **Spring Web** (REST)
- **Spring Data JPA** + **H2** (banco em memória)
- **Bean Validation** (Jakarta Validation)
- **JUnit 5** + **MockMvc**

## Como executar

```bash
cd task-manager
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`:

- **Interface web** → `http://localhost:8080/` (criar, listar, editar, concluir e remover tarefas e categorias; ver métricas)
- **API REST** → `http://localhost:8080/api/...`
- **Console H2** → `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:taskdb`, user `sa`, sem senha)

O banco é populado com dados de exemplo via `data.sql` na inicialização.

## Como subir no GitHub

```bash
cd task-manager
git init
git add .
git commit -m "Initial commit: Spring Boot Task Manager API"
git branch -M main
git remote add origin https://github.com/Lourencohn/spring.git
git push -u origin main
```

## Conceitos demonstrados

| Conceito | Onde ver |
|---|---|
| **REST** | `controller/` — `@RestController`, verbos HTTP, `ResponseEntity`, paginação com `Pageable` |
| **JPA** | `model/` — `@Entity`, relacionamento `@ManyToOne`, `@OneToMany`, `@PrePersist` |
| **Repositories** | `repository/` — `JpaRepository`, query methods, `@Query` para agregações |
| **Injeção de dependência** | construtores em todos os `@Service` e `@RestController` (sem `@Autowired` em campo) |
| **DTOs + Validação** | `dto/` — `record` com `@NotBlank`, `@Size`, `@Valid` no controller |
| **Tratamento de exceções** | `exception/GlobalExceptionHandler` com `@RestControllerAdvice` |
| **Transações** | `@Transactional` em services, `readOnly = true` para consultas |
| **Testes** | `TaskControllerIntegrationTest` — validação, 404, métricas |
| **Interface web** | `src/main/resources/static/` — HTML/CSS/JS vanilla servido pelo Spring Boot |

## Endpoints

### Tarefas

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/tasks` | Lista paginada. Filtros: `status`, `priority`, `categoryId`. Paginação: `page`, `size`, `sort` |
| GET | `/api/tasks/{id}` | Detalhe |
| GET | `/api/tasks/overdue` | Tarefas atrasadas (vencidas e não concluídas) |
| POST | `/api/tasks` | Cria |
| PUT | `/api/tasks/{id}` | Atualiza |
| PATCH | `/api/tasks/{id}/complete` | Marca como concluída |
| DELETE | `/api/tasks/{id}` | Remove |

### Categorias

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/categories` | Lista |
| GET | `/api/categories/{id}` | Detalhe |
| POST | `/api/categories` | Cria (nome único) |
| PUT | `/api/categories/{id}` | Atualiza |
| DELETE | `/api/categories/{id}` | Remove (só se não houver tarefas vinculadas) |

### Métricas

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/metrics` | Totais por status, por prioridade, taxa de conclusão e tarefas atrasadas |

## Exemplos de requisição

**Criar tarefa**

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Estudar para entrevista",
    "description": "Revisar Spring Boot e JPA",
    "priority": "HIGH",
    "status": "PENDING",
    "dueDate": "2026-05-20",
    "categoryId": 2
  }'
```

**Listar tarefas de alta prioridade, ordenadas por vencimento**

```bash
curl "http://localhost:8080/api/tasks?priority=HIGH&sort=dueDate,asc"
```

**Concluir tarefa**

```bash
curl -X PATCH http://localhost:8080/api/tasks/1/complete
```

**Métricas**

```bash
curl http://localhost:8080/api/metrics
```

Resposta:

```json
{
  "totalTasks": 6,
  "pendingTasks": 3,
  "inProgressTasks": 2,
  "doneTasks": 1,
  "cancelledTasks": 0,
  "overdueTasks": 1,
  "completionRate": 16.67,
  "tasksByPriority": { "LOW": 1, "MEDIUM": 2, "HIGH": 2, "URGENT": 1 },
  "tasksByStatus":   { "PENDING": 3, "IN_PROGRESS": 2, "DONE": 1, "CANCELLED": 0 }
}
```

## Formato de erro

Todas as exceções tratadas retornam o mesmo envelope:

```json
{
  "timestamp": "2026-05-13T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Erro de validação nos campos da requisição",
  "path": "/api/tasks",
  "fieldErrors": [
    { "field": "title", "message": "O título é obrigatório" }
  ]
}
```

## Estrutura

```
src/main/java/com/example/taskmanager/
├── TaskManagerApplication.java
├── controller/    # Camada REST
├── service/       # Regras de negócio + @Transactional
├── repository/    # Spring Data JPA
├── model/         # Entidades JPA
├── dto/           # Records de request/response
├── enums/         # Priority, TaskStatus
└── exception/     # Exceções de negócio + handler global
```

## Pontos de discussão para a entrevista

- **Por que DTOs em vez de expor a entidade?** Desacopla a API do modelo de dados, evita lazy-loading em serialização e permite validação específica por operação.
- **Por que `record`?** Imutáveis, concisos, ideais para DTOs.
- **Por que injeção por construtor?** Permite imutabilidade (`final`), facilita testes (sem reflection) e deixa explícitas as dependências obrigatórias.
- **Por que `@RestControllerAdvice`?** Centraliza o tratamento de erros e padroniza o formato de resposta.
- **Por que `@Transactional(readOnly = true)`?** Otimização do provider JPA para consultas — sem flush, sem dirty-checking.
