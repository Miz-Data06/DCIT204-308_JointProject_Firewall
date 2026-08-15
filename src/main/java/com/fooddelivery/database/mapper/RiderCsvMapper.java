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
        String riderId = LocationCsvMapper.required(record, "resource_id", "resource_Id");
        String locationId = record.hasHeader("home_location_id") ? record.get("home_location_id") : null;
        if (locationId == null || locationId.isBlank()) {
            String locationName = LocationCsvMapper.required(record, "homeLocation");
            locationId = locationIdByName.get(locationName);
            if (locationId == null) {
                throw new IllegalArgumentException("Unknown rider home location at row " + record.rowNumber() + ": " + locationName);
            }
        }
        if (locationId == null) {
            throw new IllegalArgumentException("Missing rider home location at row " + record.rowNumber());
        }
        return new Rider(
                riderId,
                "Rider " + riderId,
                locationId,
                mapType(LocationCsvMapper.required(record, "type")),
                LocationCsvMapper.parseDouble(record, "capacity"),
                mapAvailability(LocationCsvMapper.required(record, "availability_status", "available_Status")));
    }

    static VehicleType mapType(String value) {
        return switch (value) {
            case "MOTORCYCLE" -> VehicleType.MOTORCYCLE;
            case "CAR" -> VehicleType.CAR;
            case "CARGO_VEHICLE" -> VehicleType.CARGO_VEHICLE;
            case "Motorcycle" -> VehicleType.MOTORCYCLE;
            case "Car Delivery" -> VehicleType.CAR;
            case "Cargo" -> VehicleType.CARGO_VEHICLE;
            default -> throw new IllegalArgumentException("Unknown resource type: " + value);
        };
    }

    static boolean mapAvailability(String value) {
        return switch (value) {
            case "AVAILABLE" -> true;
            case "IN_USE", "UNDER_MAINTENANCE" -> false;
            case "Available" -> true;
            case "In Use", "Under Maintenance" -> false;
            default -> throw new IllegalArgumentException("Unknown availability status: " + value);
        };
    }
}
