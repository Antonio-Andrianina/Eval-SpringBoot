package com.example.rest_service.dto;

import com.example.rest_service.entity.DishTypeEnum;

public class CreateDishDTO {
    private String name;
    private DishTypeEnum dishType;
    private Double sellingPrice;

    public CreateDishDTO() {}

    public CreateDishDTO(String name, DishTypeEnum dishType, Double sellingPrice) {
        this.name = name;
        this.dishType = dishType;
        this.sellingPrice = sellingPrice;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public DishTypeEnum getDishType() { return dishType; }
    public void setDishType(DishTypeEnum dishType) { this.dishType = dishType; }

    public Double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(Double sellingPrice) { this.sellingPrice = sellingPrice; }
}
