package com.fooddelivery.database.mapper;

import com.fooddelivery.database.csv.CsvRecord;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.LocationType;

public class LocationCsvMapper {
    public Location map(CsvRecord record) {
        return new Location(
                required(record, "locationId"),
                required(record, "name"),
                required(record, "area"),
                mapType(required(record, "type")),
                parseDouble(record, "latitude"),
                parseDouble(record, "longitude"));
    }

    static LocationType mapType(String value) {
        return switch (value) {
            case "Campus" -> LocationType.CAMPUS;
            case "Hostel/Hall" -> LocationType.HOSTEL_HALL;
            case "Junction" -> LocationType.JUNCTION;
            case "Landmark" -> LocationType.LANDMARK;
            case "Market" -> LocationType.MARKET;
            case "Residential Area" -> LocationType.RESIDENTIAL_AREA;
            case "Restaurant" -> LocationType.RESTAURANT;
            default -> throw new IllegalArgumentException("Unknown location type: " + value);
        };
    }

    static String required(CsvRecord record, String header) {
        String value = record.get(header);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing " + header + " at row " + record.rowNumber());
        }
        return value;
    }

    static double parseDouble(CsvRecord record, String header) {
        try {
            return Double.parseDouble(required(record, header));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid decimal " + header + " at row " + record.rowNumber(), exception);
        }
    }
}
