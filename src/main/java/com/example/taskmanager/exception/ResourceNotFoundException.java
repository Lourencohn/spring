package com.example.taskmanager.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s com id %d não foi encontrado(a)", resource, id));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
