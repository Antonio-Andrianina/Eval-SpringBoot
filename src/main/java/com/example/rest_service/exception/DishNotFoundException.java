package com.example.rest_service.exception;

public class DishNotFoundException extends BusinessException {
    public DishNotFoundException(Integer id) {
        super(String.format("Dish.id=%d is not found", id));
    }
}
