package com.example.rest_service.repository;

import com.example.rest_service.config.DataSource;
import com.example.rest_service.entity.*;
import com.example.rest_service.exception.DishNotFoundException;
import com.example.rest_service.exception.DishAlreadyExistsException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DishRepository {
    private final DataSource dataSource;
    private final IngredientRepository ingredientRepository;

    public DishRepository() {
        this.dataSource = DataSource.getInstance();
        this.ingredientRepository = new IngredientRepository();
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

                List<DishIngredient> dishIngredients = findDishIngredientsByDishId(dish.getId(), conn);
                dish.setDishIngredients(dishIngredients);
                dishes.add(dish);
            }
        }
        return dishes;
    }

    public List<Dish> findAllWithFilters(Double priceUnder, Double priceOver, String name) throws SQLException {
        List<Dish> dishes = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT id, name, dish_type, selling_price FROM Dish WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (priceUnder != null) {
            sql.append("AND selling_price < ? ");
            params.add(priceUnder);
        }

        if (priceOver != null) {
            sql.append("AND selling_price > ? ");
            params.add(priceOver);
        }

        if (name != null && !name.trim().isEmpty()) {
            sql.append("AND name ILIKE ? ");
            params.add("%" + name + "%");
        }

        sql.append("ORDER BY id");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

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

    public boolean existsByName(String name) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Dish WHERE name = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }


    public List<Dish> createDishes(List<com.example.rest_service.dto.CreateDishDTO> dishDTOs) throws SQLException {
        List<Dish> createdDishes = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            String insertSql = "INSERT INTO Dish (name, dish_type, selling_price) VALUES (?, ?, ?) RETURNING id";

            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                for (com.example.rest_service.dto.CreateDishDTO dto : dishDTOs) {
                    pstmt.setString(1, dto.getName());
                    pstmt.setString(2, dto.getDishType().name());
                    if (dto.getSellingPrice() != null) {
                        pstmt.setDouble(3, dto.getSellingPrice());
                    } else {
                        pstmt.setNull(3, Types.DOUBLE);
                    }

                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        Dish dish = new Dish();
                        dish.setId(rs.getInt(1));
                        dish.setName(dto.getName());
                        dish.setDishType(dto.getDishType());
                        dish.setSellingPrice(dto.getSellingPrice());
                        dish.setDishIngredients(new ArrayList<>()); // Empty list for new dishes
                        createdDishes.add(dish);
                    }
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
        }

        return createdDishes;
    }

    public Dish updateIngredients(Integer dishId, List<Integer> ingredientIds) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            Dish dish = findById(dishId);

            String deleteSql = "DELETE FROM DishIngredient WHERE id_dish = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                pstmt.setInt(1, dishId);
                pstmt.executeUpdate();
            }

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
}