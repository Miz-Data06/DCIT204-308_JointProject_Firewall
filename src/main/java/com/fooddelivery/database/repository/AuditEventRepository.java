package com.fooddelivery.database.repository;

import com.fooddelivery.database.AuditEventRecord;
import com.fooddelivery.database.DatabaseConnectionFactory;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class AuditEventRepository {
    private final DatabaseConnectionFactory connectionFactory;

    public AuditEventRepository(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void save(AuditEventRecord record) {
        String sql = """
                INSERT OR REPLACE INTO audit_events
                (audit_id, entity_type, entity_id, action, occurred_at, performed_by, details)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, record.getAuditId());
            statement.setString(2, record.getEntityType());
            statement.setString(3, record.getEntityId());
            statement.setString(4, record.getAction());
            statement.setString(5, record.getOccurredAt().toString());
            statement.setString(6, record.getPerformedBy());
            statement.setString(7, record.getDetails());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not save audit event", exception);
        }
    }

    public AuditEventRecord findById(String auditId) {
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM audit_events WHERE audit_id = ?")) {
            statement.setString(1, auditId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? toRecord(resultSet) : null;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not find audit event", exception);
        }
    }

    public CustomDynamicArray<AuditEventRecord> findAll() {
        CustomDynamicArray<AuditEventRecord> records = new CustomDynamicArray<>();
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM audit_events ORDER BY audit_id");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                records.add(toRecord(resultSet));
            }
            return records;
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not list audit events", exception);
        }
    }

    public int count() {
        return RepositorySupport.count(connectionFactory, "audit_events");
    }

    private static AuditEventRecord toRecord(ResultSet resultSet) throws SQLException {
        return new AuditEventRecord(
                resultSet.getString("audit_id"),
                resultSet.getString("entity_type"),
                resultSet.getString("entity_id"),
                resultSet.getString("action"),
                LocalDateTime.parse(resultSet.getString("occurred_at")),
                resultSet.getString("performed_by"),
                resultSet.getString("details"));
    }
}
