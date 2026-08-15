package com.fooddelivery.database.mapper;

import com.fooddelivery.database.csv.CsvRecord;
import com.fooddelivery.model.Road;

public class RoadCsvMapper {
    public Road map(CsvRecord record, int rowIndex) {
        if (rowIndex <= 0) {
            throw new IllegalArgumentException("Road row index must be positive");
        }
        String roadId = record.hasHeader("road_id") ? record.get("road_id") : null;
        if (roadId == null || roadId.isBlank()) {
            roadId = String.format("ROAD%03d", rowIndex);
        }
        return new Road(
                roadId,
                LocationCsvMapper.required(record, "from_location_id", "fromLocationId"),
                LocationCsvMapper.required(record, "to_location_id", "toLocationId"),
                LocationCsvMapper.parseDouble(record, "distance_km", "distance"),
                LocationCsvMapper.parseDouble(record, "base_travel_time_minutes", "travel_Time"),
                roadConditionMultiplier(record));
    }

    private static double roadConditionMultiplier(CsvRecord record) {
        String multiplier = record.hasHeader("road_condition_multiplier") ? record.get("road_condition_multiplier") : null;
        if (multiplier != null && !multiplier.isBlank()) {
            return Double.parseDouble(multiplier);
        }
        return LocationCsvMapper.parseDouble(record, "roadConditionWeight");
    }
}
