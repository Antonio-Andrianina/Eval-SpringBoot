package com.example.rest_service.repository;

import com.example.rest_service.config.DataSource;
import com.example.rest_service.entity.Product;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {
    private final DataSource dataSource;

    public ProductRepository() {
        this.dataSource = DataSource.getInstance();
    }

    public List<Product> findAll() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id, name, price, creation_datetime FROM Product ORDER BY id";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getDouble("price"));
                product.setCreationDateTime(rs.getTimestamp("creation_datetime").toInstant());
                products.add(product);
            }
        }
        return products;
    }

    public List<Product> findPaginated(int page, int size) throws SQLException {
        List<Product> products = new ArrayList<>();
        int offset = (page - 1) * size;
        String sql = "SELECT id, name, price, creation_datetime FROM Product ORDER BY id LIMIT ? OFFSET ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, size);
            pstmt.setInt(2, offset);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getDouble("price"));
                product.setCreationDateTime(rs.getTimestamp("creation_datetime").toInstant());
                products.add(product);
            }
        }
        return products;
    }

    public List<Product> findByCriteria(String productName, String categoryName,
                                        Instant creationMin, Instant creationMax) throws SQLException {
        List<Product> products = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT p.id, p.name, p.price, p.creation_datetime " +
                        "FROM Product p " +
                        "LEFT JOIN Product_category pc ON p.id = pc.product_id " +
                        "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (productName != null && !productName.isEmpty()) {
            sql.append("AND p.name ILIKE ? ");
            params.add("%" + productName + "%");
        }

        if (categoryName != null && !categoryName.isEmpty()) {
            sql.append("AND pc.name ILIKE ? ");
            params.add("%" + categoryName + "%");
        }

        if (creationMin != null) {
            sql.append("AND p.creation_datetime >= ? ");
            params.add(Timestamp.from(creationMin));
        }

        if (creationMax != null) {
            sql.append("AND p.creation_datetime <= ? ");
            params.add(Timestamp.from(creationMax));
        }

        sql.append("ORDER BY p.id");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getDouble("price"));
                product.setCreationDateTime(rs.getTimestamp("creation_datetime").toInstant());
                products.add(product);
            }
        }
        return products;
    }
}
