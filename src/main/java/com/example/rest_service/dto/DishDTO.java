package com.example.rest_service.dto;

import java.util.List;

public class DishDTO {
    private Integer id;
    private String name;
    private Double sellingPrice;
    private List<IngredientDTO> ingredients;

    public DishDTO() {}

    public DishDTO(Integer id, String name, Double sellingPrice, List<IngredientDTO> ingredients) {
        this.id = id;
        this.name = name;
        this.sellingPrice = sellingPrice;
        this.ingredients = ingredients;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(Double sellingPrice) { this.sellingPrice = sellingPrice; }

    public List<IngredientDTO> getIngredients() { return ingredients; }
    public void setIngredients(List<IngredientDTO> ingredients) { this.ingredients = ingredients; }
}
