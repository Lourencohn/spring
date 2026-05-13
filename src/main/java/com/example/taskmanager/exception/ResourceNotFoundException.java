package com.example.taskmanager.exception;

// Exception lançada quando alguém pede um recurso que não existe (Task ou Category
// com id que não está no banco). O handler global transforma em HTTP 404.
public class ResourceNotFoundException extends RuntimeException {

    // Construtor de conveniência pra montar a mensagem padronizada
    // ("Tarefa com id 42 não foi encontrado(a)").
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s com id %d não foi encontrado(a)", resource, id));
    }

    // Sobrecarga pra casos onde a mensagem é específica e não cabe no template.
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
