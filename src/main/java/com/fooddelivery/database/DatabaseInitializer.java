package com.fooddelivery.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    private final DatabaseConnectionFactory connectionFactory;

    public DatabaseInitializer(DatabaseConnectionFactory connectionFactory) {
        if (connectionFactory == null) {
            throw new IllegalArgumentException("Connection factory must not be null");
        }
        this.connectionFactory = connectionFactory;
    }

    public void initialize() {
        try (Connection connection = connectionFactory.openConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(DatabaseSchema.CREATE_LOCATIONS);
            statement.execute(DatabaseSchema.CREATE_ROADS);
            statement.execute(DatabaseSchema.CREATE_RIDERS);
            statement.execute(DatabaseSchema.CREATE_DELIVERY_REQUESTS);
            statement.execute(DatabaseSchema.CREATE_ALGORITHM_RUNS);
            statement.execute(DatabaseSchema.CREATE_AUDIT_EVENTS);
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not initialize database schema", exception);
        }
    }
}
