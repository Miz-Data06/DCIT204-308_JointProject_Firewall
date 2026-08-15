package com.fooddelivery.database.repository;

import com.fooddelivery.database.DatabaseConnectionFactory;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.RequestStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class DeliveryRequestRepository {
    private final DatabaseConnectionFactory connectionFactory;

    public DeliveryRequestRepository(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void save(DeliveryRequest request) {
        String sql = """
                INSERT OR REPLACE INTO delivery_requests
                (request_id, source_location_id, destination_location_id, category, urgency, capacity_required,
                 time_submitted, deadline, status, priority_score)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, request.getRequestId());
            statement.setString(2, request.getSourceLocationId());
            statement.setString(3, request.getDestinationLocationId());
            statement.setString(4, request.getCategory());
            statement.setInt(5, request.getUrgency());
            statement.setDouble(6, request.getCapacityRequired());
            statement.setString(7, request.getTimeSubmitted().toString());
            statement.setString(8, request.getDeadline().toString());
            statement.setString(9, request.getStatus().name());
            statement.setDouble(10, request.getPriorityScore());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not save delivery request", exception);
        }
    }

    public DeliveryRequest findById(String requestId) {
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM delivery_requests WHERE request_id = ?")) {
            statement.setString(1, requestId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? toDeliveryRequest(resultSet) : null;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not find delivery request", exception);
        }
    }

    public CustomDynamicArray<DeliveryRequest> findAll() {
        CustomDynamicArray<DeliveryRequest> requests = new CustomDynamicArray<>();
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM delivery_requests ORDER BY request_id");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                requests.add(toDeliveryRequest(resultSet));
            }
            return requests;
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not list delivery requests", exception);
        }
    }

    public int count() {
        return RepositorySupport.count(connectionFactory, "delivery_requests");
    }

    private static DeliveryRequest toDeliveryRequest(ResultSet resultSet) throws SQLException {
        return new DeliveryRequest(
                resultSet.getString("request_id"),
                resultSet.getString("source_location_id"),
                resultSet.getString("destination_location_id"),
                resultSet.getString("category"),
                resultSet.getInt("urgency"),
                resultSet.getDouble("capacity_required"),
                LocalDateTime.parse(resultSet.getString("time_submitted")),
                LocalDateTime.parse(resultSet.getString("deadline")),
                RequestStatus.valueOf(resultSet.getString("status")),
                resultSet.getDouble("priority_score"));
    }
}
