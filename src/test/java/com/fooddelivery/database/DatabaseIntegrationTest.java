package com.fooddelivery.database;

import com.fooddelivery.database.mapper.DatasetLoadResult;
import com.fooddelivery.database.mapper.DatasetLoader;
import com.fooddelivery.database.repository.DeliveryRequestRepository;
import com.fooddelivery.database.repository.LocationRepository;
import com.fooddelivery.database.repository.RiderRepository;
import com.fooddelivery.database.repository.RoadRepository;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.LocationType;
import com.fooddelivery.model.RequestStatus;
import com.fooddelivery.model.Rider;
import com.fooddelivery.model.Road;
import com.fooddelivery.model.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseIntegrationTest {
    @TempDir
    Path tempDirectory;

    private DatabaseConnectionFactory connectionFactory;
    private LocationRepository locationRepository;
    private RoadRepository roadRepository;
    private RiderRepository riderRepository;
    private DeliveryRequestRepository requestRepository;

    @BeforeEach
    void setUp() {
        connectionFactory = new DatabaseConnectionFactory(new DatabaseConfig(tempDirectory.resolve("test.db")));
        new DatabaseInitializer(connectionFactory).initialize();
        locationRepository = new LocationRepository(connectionFactory);
        roadRepository = new RoadRepository(connectionFactory);
        riderRepository = new RiderRepository(connectionFactory);
        requestRepository = new DeliveryRequestRepository(connectionFactory);
    }

    @Test
    void createsSchemaAndRoundTripsEveryCoreModel() {
        saveTwoLocations();
        Road road = new Road("ROAD001", "LOC001", "LOC002", 1.5, 4.0, 1.25);
        Rider rider = new Rider("RES001", "Rider RES001", "LOC001", VehicleType.MOTORCYCLE, 2.0, true);
        DeliveryRequest request = new DeliveryRequest(
                "SR001",
                "LOC001",
                "LOC002",
                "Food Delivery",
                3,
                1.0,
                LocalDateTime.of(2026, 1, 1, 8, 0),
                LocalDateTime.of(2026, 1, 1, 9, 0),
                RequestStatus.PENDING,
                0.75);

        roadRepository.save(road);
        riderRepository.save(rider);
        requestRepository.save(request);

        assertEquals(2, locationRepository.count());
        assertEquals(1, roadRepository.count());
        assertEquals(1, riderRepository.count());
        assertEquals(1, requestRepository.count());
        assertEquals(5.0, roadRepository.findById("ROAD001").getEffectiveTime());
        assertEquals(VehicleType.MOTORCYCLE, riderRepository.findById("RES001").getVehicleType());
        assertEquals(RequestStatus.PENDING, requestRepository.findById("SR001").getStatus());
    }

    @Test
    void duplicatePrimaryKeyReplacesExistingRow() {
        locationRepository.save(new Location("LOC001", "First", "Area", LocationType.CAMPUS, 5.0, -0.1));
        locationRepository.save(new Location("LOC001", "Second", "Area", LocationType.MARKET, 5.1, -0.2));

        Location saved = locationRepository.findById("LOC001");

        assertEquals(1, locationRepository.count());
        assertEquals("Second", saved.getName());
        assertEquals(LocationType.MARKET, saved.getType());
    }

    @Test
    void rejectsMissingForeignKeyWherePractical() {
        Road road = new Road("ROAD999", "LOC404", "LOC405", 1.0, 2.0, 1.0);

        assertThrows(IllegalStateException.class, () -> roadRepository.save(road));
    }

    @Test
    void importsDatasetIntoDatabaseWithExpectedCounts() {
        DatasetLoadResult dataset = new DatasetLoader().load(Path.of("data"));

        new DatasetDatabaseImporter(connectionFactory).importDataset(dataset);

        assertEquals(150, locationRepository.count());
        assertEquals(300, roadRepository.count());
        assertEquals(90, riderRepository.count());
        assertEquals(900, requestRepository.count());
        assertNotNull(locationRepository.findById("LOC001"));
        assertNotNull(roadRepository.findById("ROAD001"));
        assertNotNull(riderRepository.findById("RES001"));
        assertNotNull(requestRepository.findById("SR001"));
    }

    private void saveTwoLocations() {
        locationRepository.save(new Location("LOC001", "Pizza King", "Greater Accra", LocationType.RESTAURANT, 5.6, -0.1));
        locationRepository.save(new Location("LOC002", "Upsa", "Greater Accra", LocationType.CAMPUS, 5.7, -0.2));
    }
}
