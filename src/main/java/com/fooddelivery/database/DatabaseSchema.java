package com.fooddelivery.database;

/**
 * Canonical application schema, mirrored by database/schema.sql.
 * SchemaCompatibilityTest verifies that both initialization paths create the same schema.
 */
public final class DatabaseSchema {
    private DatabaseSchema() {
    }

    public static final String CREATE_LOCATIONS = """
            CREATE TABLE IF NOT EXISTS locations (
                location_id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL UNIQUE,
                area TEXT NOT NULL,
                type TEXT NOT NULL CHECK (type IN
                    ('CAMPUS', 'HOSTEL_HALL', 'JUNCTION', 'LANDMARK', 'MARKET', 'RESIDENTIAL_AREA', 'RESTAURANT')),
                latitude REAL NOT NULL CHECK (latitude BETWEEN -90.0 AND 90.0),
                longitude REAL NOT NULL CHECK (longitude BETWEEN -180.0 AND 180.0)
            )
            """;

    public static final String CREATE_ROADS = """
            CREATE TABLE IF NOT EXISTS roads (
                road_id TEXT NOT NULL PRIMARY KEY,
                from_location_id TEXT NOT NULL,
                to_location_id TEXT NOT NULL,
                distance_km REAL NOT NULL CHECK (distance_km > 0.0),
                normal_travel_time_minutes REAL NOT NULL CHECK (normal_travel_time_minutes > 0.0),
                road_condition_weight REAL NOT NULL CHECK (road_condition_weight > 0.0),
                FOREIGN KEY (from_location_id) REFERENCES locations(location_id),
                FOREIGN KEY (to_location_id) REFERENCES locations(location_id),
                CHECK (from_location_id <> to_location_id)
            )
            """;

    public static final String CREATE_RIDERS = """
            CREATE TABLE IF NOT EXISTS riders (
                rider_id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                current_location_id TEXT NOT NULL,
                vehicle_type TEXT NOT NULL CHECK (vehicle_type IN ('MOTORCYCLE', 'CAR', 'CARGO_VEHICLE')),
                carrying_capacity REAL NOT NULL CHECK (carrying_capacity > 0.0),
                available INTEGER NOT NULL CHECK (available IN (0, 1)),
                FOREIGN KEY (current_location_id) REFERENCES locations(location_id)
            )
            """;

    public static final String CREATE_DELIVERY_REQUESTS = """
            CREATE TABLE IF NOT EXISTS delivery_requests (
                request_id TEXT NOT NULL PRIMARY KEY,
                source_location_id TEXT NOT NULL,
                destination_location_id TEXT NOT NULL,
                category TEXT NOT NULL,
                urgency INTEGER NOT NULL CHECK (urgency > 0),
                capacity_required REAL NOT NULL CHECK (capacity_required > 0.0),
                time_submitted TEXT NOT NULL,
                deadline TEXT NOT NULL,
                status TEXT NOT NULL CHECK (status IN ('PENDING', 'ASSIGNED', 'PICKED_UP', 'DELIVERED', 'CANCELLED')),
                priority_score REAL NOT NULL,
                FOREIGN KEY (source_location_id) REFERENCES locations(location_id),
                FOREIGN KEY (destination_location_id) REFERENCES locations(location_id),
                CHECK (source_location_id <> destination_location_id)
            )
            """;

    public static final String CREATE_ALGORITHM_RUNS = """
            CREATE TABLE IF NOT EXISTS algorithm_runs (
                run_id TEXT NOT NULL PRIMARY KEY,
                algorithm_name TEXT NOT NULL,
                input_size INTEGER NOT NULL,
                run_number INTEGER NOT NULL,
                runtime_ns INTEGER NOT NULL,
                memory_kb REAL NOT NULL,
                executed_at TEXT NOT NULL,
                CHECK (input_size >= 0),
                CHECK (run_number > 0),
                CHECK (runtime_ns >= 0),
                CHECK (memory_kb >= 0.0)
            )
            """;

    public static final String CREATE_AUDIT_EVENTS = """
            CREATE TABLE IF NOT EXISTS audit_events (
                audit_id TEXT NOT NULL PRIMARY KEY,
                entity_type TEXT NOT NULL,
                entity_id TEXT NOT NULL,
                action TEXT NOT NULL,
                occurred_at TEXT NOT NULL,
                performed_by TEXT NOT NULL,
                details TEXT NOT NULL
            )
            """;

    static final String[] CREATE_INDEXES = {
            "CREATE INDEX IF NOT EXISTS idx_locations_type ON locations(type)",
            "CREATE INDEX IF NOT EXISTS idx_locations_name ON locations(name)",
            "CREATE INDEX IF NOT EXISTS idx_roads_from_location ON roads(from_location_id)",
            "CREATE INDEX IF NOT EXISTS idx_roads_to_location ON roads(to_location_id)",
            "CREATE INDEX IF NOT EXISTS idx_delivery_requests_source ON delivery_requests(source_location_id)",
            "CREATE INDEX IF NOT EXISTS idx_delivery_requests_destination ON delivery_requests(destination_location_id)",
            "CREATE INDEX IF NOT EXISTS idx_delivery_requests_status ON delivery_requests(status)",
            "CREATE INDEX IF NOT EXISTS idx_delivery_requests_urgency ON delivery_requests(urgency)",
            "CREATE INDEX IF NOT EXISTS idx_riders_current_location ON riders(current_location_id)",
            "CREATE INDEX IF NOT EXISTS idx_riders_available ON riders(available)",
            "CREATE INDEX IF NOT EXISTS idx_algorithm_runs_name ON algorithm_runs(algorithm_name)",
            "CREATE INDEX IF NOT EXISTS idx_algorithm_runs_input ON algorithm_runs(input_size)",
            "CREATE INDEX IF NOT EXISTS idx_audit_events_entity ON audit_events(entity_type, entity_id)",
            "CREATE INDEX IF NOT EXISTS idx_audit_events_action ON audit_events(action)"
    };
}
