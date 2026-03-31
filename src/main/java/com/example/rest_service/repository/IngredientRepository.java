package com.example.rest_service.repository;

import com.example.rest_service.config.DataSource;
import com.example.rest_service.entity.CategoryEnum;
import com.example.rest_service.entity.Ingredient;
import com.example.rest_service.entity.MovementType;
import com.example.rest_service.entity.StockMovement;
import com.example.rest_service.exception.IngredientNotFoundException;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class IngredientRepository {
    private final DataSource dataSource;

    public IngredientRepository() {
        this.dataSource = DataSource.getInstance();
    }

    public List<Ingredient> findAll() throws SQLException {
        List<Ingredient> ingredients = new ArrayList<>();
        String sql = "SELECT id, name, price, category FROM Ingredient_Base ORDER BY id";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setPrice(rs.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
                ingredients.add(ingredient);
            }
        }
        return ingredients;
    }

    public Ingredient findById(Integer id) throws SQLException {
        String sql = "SELECT id, name, price, category FROM Ingredient_Base WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setPrice(rs.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
                return ingredient;
            }
            throw new IngredientNotFoundException(id);
        }
    }

    public Ingredient save(Ingredient ingredient) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            String checkSql = "SELECT id FROM Ingredient_Base WHERE name = ?";
            Integer ingredientId = null;

            try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setString(1, ingredient.getName());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    ingredientId = rs.getInt(1);
                    String updateSql = "UPDATE Ingredient_Base SET price = ?, category = ? WHERE id = ?";
                    try (PreparedStatement pstmt2 = conn.prepareStatement(updateSql)) {
                        pstmt2.setDouble(1, ingredient.getPrice());
                        pstmt2.setString(2, ingredient.getCategory().name());
                        pstmt2.setInt(3, ingredientId);
                        pstmt2.executeUpdate();
                    }
                } else {
                    String insertSql = "INSERT INTO Ingredient_Base (name, price, category) VALUES (?, ?, ?) RETURNING id";
                    try (PreparedStatement pstmt2 = conn.prepareStatement(insertSql)) {
                        pstmt2.setString(1, ingredient.getName());
                        pstmt2.setDouble(2, ingredient.getPrice());
                        pstmt2.setString(3, ingredient.getCategory().name());
                        ResultSet rs2 = pstmt2.executeQuery();
                        if (rs2.next()) {
                            ingredientId = rs2.getInt(1);
                        }
                    }
                }
            }

            ingredient.setId(ingredientId);

            if (ingredient.getStockMovements() != null && !ingredient.getStockMovements().isEmpty()) {
                String movementSql = "INSERT INTO StockMovement (id_ingredient, quantity, type, unit, creation_datetime) " +
                        "VALUES (?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING";

                for (StockMovement movement : ingredient.getStockMovements()) {
                    try (PreparedStatement pstmt = conn.prepareStatement(movementSql, Statement.RETURN_GENERATED_KEYS)) {
                        pstmt.setInt(1, ingredientId);
                        pstmt.setDouble(2, movement.getQuantity());
                        pstmt.setString(3, movement.getType().name());
                        pstmt.setString(4, movement.getUnit());
                        pstmt.setTimestamp(5, Timestamp.from(movement.getCreationDateTime()));
                        pstmt.executeUpdate();
                    }
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
        }
        return ingredient;
    }

    public List<Ingredient> findByCriteria(String ingredientName, CategoryEnum category,
                                           String dishName, int page, int size) throws SQLException {
        int offset = (page - 1) * size;
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT ib.id, ib.name, ib.price, ib.category " +
                        "FROM Ingredient_Base ib " +
                        "LEFT JOIN DishIngredient di ON ib.id = di.id_ingredient " +
                        "LEFT JOIN Dish d ON di.id_dish = d.id " +
                        "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (ingredientName != null && !ingredientName.isEmpty()) {
            sql.append("AND ib.name ILIKE ? ");
            params.add("%" + ingredientName + "%");
        }

        if (category != null) {
            sql.append("AND ib.category = ? ");
            params.add(category.name());
        }

        if (dishName != null && !dishName.isEmpty()) {
            sql.append("AND d.name ILIKE ? ");
            params.add("%" + dishName + "%");
        }

        sql.append("ORDER BY ib.id LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);

        List<Ingredient> ingredients = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setPrice(rs.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
                ingredients.add(ingredient);
            }
        }
        return ingredients;
    }

    public Double getStockValueAt(Integer ingredientId, Instant at, String unit) throws SQLException {
        String sql = "SELECT COALESCE(SUM(CASE WHEN type = 'IN' THEN quantity ELSE -quantity END), 0) as stock " +
                "FROM StockMovement WHERE id_ingredient = ? AND creation_datetime <= ? AND unit = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ingredientId);
            pstmt.setTimestamp(2, Timestamp.from(at));
            pstmt.setString(3, unit);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("stock");
            }
        }
        return 0.0;
    }
}
