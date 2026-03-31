package com.example.rest_service.exception;

public class DishAlreadyExistsException extends BusinessException {
    public DishAlreadyExistsException(String name) {
        super(String.format("Dish.name=%s already exists", name));
    }
}
