package com.fooddelivery.database;

public final class DatabaseSchema {
    private DatabaseSchema() {
    }

    public static final String CREATE_LOCATIONS = """
            CREATE TABLE IF NOT EXISTS locations (
                location_id TEXT PRIMARY KEY,
                name TEXT NOT NULL UNIQUE,
                area TEXT NOT NULL,
                type TEXT NOT NULL,
                latitude REAL NOT NULL,
                longitude REAL NOT NULL
            )
            """;

    public static final String CREATE_ROADS = """
            CREATE TABLE IF NOT EXISTS roads (
                road_id TEXT PRIMARY KEY,
                from_location_id TEXT NOT NULL,
                to_location_id TEXT NOT NULL,
                distance_km REAL NOT NULL,
                normal_travel_time_minutes REAL NOT NULL,
                road_condition_weight REAL NOT NULL,
                FOREIGN KEY (from_location_id) REFERENCES locations(location_id),
                FOREIGN KEY (to_location_id) REFERENCES locations(location_id)
            )
            """;

    public static final String CREATE_RIDERS = """
            CREATE TABLE IF NOT EXISTS riders (
                rider_id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                current_location_id TEXT NOT NULL,
                vehicle_type TEXT NOT NULL,
                carrying_capacity REAL NOT NULL,
                available INTEGER NOT NULL,
                FOREIGN KEY (current_location_id) REFERENCES locations(location_id)
            )
            """;

    public static final String CREATE_DELIVERY_REQUESTS = """
            CREATE TABLE IF NOT EXISTS delivery_requests (
                request_id TEXT PRIMARY KEY,
                source_location_id TEXT NOT NULL,
                destination_location_id TEXT NOT NULL,
                category TEXT NOT NULL,
                urgency INTEGER NOT NULL,
                capacity_required REAL NOT NULL,
                time_submitted TEXT NOT NULL,
                deadline TEXT NOT NULL,
                status TEXT NOT NULL,
                priority_score REAL NOT NULL,
                FOREIGN KEY (source_location_id) REFERENCES locations(location_id),
                FOREIGN KEY (destination_location_id) REFERENCES locations(location_id)
            )
            """;
}
