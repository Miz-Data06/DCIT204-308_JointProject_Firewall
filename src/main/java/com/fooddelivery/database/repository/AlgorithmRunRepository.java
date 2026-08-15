package com.fooddelivery.database.repository;

import com.fooddelivery.database.AlgorithmRunRecord;
import com.fooddelivery.database.DatabaseConnectionFactory;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class AlgorithmRunRepository {
    private final DatabaseConnectionFactory connectionFactory;

    public AlgorithmRunRepository(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void save(AlgorithmRunRecord record) {
        String sql = """
                INSERT OR REPLACE INTO algorithm_runs
                (run_id, algorithm_name, input_size, run_number, runtime_ns, memory_kb, executed_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, record.getRunId());
            statement.setString(2, record.getAlgorithmName());
            statement.setInt(3, record.getInputSize());
            statement.setInt(4, record.getRunNumber());
            statement.setLong(5, record.getRuntimeNs());
            statement.setDouble(6, record.getMemoryKb());
            statement.setString(7, record.getExecutedAt().toString());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not save algorithm run", exception);
        }
    }

    public AlgorithmRunRecord findById(String runId) {
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM algorithm_runs WHERE run_id = ?")) {
            statement.setString(1, runId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? toRecord(resultSet) : null;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not find algorithm run", exception);
        }
    }

    public CustomDynamicArray<AlgorithmRunRecord> findAll() {
        CustomDynamicArray<AlgorithmRunRecord> records = new CustomDynamicArray<>();
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM algorithm_runs ORDER BY run_id");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                records.add(toRecord(resultSet));
            }
            return records;
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not list algorithm runs", exception);
        }
    }

    public int count() {
        return RepositorySupport.count(connectionFactory, "algorithm_runs");
    }

    private static AlgorithmRunRecord toRecord(ResultSet resultSet) throws SQLException {
        return new AlgorithmRunRecord(
                resultSet.getString("run_id"),
                resultSet.getString("algorithm_name"),
                resultSet.getInt("input_size"),
                resultSet.getInt("run_number"),
                resultSet.getLong("runtime_ns"),
                resultSet.getDouble("memory_kb"),
                LocalDateTime.parse(resultSet.getString("executed_at")));
    }
}
