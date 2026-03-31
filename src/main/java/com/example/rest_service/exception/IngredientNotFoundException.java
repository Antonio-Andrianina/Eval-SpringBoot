package com.example.rest_service.exception;

public class IngredientNotFoundException extends BusinessException {
    public IngredientNotFoundException(Integer id) {
        super(String.format("Ingredient.id=%d is not found", id));
    }

    public IngredientNotFoundException(String message) {
        super(message);
    }
}
