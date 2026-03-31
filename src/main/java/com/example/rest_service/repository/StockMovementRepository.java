package com.example.rest_service.repository;

import com.example.rest_service.config.DataSource;
import com.example.rest_service.entity.MovementType;
import com.example.rest_service.entity.StockMovement;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class StockMovementRepository {
    private final DataSource dataSource;

    public StockMovementRepository() {
        this.dataSource = DataSource.getInstance();
    }

    public List<StockMovement> findByIngredientId(Integer ingredientId) throws SQLException {
        List<StockMovement> movements = new ArrayList<>();
        String sql = "SELECT id, quantity, type, unit, creation_datetime FROM StockMovement " +
                "WHERE id_ingredient = ? ORDER BY creation_datetime";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ingredientId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                StockMovement movement = new StockMovement();
                movement.setId(rs.getInt("id"));
                movement.setQuantity(rs.getDouble("quantity"));
                movement.setType(MovementType.valueOf(rs.getString("type")));
                movement.setUnit(rs.getString("unit"));
                movement.setCreationDateTime(rs.getTimestamp("creation_datetime").toInstant());
                movements.add(movement);
            }
        }
        return movements;
    }

    public StockMovement save(StockMovement movement) throws SQLException {
        String sql = "INSERT INTO StockMovement (id_ingredient, quantity, type, unit, creation_datetime) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, movement.getIngredient().getId());
            pstmt.setDouble(2, movement.getQuantity());
            pstmt.setString(3, movement.getType().name());
            pstmt.setString(4, movement.getUnit());
            pstmt.setTimestamp(5, Timestamp.from(movement.getCreationDateTime()));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                movement.setId(rs.getInt(1));
            }
        }
        return movement;
    }
}