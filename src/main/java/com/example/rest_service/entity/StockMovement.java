package com.example.rest_service.entity;

import java.time.Instant;

public class StockMovement {
    private Integer id;
    private Ingredient ingredient;
    private MovementType type;
    private Double quantity;
    private String unit;
    private Instant creationDateTime;

    public StockMovement() {}

    public StockMovement(Ingredient ingredient, MovementType type, Double quantity, String unit, Instant creationDateTime) {
        this.ingredient = ingredient;
        this.type = type;
        this.quantity = quantity;
        this.unit = unit;
        this.creationDateTime = creationDateTime;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Ingredient getIngredient() { return ingredient; }
    public void setIngredient(Ingredient ingredient) { this.ingredient = ingredient; }

    public MovementType getType() { return type; }
    public void setType(MovementType type) { this.type = type; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Instant getCreationDateTime() { return creationDateTime; }
    public void setCreationDateTime(Instant creationDateTime) { this.creationDateTime = creationDateTime; }
}
