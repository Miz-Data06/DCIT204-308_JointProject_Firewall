package com.fooddelivery.database.repository;

import com.fooddelivery.database.DatabaseConnectionFactory;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.LocationType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LocationRepository {
    private final DatabaseConnectionFactory connectionFactory;

    public LocationRepository(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void save(Location location) {
        String sql = """
                INSERT OR REPLACE INTO locations
                (location_id, name, area, type, latitude, longitude)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, location.getLocationId());
            statement.setString(2, location.getName());
            statement.setString(3, location.getArea());
            statement.setString(4, location.getType().name());
            statement.setDouble(5, location.getLatitude());
            statement.setDouble(6, location.getLongitude());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not save location", exception);
        }
    }

    public Location findById(String locationId) {
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM locations WHERE location_id = ?")) {
            statement.setString(1, locationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? toLocation(resultSet) : null;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not find location", exception);
        }
    }

    public CustomDynamicArray<Location> findAll() {
        CustomDynamicArray<Location> locations = new CustomDynamicArray<>();
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM locations ORDER BY location_id");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                locations.add(toLocation(resultSet));
            }
            return locations;
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not list locations", exception);
        }
    }

    public int count() {
        return RepositorySupport.count(connectionFactory, "locations");
    }

    private static Location toLocation(ResultSet resultSet) throws SQLException {
        return new Location(
                resultSet.getString("location_id"),
                resultSet.getString("name"),
                resultSet.getString("area"),
                LocationType.valueOf(resultSet.getString("type")),
                resultSet.getDouble("latitude"),
                resultSet.getDouble("longitude"));
    }
}
