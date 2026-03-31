package com.example.rest_service.repository;

import com.example.rest_service.config.DataSource;
import com.example.rest_service.entity.*;
import com.example.rest_service.exception.DishNotFoundException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DishRepository {
    private final DataSource dataSource;

    public DishRepository() {
        this.dataSource = DataSource.getInstance();
        IngredientRepository ingredientRepository = new IngredientRepository();
    }

    public List<Dish> findAll() throws SQLException {
        List<Dish> dishes = new ArrayList<>();
        String sql = "SELECT id, name, dish_type, selling_price FROM Dish ORDER BY id";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Dish dish = new Dish();
                dish.setId(rs.getInt("id"));
                dish.setName(rs.getString("name"));
                dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
                double sellingPrice = rs.getDouble("selling_price");
                if (!rs.wasNull()) {
                    dish.setSellingPrice(sellingPrice);
                }

                // Load dish ingredients
                List<DishIngredient> dishIngredients = findDishIngredientsByDishId(dish.getId(), conn);
                dish.setDishIngredients(dishIngredients);
                dishes.add(dish);
            }
        }
        return dishes;
    }

    public Dish findById(Integer id) throws SQLException {
        String sql = "SELECT id, name, dish_type, selling_price FROM Dish WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Dish dish = new Dish();
                dish.setId(rs.getInt("id"));
                dish.setName(rs.getString("name"));
                dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
                double sellingPrice = rs.getDouble("selling_price");
                if (!rs.wasNull()) {
                    dish.setSellingPrice(sellingPrice);
                }

                List<DishIngredient> dishIngredients = findDishIngredientsByDishId(id, conn);
                dish.setDishIngredients(dishIngredients);
                return dish;
            }
            throw new DishNotFoundException(id);
        }
    }

    private List<DishIngredient> findDishIngredientsByDishId(Integer dishId, Connection conn) throws SQLException {
        List<DishIngredient> dishIngredients = new ArrayList<>();
        String sql = "SELECT di.id, di.quantity_required, di.unit, " +
                "ib.id as ingredient_id, ib.name as ingredient_name, " +
                "ib.price as ingredient_price, ib.category as ingredient_category " +
                "FROM DishIngredient di " +
                "JOIN Ingredient_Base ib ON di.id_ingredient = ib.id " +
                "WHERE di.id_dish = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, dishId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                DishIngredient di = new DishIngredient();
                di.setId(rs.getInt("id"));
                di.setQuantityRequired(rs.getDouble("quantity_required"));
                di.setUnit(rs.getString("unit"));

                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("ingredient_id"));
                ingredient.setName(rs.getString("ingredient_name"));
                ingredient.setPrice(rs.getDouble("ingredient_price"));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString("ingredient_category")));

                di.setIngredient(ingredient);
                dishIngredients.add(di);
            }
        }
        return dishIngredients;
    }

    public Dish updateIngredients(Integer dishId, List<Integer> ingredientIds) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            // Check if dish exists
            Dish dish = findById(dishId);

            // Remove existing associations
            String deleteSql = "DELETE FROM DishIngredient WHERE id_dish = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                pstmt.setInt(1, dishId);
                pstmt.executeUpdate();
            }

            // Add new associations
            if (ingredientIds != null && !ingredientIds.isEmpty()) {
                String insertSql = "INSERT INTO DishIngredient (id_dish, id_ingredient, quantity_required, unit) " +
                        "VALUES (?, ?, 1.0, 'KG')";
                for (Integer ingredientId : ingredientIds) {
                    try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                        pstmt.setInt(1, dishId);
                        pstmt.setInt(2, ingredientId);
                        pstmt.executeUpdate();
                    }
                }
            }

            conn.commit();
            conn.setAutoCommit(true);

            // Reload dish with updated ingredients
            return findById(dishId);
        }
    }

    public List<Dish> findByIngredientName(String ingredientName) throws SQLException {
        List<Dish> dishes = new ArrayList<>();
        String sql = "SELECT DISTINCT d.id, d.name, d.dish_type, d.selling_price " +
                "FROM Dish d " +
                "JOIN DishIngredient di ON d.id = di.id_dish " +
                "JOIN Ingredient_Base ib ON di.id_ingredient = ib.id " +
                "WHERE ib.name ILIKE ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + ingredientName + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Dish dish = new Dish();
                dish.setId(rs.getInt("id"));
                dish.setName(rs.getString("name"));
                dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
                double sellingPrice = rs.getDouble("selling_price");
                if (!rs.wasNull()) {
                    dish.setSellingPrice(sellingPrice);
                }
                dishes.add(dish);
            }
        }
        return dishes;
    }

    public Double getDishCost(Integer dishId) throws SQLException {
        String sql = "SELECT SUM(ib.price * di.quantity_required) as total_cost " +
                "FROM DishIngredient di " +
                "JOIN Ingredient_Base ib ON di.id_ingredient = ib.id " +
                "WHERE di.id_dish = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, dishId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double cost = rs.getDouble("total_cost");
                return rs.wasNull() ? 0.0 : cost;
            }
        }
        return 0.0;
    }
}