package com.example.rest_service.entity;

import java.util.List;

public class Dish {
    private Integer id;
    private String name;
    private DishTypeEnum dishType;
    private Double sellingPrice;
    private List<DishIngredient> dishIngredients;

    public Dish() {}

    public Dish(Integer id, String name, DishTypeEnum dishType, Double sellingPrice) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.sellingPrice = sellingPrice;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public DishTypeEnum getDishType() { return dishType; }
    public void setDishType(DishTypeEnum dishType) { this.dishType = dishType; }

    public Double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(Double sellingPrice) { this.sellingPrice = sellingPrice; }

    public List<DishIngredient> getDishIngredients() { return dishIngredients; }
    public void setDishIngredients(List<DishIngredient> dishIngredients) { this.dishIngredients = dishIngredients; }

    public Double getDishCost() {
        if (dishIngredients == null || dishIngredients.isEmpty()) {
            return 0.0;
        }
        return dishIngredients.stream()
                .mapToDouble(di -> di.getIngredient().getPrice() * di.getQuantityRequired())
                .sum();
    }

    public Double getGrossMargin() {
        if (sellingPrice == null) {
            throw new RuntimeException("Selling price is null for dish: " + name);
        }
        return sellingPrice - getDishCost();
    }

    @Override
    public String toString() {
        return String.format("Dish{id=%d, name='%s', dishType=%s, sellingPrice=%.2f}",
                id, name, dishType, sellingPrice != null ? sellingPrice : 0.0);
    }
}
