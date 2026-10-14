package com.fooddelivery.database.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.fooddelivery.database.DatabaseConnectionFactory;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.Rider;
import com.fooddelivery.model.VehicleType;

public class RiderRepository {
    private final DatabaseConnectionFactory connectionFactory;

    public RiderRepository(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void save(Rider rider) {
        String sql = """
                INSERT OR REPLACE INTO riders
                (rider_id, name, current_location_id, vehicle_type, carrying_capacity, available)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, rider.getRiderId());
            statement.setString(2, rider.getName());
            statement.setString(3, rider.getCurrentLocationId());
            statement.setString(4, rider.getVehicleType().name());
            statement.setDouble(5, rider.getCarryingCapacity());
            statement.setInt(6, rider.isAvailable() ? 1 : 0);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not save rider", exception);
        }
    }

    public boolean reserveIfAvailable(String riderId) {
        try (Connection connection = connectionFactory.openConnection()) {
            return reserveIfAvailable(connection, riderId);
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not reserve rider", exception);
        }
    }

    public boolean reserveIfAvailable(Connection connection, String riderId) {
        String sql = "UPDATE riders SET available = 0 WHERE rider_id = ? AND available = 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, riderId);
            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not reserve rider", exception);
        }
    }

    public Rider findById(String riderId) {
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM riders WHERE rider_id = ?")) {
            statement.setString(1, riderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? toRider(resultSet) : null;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not find rider", exception);
        }
    }

    public CustomDynamicArray<Rider> findAll() {
        CustomDynamicArray<Rider> riders = new CustomDynamicArray<>();
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM riders ORDER BY rider_id");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                riders.add(toRider(resultSet));
            }
            return riders;
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not list riders", exception);
        }
    }

    public void updateAvailability(String riderId, boolean available) {
        String sql = "UPDATE riders SET available = ? WHERE rider_id = ?";
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, available ? 1 : 0);
            statement.setString(2, riderId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not update rider availability", exception);
        }
    }

    public int count() {
        return RepositorySupport.count(connectionFactory, "riders");
    }

    private static Rider toRider(ResultSet resultSet) throws SQLException {
        return new Rider(
                resultSet.getString("rider_id"),
                resultSet.getString("name"),
                resultSet.getString("current_location_id"),
                VehicleType.valueOf(resultSet.getString("vehicle_type")),
                resultSet.getDouble("carrying_capacity"),
                resultSet.getInt("available") == 1);
    }
}
