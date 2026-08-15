-- Official SQLite schema for the DCIT 204/308 Food Delivery System.
-- Enable foreign keys in every SQLite session before using this schema:
-- PRAGMA foreign_keys = ON;

PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS locations (
    location_id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    area TEXT NOT NULL,
    type TEXT NOT NULL CHECK (
        type IN (
            'CAMPUS',
            'HOSTEL_HALL',
            'JUNCTION',
            'LANDMARK',
            'MARKET',
            'RESIDENTIAL_AREA',
            'RESTAURANT'
        )
    ),
    latitude REAL NOT NULL CHECK (latitude BETWEEN -90.0 AND 90.0),
    longitude REAL NOT NULL CHECK (longitude BETWEEN -180.0 AND 180.0)
);

CREATE TABLE IF NOT EXISTS roads (
    road_id TEXT PRIMARY KEY,
    from_location_id TEXT NOT NULL,
    to_location_id TEXT NOT NULL,
    distance_km REAL NOT NULL CHECK (distance_km > 0.0),
    base_travel_time_minutes REAL NOT NULL CHECK (base_travel_time_minutes > 0.0),
    road_condition TEXT NOT NULL CHECK (
        road_condition IN ('VERY_GOOD', 'GOOD', 'FAIR', 'POOR', 'VERY_POOR')
    ),
    road_condition_multiplier REAL NOT NULL CHECK (road_condition_multiplier > 0.0),
    FOREIGN KEY (from_location_id) REFERENCES locations(location_id),
    FOREIGN KEY (to_location_id) REFERENCES locations(location_id),
    CHECK (from_location_id <> to_location_id)
);

CREATE TABLE IF NOT EXISTS service_requests (
    request_id TEXT PRIMARY KEY,
    source_location_id TEXT NOT NULL,
    destination_location_id TEXT NOT NULL,
    category TEXT NOT NULL,
    urgency TEXT NOT NULL CHECK (urgency IN ('LOW', 'MEDIUM', 'HIGH')),
    time_submitted TEXT NOT NULL,
    deadline TEXT NOT NULL,
    status TEXT NOT NULL CHECK (
        status IN ('PENDING', 'ASSIGNED', 'IN_TRANSIT', 'COMPLETED', 'CANCELLED')
    ),
    capacity_required REAL NOT NULL CHECK (capacity_required > 0.0),
    FOREIGN KEY (source_location_id) REFERENCES locations(location_id),
    FOREIGN KEY (destination_location_id) REFERENCES locations(location_id),
    CHECK (source_location_id <> destination_location_id)
);

CREATE TABLE IF NOT EXISTS resources (
    resource_id TEXT PRIMARY KEY,
    type TEXT NOT NULL CHECK (type IN ('MOTORCYCLE', 'CAR', 'CARGO_VEHICLE')),
    home_location_id TEXT NOT NULL,
    capacity REAL NOT NULL CHECK (capacity > 0.0),
    availability_status TEXT NOT NULL CHECK (
        availability_status IN ('AVAILABLE', 'IN_USE', 'UNDER_MAINTENANCE')
    ),
    FOREIGN KEY (home_location_id) REFERENCES locations(location_id)
);

CREATE TABLE IF NOT EXISTS algorithm_runs (
    run_id TEXT PRIMARY KEY,
    algorithm_name TEXT NOT NULL,
    input_size INTEGER NOT NULL CHECK (input_size >= 0),
    run_number INTEGER NOT NULL CHECK (run_number > 0),
    runtime_ns INTEGER NOT NULL CHECK (runtime_ns >= 0),
    memory_kb REAL NOT NULL CHECK (memory_kb >= 0.0),
    executed_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS audit_events (
    audit_id TEXT PRIMARY KEY,
    entity_type TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    action TEXT NOT NULL,
    occurred_at TEXT NOT NULL,
    performed_by TEXT NOT NULL,
    details TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_locations_type ON locations(type);
CREATE INDEX IF NOT EXISTS idx_locations_name ON locations(name);

CREATE INDEX IF NOT EXISTS idx_roads_from_location ON roads(from_location_id);
CREATE INDEX IF NOT EXISTS idx_roads_to_location ON roads(to_location_id);
CREATE INDEX IF NOT EXISTS idx_roads_condition ON roads(road_condition);

CREATE INDEX IF NOT EXISTS idx_service_requests_source ON service_requests(source_location_id);
CREATE INDEX IF NOT EXISTS idx_service_requests_destination ON service_requests(destination_location_id);
CREATE INDEX IF NOT EXISTS idx_service_requests_status ON service_requests(status);
CREATE INDEX IF NOT EXISTS idx_service_requests_urgency ON service_requests(urgency);

CREATE INDEX IF NOT EXISTS idx_resources_home_location ON resources(home_location_id);
CREATE INDEX IF NOT EXISTS idx_resources_availability ON resources(availability_status);

CREATE INDEX IF NOT EXISTS idx_algorithm_runs_name ON algorithm_runs(algorithm_name);
CREATE INDEX IF NOT EXISTS idx_algorithm_runs_input ON algorithm_runs(input_size);

CREATE INDEX IF NOT EXISTS idx_audit_events_entity ON audit_events(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_events_action ON audit_events(action);
