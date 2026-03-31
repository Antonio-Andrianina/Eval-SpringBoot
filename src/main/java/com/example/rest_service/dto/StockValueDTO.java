package com.example.rest_service.dto;

public class StockValueDTO {
    private String unit;
    private Double value;

    public StockValueDTO() {}

    public StockValueDTO(String unit, Double value) {
        this.unit = unit;
        this.value = value;
    }

    // Getters and Setters
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
}
