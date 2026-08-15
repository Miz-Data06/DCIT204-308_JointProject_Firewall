package com.fooddelivery.database.mapper;

import com.fooddelivery.database.csv.CsvRecord;
import com.fooddelivery.model.Road;

public class RoadCsvMapper {
    public Road map(CsvRecord record, int rowIndex) {
        if (rowIndex <= 0) {
            throw new IllegalArgumentException("Road row index must be positive");
        }
        return new Road(
                String.format("ROAD%03d", rowIndex),
                LocationCsvMapper.required(record, "fromLocationId"),
                LocationCsvMapper.required(record, "toLocationId"),
                LocationCsvMapper.parseDouble(record, "distance"),
                LocationCsvMapper.parseDouble(record, "travel_Time"),
                LocationCsvMapper.parseDouble(record, "roadConditionWeight"));
    }
}
