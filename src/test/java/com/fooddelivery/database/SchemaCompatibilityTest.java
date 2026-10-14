package com.fooddelivery.database;

import com.fooddelivery.database.mapper.DatasetLoadResult;
import com.fooddelivery.database.mapper.DatasetLoader;
import com.fooddelivery.database.repository.AlgorithmRunRepository;
import com.fooddelivery.database.repository.AuditEventRepository;
import com.fooddelivery.database.repository.DeliveryRequestRepository;
import com.fooddelivery.database.repository.LocationRepository;
import com.fooddelivery.database.repository.RiderRepository;
import com.fooddelivery.database.repository.RoadRepository;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.LocationType;
import com.fooddelivery.model.RequestStatus;
import com.fooddelivery.model.Rider;
import com.fooddelivery.model.Road;
import com.fooddelivery.model.VehicleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaCompatibilityTest {
    @TempDir
    Path tempDirectory;

    @Test
    void submittedSqlSupportsRepositoryRoundTripsAndUpdatesWithoutJavaInitialization() throws Exception {
        DatabaseConnectionFactory factory = factory("submitted.db");
        initializeFromSubmittedSql(factory);
        saveFixtures(factory);

        LocationRepository locations = new LocationRepository(factory);
        RoadRepository roads = new RoadRepository(factory);
        RiderRepository riders = new RiderRepository(factory);
        DeliveryRequestRepository requests = new DeliveryRequestRepository(factory);
        AlgorithmRunRepository runs = new AlgorithmRunRepository(factory);
        AuditEventRepository events = new AuditEventRepository(factory);

        assertEquals(2, locations.count());
        assertEquals(2, locations.findAll().size());
        assertEquals("First", locations.findById("LOC001").getName());
        assertEquals(1, roads.count());
        assertEquals(1, roads.findAll().size());
        assertEquals(5.0, roads.findById("ROAD001").getEffectiveTime());
        assertEquals(1, riders.count());
        assertEquals(1, riders.findAll().size());
        assertEquals(VehicleType.CAR, riders.findById("RES001").getVehicleType());
        assertEquals(1, requests.count());
        assertEquals(1, requests.findAll().size());
        // These values are valid in the existing model even though the CSV uses urgency 1-3.
        assertEquals(4, requests.findById("SR001").getUrgency());
        assertEquals(-0.25, requests.findById("SR001").getPriorityScore());
        assertEquals(1, runs.count());
        assertEquals(1, runs.findAll().size());
        assertEquals("TEST_ONLY", runs.findById("TEST_RUN").getAlgorithmName());
        assertEquals(1, events.count());
        assertEquals(1, events.findAll().size());
        assertEquals("TEST_ONLY", events.findById("TEST_EVENT").getAction());
        assertNull(roads.findById("missing"));

        locations.save(new Location("LOC001", "Renamed", "Legon", LocationType.MARKET, 5.1, -0.2));
        roads.save(new Road("ROAD001", "LOC001", "LOC002", 2.0, 6.0, 1.5));
        Rider rider = riders.findById("RES001");
        rider.setAvailable(false);
        rider.setCurrentLocationId("LOC002");
        riders.save(rider);
        DeliveryRequest request = requests.findById("SR001");
        for (RequestStatus status : RequestStatus.values()) {
            request.setStatus(status);
            requests.save(request);
            assertEquals(status, requests.findById("SR001").getStatus());
        }
        runs.save(new AlgorithmRunRecord("TEST_RUN", "TEST_UPDATED", 0, 2, 0, 0.0, timestamp()));
        events.save(new AuditEventRecord("TEST_EVENT", "TEST_ONLY", "SR001", "TEST_UPDATED",
                timestamp(), "TEST_ONLY", "Isolated repository fixture"));

        assertEquals("Renamed", locations.findById("LOC001").getName());
        assertEquals(9.0, roads.findById("ROAD001").getEffectiveTime());
        assertFalse(riders.findById("RES001").isAvailable());
        assertEquals("LOC002", riders.findById("RES001").getCurrentLocationId());
        assertEquals(2, runs.findById("TEST_RUN").getRunNumber());
        assertEquals("TEST_UPDATED", events.findById("TEST_EVENT").getAction());
        assertEquals(2, locations.count());
        assertEquals(1, roads.count());
        assertEquals(1, riders.count());
        assertEquals(1, requests.count());
        assertEquals(1, runs.count());
        assertEquals(1, events.count());
        assertHealthy(factory);
    }

    @Test
    void submittedSqlImportsDatasetWithoutChangingMappedFieldsOrCreatingSupportRecords() throws Exception {
        DatabaseConnectionFactory factory = factory("import.db");
        initializeFromSubmittedSql(factory);
        DatasetLoadResult expected = new DatasetLoader().load(Path.of("data"));
        DatasetDatabaseImporter importer = new DatasetDatabaseImporter(factory);
        importer.importDataset(expected);

        LocationRepository locations = new LocationRepository(factory);
        RoadRepository roads = new RoadRepository(factory);
        DeliveryRequestRepository requests = new DeliveryRequestRepository(factory);
        RiderRepository riders = new RiderRepository(factory);
        assertEquals(150, locations.count());
        assertEquals(300, roads.count());
        assertEquals(900, requests.count());
        assertEquals(90, riders.count());
        assertRoundTrip(expected.getLocations(), locations.findAll(), location -> List.of(
                location.getLocationId(), location.getName(), location.getArea(), location.getType(),
                location.getLatitude(), location.getLongitude()));
        assertRoundTrip(expected.getRoads(), roads.findAll(), road -> List.of(
                road.getRoadId(), road.getFromLocationId(), road.getToLocationId(), road.getDistanceKm(),
                road.getNormalTravelTimeMinutes(), road.getRoadConditionWeight(), road.getEffectiveTime()));
        assertRoundTrip(expected.getDeliveryRequests(), requests.findAll(), request -> List.of(
                request.getRequestId(), request.getSourceLocationId(), request.getDestinationLocationId(),
                request.getCategory(), request.getUrgency(), request.getCapacityRequired(),
                request.getTimeSubmitted(), request.getDeadline(), request.getStatus(), request.getPriorityScore()));
        assertRoundTrip(expected.getRiders(), riders.findAll(), rider -> List.of(
                rider.getRiderId(), rider.getName(), rider.getCurrentLocationId(), rider.getVehicleType(),
                rider.getCarryingCapacity(), rider.isAvailable()));

        Map<String, String> importedRows = databaseRows(factory);
        // Runtime initialization and reimport must also work on a populated SQL-created database.
        new DatabaseInitializer(factory).initialize();
        importer.importDataset(expected);
        assertEquals(importedRows, databaseRows(factory));
        assertEquals(0, new AlgorithmRunRepository(factory).count());
        assertEquals(0, new AuditEventRepository(factory).count());
        assertHealthy(factory);
    }

    @Test
    void submittedSqlAndRuntimeCreateEquivalentSchemasInEitherOrder() throws Exception {
        DatabaseConnectionFactory submitted = factory("sql.db");
        DatabaseConnectionFactory runtime = factory("java.db");
        initializeFromSubmittedSql(submitted);
        new DatabaseInitializer(runtime).initialize();
        Map<String, String> schema = schemaSnapshot(submitted);
        assertEquals(20, schema.size()); // Six tables and fourteen explicit indexes.
        assertEquals(schema, schemaSnapshot(runtime));

        new DatabaseInitializer(submitted).initialize();
        initializeFromSubmittedSql(submitted);
        initializeFromSubmittedSql(runtime);
        new DatabaseInitializer(runtime).initialize();
        assertEquals(schema, schemaSnapshot(submitted));
        assertEquals(schema, schemaSnapshot(runtime));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void bothInitializationPathsEnforceForeignKeysAndModelCompatibleConstraints(boolean submittedSql) throws Exception {
        DatabaseConnectionFactory factory = factory("constraints.db");
        if (submittedSql) {
            initializeFromSubmittedSql(factory);
        } else {
            new DatabaseInitializer(factory).initialize();
        }
        saveFixtures(factory);
        try (Connection connection = factory.openConnection(); Statement statement = connection.createStatement()) {
            List<String> invalidUpdates = List.of(
                    "UPDATE locations SET latitude = 91",
                    "UPDATE locations SET longitude = -181",
                    "UPDATE locations SET type = 'UNKNOWN'",
                    "UPDATE locations SET name = 'First' WHERE location_id = 'LOC002'",
                    "UPDATE roads SET distance_km = 0",
                    "UPDATE roads SET normal_travel_time_minutes = -1",
                    "UPDATE roads SET road_condition_weight = 0",
                    "UPDATE roads SET to_location_id = from_location_id",
                    "UPDATE roads SET from_location_id = 'missing'",
                    "UPDATE roads SET to_location_id = 'missing'",
                    "UPDATE riders SET available = 2",
                    "UPDATE riders SET vehicle_type = 'UNKNOWN'",
                    "UPDATE riders SET carrying_capacity = 0",
                    "UPDATE riders SET current_location_id = 'missing'",
                    "UPDATE delivery_requests SET urgency = 0",
                    "UPDATE delivery_requests SET capacity_required = -1",
                    "UPDATE delivery_requests SET status = 'COMPLETED'",
                    "UPDATE delivery_requests SET destination_location_id = source_location_id",
                    "UPDATE delivery_requests SET source_location_id = 'missing'",
                    "UPDATE delivery_requests SET destination_location_id = 'missing'",
                    "UPDATE algorithm_runs SET run_id = NULL",
                    "UPDATE algorithm_runs SET input_size = -1",
                    "UPDATE algorithm_runs SET run_number = 0",
                    "UPDATE algorithm_runs SET runtime_ns = -1",
                    "UPDATE algorithm_runs SET memory_kb = -1",
                    "UPDATE audit_events SET audit_id = NULL");
            for (String sql : invalidUpdates) {
                assertThrows(SQLException.class, () -> statement.executeUpdate(sql), sql);
            }
        }
        assertHealthy(factory);
    }

    private DatabaseConnectionFactory factory(String filename) {
        return new DatabaseConnectionFactory(new DatabaseConfig(tempDirectory.resolve(filename)));
    }

    private static void initializeFromSubmittedSql(DatabaseConnectionFactory factory) throws Exception {
        String script = Files.readString(Path.of("database", "schema.sql"))
                .replaceAll("(?m)^\\s*--[^\\r\\n]*", "");
        try (Connection connection = factory.openConnection(); Statement statement = connection.createStatement()) {
            // The submitted DDL contains no triggers or semicolons inside string literals.
            for (String sql : script.split(";")) {
                if (!sql.isBlank()) {
                    statement.execute(sql);
                }
            }
        }
    }

    private static Map<String, String> schemaSnapshot(DatabaseConnectionFactory factory) throws SQLException {
        Map<String, String> schema = new TreeMap<>();
        try (Connection connection = factory.openConnection(); Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT type, name, sql FROM sqlite_master WHERE sql IS NOT NULL ORDER BY type, name")) {
            while (result.next()) {
                schema.put(result.getString("type") + ":" + result.getString("name"),
                        result.getString("sql").replaceAll("\\s+", " ").trim());
            }
        }
        return schema;
    }

    private static Map<String, String> databaseRows(DatabaseConnectionFactory factory) throws SQLException {
        Map<String, String> rows = new TreeMap<>();
        try (Connection connection = factory.openConnection(); Statement statement = connection.createStatement()) {
            for (String table : List.of("locations", "roads", "riders", "delivery_requests", "algorithm_runs", "audit_events")) {
                try (ResultSet result = statement.executeQuery("SELECT * FROM " + table + " ORDER BY 1")) {
                    while (result.next()) {
                        StringBuilder fields = new StringBuilder();
                        for (int column = 1; column <= result.getMetaData().getColumnCount(); column++) {
                            String value = result.getString(column);
                            fields.append(value.length()).append(':').append(value);
                        }
                        rows.put(table + ":" + result.getString(1), fields.toString());
                    }
                }
            }
        }
        return rows;
    }

    private static <T> void assertRoundTrip(CustomDynamicArray<T> expected, CustomDynamicArray<T> actual,
                                          Function<T, List<Object>> fields) {
        assertEquals(expected.size(), actual.size());
        Map<String, List<Object>> expectedFields = new TreeMap<>();
        Map<String, List<Object>> actualFields = new TreeMap<>();
        for (int i = 0; i < expected.size(); i++) {
            List<Object> row = fields.apply(expected.get(i));
            expectedFields.put((String) row.get(0), row);
        }
        for (int i = 0; i < actual.size(); i++) {
            List<Object> row = fields.apply(actual.get(i));
            actualFields.put((String) row.get(0), row);
        }
        assertEquals(expected.size(), expectedFields.size());
        assertEquals(actual.size(), actualFields.size());
        assertEquals(expectedFields, actualFields);
    }

    private static void saveFixtures(DatabaseConnectionFactory factory) {
        new LocationRepository(factory).save(new Location("LOC001", "First", "Legon", LocationType.CAMPUS, 5.0, -0.1));
        new LocationRepository(factory).save(new Location("LOC002", "Second", "Legon", LocationType.RESTAURANT, 5.1, -0.2));
        new RoadRepository(factory).save(new Road("ROAD001", "LOC001", "LOC002", 1.5, 4.0, 1.25));
        new RiderRepository(factory).save(new Rider("RES001", "Fixture Rider", "LOC001", VehicleType.CAR, 2.5, true));
        new DeliveryRequestRepository(factory).save(new DeliveryRequest("SR001", "LOC001", "LOC002", "Food",
                4, 1.25, timestamp(), timestamp().plusHours(1), RequestStatus.PICKED_UP, -0.25));
        // Isolated test fixtures only; no benchmark measurements or operational history are seeded.
        new AlgorithmRunRepository(factory).save(new AlgorithmRunRecord("TEST_RUN", "TEST_ONLY", 0, 1, 0, 0.0, timestamp()));
        new AuditEventRepository(factory).save(new AuditEventRecord("TEST_EVENT", "TEST_ONLY", "SR001", "TEST_ONLY",
                timestamp(), "TEST_ONLY", "Isolated repository fixture"));
    }

    private static LocalDateTime timestamp() {
        return LocalDateTime.of(2026, 1, 1, 8, 0);
    }

    private static void assertHealthy(DatabaseConnectionFactory factory) throws SQLException {
        try (Connection connection = factory.openConnection(); Statement statement = connection.createStatement()) {
            try (ResultSet result = statement.executeQuery("PRAGMA foreign_keys")) {
                assertTrue(result.next());
                assertEquals(1, result.getInt(1));
            }
            try (ResultSet result = statement.executeQuery("PRAGMA foreign_key_check")) {
                assertFalse(result.next());
            }
            try (ResultSet result = statement.executeQuery("PRAGMA integrity_check")) {
                assertTrue(result.next());
                assertEquals("ok", result.getString(1));
            }
        }
    }
}
