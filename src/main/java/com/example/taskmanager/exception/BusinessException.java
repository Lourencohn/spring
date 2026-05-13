package com.example.taskmanager.exception;

// Exception pra violações de regra de negócio (estado inválido, conflito, etc).
// Estende RuntimeException pra não precisar declarar throws em todos os métodos.
// O GlobalExceptionHandler captura e devolve HTTP 422 Unprocessable Entity,
// que é o status correto pra "entendi a requisição, mas não posso processá-la
// porque viola uma regra de negócio".
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
