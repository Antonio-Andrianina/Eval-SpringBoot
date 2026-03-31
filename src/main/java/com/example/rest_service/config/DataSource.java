package com.example.rest_service.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {
    private static DataSource instance;
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    private final String jdbcUrl;
    private final String username;
    private final String password;

    private DataSource() {
        this.jdbcUrl = getEnv("DISH_DB_URL", "jdbc:postgresql://localhost:5432/mini_dish_db");
        this.username = getEnv("DB_USERNAME", "mini_dish_db_manager");
        this.password = getEnv("DB_PASSWORD", "123456");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL Driver not found", e);
        }
    }

    public static synchronized DataSource getInstance() {
        if (instance == null) {
            instance = new DataSource();
        }
        return instance;
    }

    private String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null && dotenv != null) {
            value = dotenv.get(key);
        }
        return value != null ? value : defaultValue;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }
}
