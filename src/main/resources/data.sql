INSERT INTO categories (id, name, description) VALUES (1, 'Trabalho', 'Tarefas profissionais e do escritório');
INSERT INTO categories (id, name, description) VALUES (2, 'Estudos', 'Cursos, livros e estudos pessoais');
INSERT INTO categories (id, name, description) VALUES (3, 'Casa', 'Tarefas domésticas e organização');
INSERT INTO categories (id, name, description) VALUES (4, 'Saúde', 'Consultas, exercícios e bem-estar');

ALTER TABLE categories ALTER COLUMN id RESTART WITH 5;

INSERT INTO tasks (title, description, priority, status, due_date, created_at, category_id)
VALUES ('Preparar apresentação trimestral', 'Slides do Q2 para a diretoria', 'HIGH', 'IN_PROGRESS', DATEADD('DAY', 3, CURRENT_DATE), CURRENT_TIMESTAMP, 1);

INSERT INTO tasks (title, description, priority, status, due_date, created_at, category_id)
VALUES ('Revisar pull requests do time', 'Backlog de revisões pendentes no repositório', 'MEDIUM', 'PENDING', DATEADD('DAY', 1, CURRENT_DATE), CURRENT_TIMESTAMP, 1);

INSERT INTO tasks (title, description, priority, status, due_date, created_at, completed_at, category_id)
VALUES ('Estudar Spring Data JPA', 'Capítulos 4 a 6 do livro', 'MEDIUM', 'DONE', DATEADD('DAY', -2, CURRENT_DATE), DATEADD('DAY', -5, CURRENT_TIMESTAMP), DATEADD('DAY', -1, CURRENT_TIMESTAMP), 2);

INSERT INTO tasks (title, description, priority, status, due_date, created_at, category_id)
VALUES ('Pagar contas do mês', 'Luz, internet e condomínio', 'URGENT', 'PENDING', DATEADD('DAY', -1, CURRENT_DATE), CURRENT_TIMESTAMP, 3);

INSERT INTO tasks (title, description, priority, status, due_date, created_at, category_id)
VALUES ('Agendar check-up anual', 'Clínico geral e exames de rotina', 'LOW', 'PENDING', DATEADD('DAY', 15, CURRENT_DATE), CURRENT_TIMESTAMP, 4);

INSERT INTO tasks (title, description, priority, status, due_date, created_at, category_id)
VALUES ('Resolver exercícios de algoritmos', 'Lista 3 - grafos', 'HIGH', 'IN_PROGRESS', DATEADD('DAY', 7, CURRENT_DATE), CURRENT_TIMESTAMP, 2);
