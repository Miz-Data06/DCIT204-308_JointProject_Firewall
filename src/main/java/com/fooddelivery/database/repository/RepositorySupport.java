package com.fooddelivery.database.repository;

import com.fooddelivery.database.DatabaseConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class RepositorySupport {
    private RepositorySupport() {
    }

    static int count(DatabaseConnectionFactory connectionFactory, String tableName) {
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM " + tableName);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not count " + tableName, exception);
        }
    }
}
