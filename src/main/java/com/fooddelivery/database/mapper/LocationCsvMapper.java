package com.fooddelivery.database.mapper;

import com.fooddelivery.database.csv.CsvRecord;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.LocationType;

public class LocationCsvMapper {
    public Location map(CsvRecord record) {
        return new Location(
                required(record, "location_id", "locationId"),
                required(record, "name"),
                required(record, "area"),
                mapType(required(record, "type")),
                parseDouble(record, "latitude"),
                parseDouble(record, "longitude"));
    }

    static LocationType mapType(String value) {
        return switch (value) {
            case "CAMPUS" -> LocationType.CAMPUS;
            case "HOSTEL_HALL" -> LocationType.HOSTEL_HALL;
            case "JUNCTION" -> LocationType.JUNCTION;
            case "LANDMARK" -> LocationType.LANDMARK;
            case "MARKET" -> LocationType.MARKET;
            case "RESIDENTIAL_AREA" -> LocationType.RESIDENTIAL_AREA;
            case "RESTAURANT" -> LocationType.RESTAURANT;
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

    static String required(CsvRecord record, String preferredHeader, String fallbackHeader) {
        if (record.hasHeader(preferredHeader)) {
            String value = record.get(preferredHeader);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return required(record, fallbackHeader);
    }

    static double parseDouble(CsvRecord record, String header) {
        try {
            return Double.parseDouble(required(record, header));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid decimal " + header + " at row " + record.rowNumber(), exception);
        }
    }

    static double parseDouble(CsvRecord record, String preferredHeader, String fallbackHeader) {
        try {
            return Double.parseDouble(required(record, preferredHeader, fallbackHeader));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid decimal " + preferredHeader + " at row " + record.rowNumber(), exception);
        }
    }
}
