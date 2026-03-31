package com.example.rest_service.dto;

import com.example.rest_service.entity.CategoryEnum;

public class IngredientDTO {
    private Integer id;
    private String name;
    private Double price;
    private CategoryEnum category;

    public IngredientDTO() {}

    public IngredientDTO(Integer id, String name, Double price, CategoryEnum category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public CategoryEnum getCategory() { return category; }
    public void setCategory(CategoryEnum category) { this.category = category; }
}
