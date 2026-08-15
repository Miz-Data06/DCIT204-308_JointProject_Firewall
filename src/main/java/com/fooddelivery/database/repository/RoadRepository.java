package com.fooddelivery.database.repository;

import com.fooddelivery.database.DatabaseConnectionFactory;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.Road;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RoadRepository {
    private final DatabaseConnectionFactory connectionFactory;

    public RoadRepository(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void save(Road road) {
        String sql = """
                INSERT OR REPLACE INTO roads
                (road_id, from_location_id, to_location_id, distance_km, normal_travel_time_minutes, road_condition_weight)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, road.getRoadId());
            statement.setString(2, road.getFromLocationId());
            statement.setString(3, road.getToLocationId());
            statement.setDouble(4, road.getDistanceKm());
            statement.setDouble(5, road.getNormalTravelTimeMinutes());
            statement.setDouble(6, road.getRoadConditionWeight());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not save road", exception);
        }
    }

    public Road findById(String roadId) {
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM roads WHERE road_id = ?")) {
            statement.setString(1, roadId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? toRoad(resultSet) : null;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not find road", exception);
        }
    }

    public CustomDynamicArray<Road> findAll() {
        CustomDynamicArray<Road> roads = new CustomDynamicArray<>();
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM roads ORDER BY road_id");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                roads.add(toRoad(resultSet));
            }
            return roads;
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not list roads", exception);
        }
    }

    public int count() {
        return RepositorySupport.count(connectionFactory, "roads");
    }

    private static Road toRoad(ResultSet resultSet) throws SQLException {
        return new Road(
                resultSet.getString("road_id"),
                resultSet.getString("from_location_id"),
                resultSet.getString("to_location_id"),
                resultSet.getDouble("distance_km"),
                resultSet.getDouble("normal_travel_time_minutes"),
                resultSet.getDouble("road_condition_weight"));
    }
}
