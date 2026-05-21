# Task Manager API

Uma API REST simples para gerenciar tarefas pessoais, construída com Spring Boot 3 e Java 17.

Fiz esse projeto pensando em entrevista técnica. A ideia não era encher de tecnologia, e sim mostrar que entendo bem os fundamentos: separação em camadas, injeção de dependência, validação, tratamento de erros, transações, testes e os idiomas naturais do Spring. Cada decisão tem um motivo, e eu consigo explicar o porquê.

## Stack

* Java 17 e Spring Boot 3.3
* Spring Web para a camada REST
* Spring Data JPA com banco H2 em memória
* Bean Validation (Jakarta Validation) para validar entradas
* JUnit 5 e MockMvc para os testes de integração

## Como executar

Você só precisa do Java 17 ou superior instalado. O Maven Wrapper já vem no projeto, então não é necessário ter Maven na máquina.

```bash
cd task-manager
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`. A partir daí você tem três pontos de entrada:

* **Interface web** em `http://localhost:8080/`, onde dá pra criar, listar, editar, concluir e remover tarefas e categorias, além de ver as métricas em tempo real.
* **API REST** em `http://localhost:8080/api/...`, que é o foco da apresentação.
* **Console do H2** em `http://localhost:8080/h2-console`. Para conectar, use a JDBC URL `jdbc:h2:mem:taskdb`, usuário `sa` e deixe a senha em branco. Útil pra mostrar que o JPA criou o schema sozinho.

Quando a aplicação inicia, o arquivo `data.sql` popula o banco com algumas categorias e tarefas de exemplo, então você já tem dados pra brincar.

Para rodar os testes:

```bash
./mvnw test
```

Para gerar o JAR executável:

```bash
./mvnw clean package
java -jar target/task-manager-0.0.1-SNAPSHOT.jar
```

## Estrutura do projeto

```
src/main/java/com/example/taskmanager/
├── TaskManagerApplication.java
├── controller/    # Camada REST, recebe requisições e devolve respostas
├── service/       # Regras de negócio e transações
├── repository/    # Interfaces Spring Data JPA
├── model/         # Entidades JPA mapeadas para tabelas
├── dto/           # Records de request e response
├── enums/         # Priority e TaskStatus
└── exception/     # Exceções de negócio e handler global de erros
```

A separação segue o que se espera de uma aplicação Spring bem organizada. Cada camada tem uma responsabilidade clara, e a dependência sempre aponta de fora pra dentro: controller chama service, service chama repository, repository conversa com o banco. O controller nunca toca diretamente no repository, isso protege as regras de negócio.

## O que cada conceito demonstra

Esse projeto foi construído pensando em cobrir os tópicos mais comuns de uma entrevista de Spring. Aqui está o mapa:

| Conceito | Onde encontrar no código |
|---|---|
| REST | Pasta `controller/`. Uso de `@RestController`, verbos HTTP corretos, `ResponseEntity` para customizar status e paginação com `Pageable`. |
| JPA | Pasta `model/`. Anotações `@Entity`, relacionamento `@ManyToOne` e `@OneToMany`, ciclo de vida com `@PrePersist`. |
| Repositories | Pasta `repository/`. Herança de `JpaRepository`, query methods (derivados do nome) e `@Query` para agregações nas métricas. |
| Injeção de dependência | Construtor em todos os `@Service` e `@RestController`. Nada de `@Autowired` em campo. |
| DTOs e validação | Pasta `dto/`. Uso de `record` com anotações como `@NotBlank` e `@Size`, e `@Valid` no controller para acionar a validação. |
| Tratamento de exceções | `exception/GlobalExceptionHandler` com `@RestControllerAdvice`. Centraliza o tratamento e padroniza o formato de erro. |
| Transações | `@Transactional` nos services, com `readOnly = true` nas consultas. |
| Testes | `TaskControllerIntegrationTest` cobrindo validação de campos, resposta 404 e cálculo de métricas. |
| Interface web | Pasta `src/main/resources/static/`. HTML, CSS e JS vanilla servidos pelo Spring Boot, consumindo a própria API. |

## Endpoints

### Tarefas

| Método | Rota | O que faz |
|---|---|---|
| GET | `/api/tasks` | Lista paginada. Aceita filtros por `status`, `priority` e `categoryId`, e paginação via `page`, `size` e `sort`. |
| GET | `/api/tasks/{id}` | Retorna o detalhe de uma tarefa. |
| GET | `/api/tasks/overdue` | Lista as tarefas atrasadas (vencidas e ainda não concluídas). |
| POST | `/api/tasks` | Cria uma nova tarefa. |
| PUT | `/api/tasks/{id}` | Atualiza uma tarefa existente. |
| PATCH | `/api/tasks/{id}/complete` | Marca a tarefa como concluída. |
| DELETE | `/api/tasks/{id}` | Remove a tarefa. |

### Categorias

| Método | Rota | O que faz |
|---|---|---|
| GET | `/api/categories` | Lista todas as categorias. |
| GET | `/api/categories/{id}` | Detalha uma categoria. |
| POST | `/api/categories` | Cria uma categoria. O nome precisa ser único. |
| PUT | `/api/categories/{id}` | Atualiza uma categoria. |
| DELETE | `/api/categories/{id}` | Remove. Só funciona se não houver tarefas vinculadas. |

### Métricas

| Método | Rota | O que faz |
|---|---|---|
| GET | `/api/metrics` | Retorna totais por status, totais por prioridade, taxa de conclusão e quantidade de tarefas atrasadas. |

## Exemplos de requisição

Criar uma tarefa:

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

Listar tarefas de alta prioridade ordenadas pela data de vencimento:

```bash
curl "http://localhost:8080/api/tasks?priority=HIGH&sort=dueDate,asc"
```

Concluir uma tarefa:

```bash
curl -X PATCH http://localhost:8080/api/tasks/1/complete
```

Consultar as métricas:

```bash
curl http://localhost:8080/api/metrics
```

Resposta esperada:

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

## Formato padrão de erro

Toda exceção tratada pelo `GlobalExceptionHandler` retorna o mesmo envelope, o que facilita muito a vida de quem consome a API:

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

Quando é uma validação de campo, o array `fieldErrors` vem preenchido com o nome do campo e a mensagem. Em outros tipos de erro, esse array fica vazio e a explicação vai no `message`.

## Decisões de design

Essa seção é a parte mais importante pra quem vai apresentar. Cada decisão técnica abaixo tem motivo, e saber explicar é o que separa "usei porque o tutorial mandou" de "entendo o que estou fazendo".

**Por que DTOs em vez de expor a entidade direto?**
Porque a entidade reflete o modelo persistido, não o contrato da API. Se eu expusesse a `Task` diretamente, qualquer mudança no banco quebraria os clientes. Além disso, evita problemas chatos como lazy-loading durante a serialização (o famoso `LazyInitializationException`) e permite que cada operação tenha sua própria validação.

**Por que usar `record` nos DTOs?**
Records são imutáveis por natureza, geram `equals`, `hashCode` e `toString` automaticamente, e expressam exatamente a intenção do DTO, que é transportar dados sem comportamento. Menos código boilerplate, mais clareza.

**Por que injeção por construtor e não `@Autowired` em campo?**
Três motivos. Primeiro, permite marcar os campos como `final`, o que deixa explícito que aquela dependência é obrigatória e não muda. Segundo, facilita testes, porque dá pra instanciar a classe diretamente passando mocks no construtor, sem precisar de reflection. Terceiro, deixa as dependências visíveis: se o construtor tem oito parâmetros, é sinal de que a classe está fazendo coisa demais.

**Por que `@RestControllerAdvice` global?**
Porque tratar exceção dentro de cada controller espalha lógica de apresentação por todo lado. Centralizando no handler global, a aplicação inteira fala a mesma língua de erro, com o mesmo formato JSON. Manutenção fica trivial.

**Por que `@Transactional(readOnly = true)` nas consultas?**
Em consultas puras, o Hibernate não precisa fazer dirty-checking nem flush. Marcando como `readOnly`, o provider JPA pula essas etapas e o banco também pode otimizar (em alguns drivers, abre conexão de leitura). Pequeno detalhe que mostra cuidado com performance.

**Por que H2 e não Postgres ou MySQL?**
Porque o foco é a demo. Como o projeto usa JPA, trocar de banco é só mudar a dependência no `pom.xml` e ajustar a URL no `application.yml`. O código Java não muda.

## Como subir no GitHub

Se ainda não está versionado, os comandos são:

```bash
cd task-manager
git init
git add .
git commit -m "Initial commit: Spring Boot Task Manager API"
git branch -M main
git remote add origin https://github.com/Lourencohn/spring.git
git push -u origin main
```
