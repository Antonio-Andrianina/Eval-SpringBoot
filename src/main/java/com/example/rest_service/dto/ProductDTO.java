package com.example.rest_service.dto;

import java.time.Instant;

public class ProductDTO {
    private Integer id;
    private String name;
    private Double price;
    private Instant creationDateTime;

    public ProductDTO() {}

    public ProductDTO(Integer id, String name, Double price, Instant creationDateTime) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.creationDateTime = creationDateTime;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Instant getCreationDateTime() { return creationDateTime; }
    public void setCreationDateTime(Instant creationDateTime) { this.creationDateTime = creationDateTime; }
}
