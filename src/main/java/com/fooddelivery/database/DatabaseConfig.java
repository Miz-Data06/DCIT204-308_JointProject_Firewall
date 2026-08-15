package com.fooddelivery.database;

import java.nio.file.Path;

public final class DatabaseConfig {
    private final Path databasePath;

    public DatabaseConfig(Path databasePath) {
        if (databasePath == null) {
            throw new IllegalArgumentException("Database path must not be null");
        }
        this.databasePath = databasePath;
    }

    public static DatabaseConfig defaultConfig() {
        return new DatabaseConfig(Path.of("target", "food_delivery.db"));
    }

    public Path databasePath() {
        return databasePath;
    }

    public String jdbcUrl() {
        return "jdbc:sqlite:" + databasePath;
    }
}
