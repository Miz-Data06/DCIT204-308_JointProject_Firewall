package com.fooddelivery.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnectionFactory {
    private final DatabaseConfig config;

    public DatabaseConnectionFactory(DatabaseConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Database config must not be null");
        }
        this.config = config;
    }

    public Connection openConnection() {
        try {
            Path parent = config.databasePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Connection connection = DriverManager.getConnection(config.jdbcUrl());
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }
            return connection;
        } catch (IOException | SQLException exception) {
            throw new IllegalStateException("Could not open database connection", exception);
        }
    }
}
