package com.fooddelivery.database.mapper;

import com.fooddelivery.database.csv.CsvRecord;
import com.fooddelivery.datastructures.hashing.CustomMap;
import com.fooddelivery.model.Rider;
import com.fooddelivery.model.VehicleType;

public class RiderCsvMapper {
    private final CustomMap<String, String> locationIdByName;

    public RiderCsvMapper(CustomMap<String, String> locationIdByName) {
        if (locationIdByName == null) {
            throw new IllegalArgumentException("Location lookup must not be null");
        }
        this.locationIdByName = locationIdByName;
    }

    public Rider map(CsvRecord record) {
        String riderId = LocationCsvMapper.required(record, "resource_Id");
        String locationName = LocationCsvMapper.required(record, "homeLocation");
        String locationId = locationIdByName.get(locationName);
        if (locationId == null) {
            throw new IllegalArgumentException("Unknown rider home location at row " + record.rowNumber() + ": " + locationName);
        }
        return new Rider(
                riderId,
                "Rider " + riderId,
                locationId,
                mapType(LocationCsvMapper.required(record, "type")),
                LocationCsvMapper.parseDouble(record, "capacity"),
                mapAvailability(LocationCsvMapper.required(record, "available_Status")));
    }

    static VehicleType mapType(String value) {
        return switch (value) {
            case "Motorcycle" -> VehicleType.MOTORCYCLE;
            case "Car Delivery" -> VehicleType.CAR;
            case "Cargo" -> VehicleType.CARGO_VEHICLE;
            default -> throw new IllegalArgumentException("Unknown resource type: " + value);
        };
    }

    static boolean mapAvailability(String value) {
        return switch (value) {
            case "Available" -> true;
            case "In Use", "Under Maintenance" -> false;
            default -> throw new IllegalArgumentException("Unknown availability status: " + value);
        };
    }
}
