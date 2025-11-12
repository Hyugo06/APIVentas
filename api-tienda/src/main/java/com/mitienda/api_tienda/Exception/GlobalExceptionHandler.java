package com.mitienda.api_tienda.Exception; // O el paquete que hayas creado

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // Actúa como un interceptor global de errores
public class GlobalExceptionHandler {

    /**
     * Este método se dispara automáticamente cuando una validación de @Valid falla.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // Devuelve un 400
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField(); // El campo que falló
            String errorMessage = error.getDefaultMessage(); // El 'message' que escribiste
            errors.put(fieldName, errorMessage);
        });

        return errors;
    }
}