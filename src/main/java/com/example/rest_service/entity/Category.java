package com.example.rest_service.entity;

public class Category {
    private Integer id;
    private String name;
    private Integer productId;

    public Category() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
}
