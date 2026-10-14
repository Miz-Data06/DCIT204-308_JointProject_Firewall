package com.fooddelivery.database;

import java.nio.file.Path;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fooddelivery.database.mapper.DatasetLoadResult;
import com.fooddelivery.database.mapper.DatasetLoader;
import com.fooddelivery.database.repository.DeliveryRequestRepository;
import com.fooddelivery.database.repository.LocationRepository;
import com.fooddelivery.database.repository.RiderRepository;
import com.fooddelivery.database.repository.RoadRepository;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.gui.GuiApplicationService;
import com.fooddelivery.gui.GuiOrderResult;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.LocationType;
import com.fooddelivery.model.RequestStatus;
import com.fooddelivery.model.Rider;
import com.fooddelivery.model.Road;
import com.fooddelivery.model.VehicleType;

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

    @Test
    void guiServicePersistsOrdersAndLocksAssignedRiderAvailability() {
        CustomDatasetFixture fixture = new CustomDatasetFixture();
        new DatasetDatabaseImporter(connectionFactory).importDataset(fixture.dataset());

        GuiApplicationService service = new GuiApplicationService(fixture.dataset(), connectionFactory);
        GuiOrderResult result = service.placeOrder("LOC001", "LOC003", "Food Delivery", "HIGH", "1.0");

        assertEquals("GUI001", result.getRequestId());
        assertEquals(1, requestRepository.count());
        assertEquals(RequestStatus.ASSIGNED, requestRepository.findById("GUI001").getStatus());
        assertEquals(Boolean.FALSE, riderRepository.findById("RES001").isAvailable());
    }

    @Test
    void restartPersistsOrdersAndKeepsUniqueRequestIds() {
        DatabaseConnectionFactory restartFactory = new DatabaseConnectionFactory(
                new DatabaseConfig(tempDirectory.resolve("restart.db")));
        new DatabaseInitializer(restartFactory).initialize();

        CustomDatasetFixture fixture = new CustomDatasetFixture();
        new DatasetDatabaseImporter(restartFactory).importDataset(fixture.dataset());

        GuiApplicationService first = new GuiApplicationService(fixture.dataset(), restartFactory);
        GuiOrderResult firstOrder = first.placeOrder("LOC001", "LOC003", "Food Delivery", "HIGH", "1.0");

        DatasetLoadResult restartedDataset = snapshotDataset(restartFactory);
        GuiApplicationService restarted = new GuiApplicationService(restartedDataset, restartFactory);
        GuiOrderResult secondOrder = restarted.placeOrder("LOC001", "LOC002", "Food Delivery", "HIGH", "1.0");

        assertEquals("GUI001", firstOrder.getRequestId());
        assertEquals("GUI002", secondOrder.getRequestId());
        assertNotNull(new DeliveryRequestRepository(restartFactory).findById("GUI001"));
        assertNotNull(new DeliveryRequestRepository(restartFactory).findById("GUI002"));
        assertEquals(Boolean.FALSE, new RiderRepository(restartFactory).findById("RES001").isAvailable());
    }

    @Test
    void secondOrderCannotReserveSameUnavailableRider() {
        DatabaseConnectionFactory restartFactory = new DatabaseConnectionFactory(
                new DatabaseConfig(tempDirectory.resolve("unavailable-rider.db")));
        new DatabaseInitializer(restartFactory).initialize();

        CustomDatasetFixture fixture = new CustomDatasetFixture();
        new DatasetDatabaseImporter(restartFactory).importDataset(fixture.dataset());

        GuiApplicationService service = new GuiApplicationService(fixture.dataset(), restartFactory);
        GuiOrderResult firstOrder = service.placeOrder("LOC001", "LOC003", "Food Delivery", "HIGH", "1.0");
        assertEquals("GUI001", firstOrder.getRequestId());

        DatasetLoadResult restartedDataset = snapshotDataset(restartFactory);
        GuiApplicationService restarted = new GuiApplicationService(restartedDataset, restartFactory);
        GuiOrderResult secondOrder = restarted.placeOrder("LOC001", "LOC002", "Food Delivery", "MEDIUM", "1.0");

        assertEquals(false, secondOrder.isRiderAssigned());
        assertEquals(Boolean.FALSE, new RiderRepository(restartFactory).findById("RES001").isAvailable());
    }

    @Test
    void csvImportDoesNotOverwritePersistedGuiOrders() {
        CustomDatasetFixture fixture = new CustomDatasetFixture();
        new DatasetDatabaseImporter(connectionFactory).importDataset(fixture.dataset());

        GuiApplicationService service = new GuiApplicationService(fixture.dataset(), connectionFactory);
        service.placeOrder("LOC001", "LOC003", "Food Delivery", "HIGH", "1.0");

        new DatasetDatabaseImporter(connectionFactory).importDataset(new DatasetLoader().load(Path.of("data")));

        DeliveryRequest persisted = requestRepository.findById("GUI001");
        assertNotNull(persisted);
        assertEquals("GUI001", persisted.getRequestId());
    }

    @Test
    void atomicAssignmentCommitsOrderAndReservationTogether() {
        CustomDatasetFixture fixture = new CustomDatasetFixture();
        new DatasetDatabaseImporter(connectionFactory).importDataset(fixture.dataset());

        GuiApplicationService service = new GuiApplicationService(fixture.dataset(), connectionFactory);
        GuiOrderResult result = service.placeOrder("LOC001", "LOC003", "Food Delivery", "HIGH", "1.0");

        assertEquals("GUI001", result.getRequestId());
        assertEquals(1, requestRepository.count());
        assertEquals(RequestStatus.ASSIGNED, requestRepository.findById("GUI001").getStatus());
        assertFalse(riderRepository.findById("RES001").isAvailable());
    }

    @Test
    void failedRiderReservationRollsBackOrderInsertion() {
        CustomDatasetFixture fixture = new CustomDatasetFixture();
        new DatasetDatabaseImporter(connectionFactory).importDataset(fixture.dataset());
        riderRepository.updateAvailability("RES001", false);

        GuiApplicationService service = new GuiApplicationService(fixture.dataset(), connectionFactory);
        GuiOrderResult result = service.placeOrder("LOC001", "LOC003", "Food Delivery", "HIGH", "1.0");

        assertFalse(result.isRiderAssigned());
        assertEquals(0, requestRepository.count());
        assertFalse(riderRepository.findById("RES001").isAvailable());
    }

    @Test
    void txnFailureToInsertRequestDoesNotReserveRider() throws Exception {
        DatabaseConnectionFactory failureFactory = new DatabaseConnectionFactory(
                new DatabaseConfig(tempDirectory.resolve("transaction-failure.db")));
        new DatabaseInitializer(failureFactory).initialize();

        LocationRepository locationRepository = new LocationRepository(failureFactory);
        RoadRepository roadRepository = new RoadRepository(failureFactory);
        RiderRepository riderRepository = new RiderRepository(failureFactory);
        DeliveryRequestRepository requestRepository = new DeliveryRequestRepository(failureFactory);

        locationRepository.save(new Location("LOC001", "Restaurant A", "Area", LocationType.RESTAURANT, 5.0, -0.1));
        locationRepository.save(new Location("LOC002", "Hostel B", "Area", LocationType.HOSTEL_HALL, 5.1, -0.2));
        locationRepository.save(new Location("LOC003", "Campus C", "Area", LocationType.CAMPUS, 5.2, -0.3));
        roadRepository.save(new Road("ROAD001", "LOC001", "LOC002", 1.0, 5.0, 1.0));
        roadRepository.save(new Road("ROAD002", "LOC002", "LOC003", 2.0, 10.0, 1.0));
        roadRepository.save(new Road("ROAD003", "LOC003", "LOC001", 2.5, 8.0, 1.0));
        riderRepository.save(new Rider("RES001", "Rider One", "LOC003", VehicleType.MOTORCYCLE, 3.0, true));

        try (Connection connection = failureFactory.openConnection()) {
            connection.setAutoCommit(false);
            DeliveryRequest request = new DeliveryRequest(
                    "GUI001",
                    "LOC001",
                    "LOC003",
                    "Food Delivery",
                    3,
                    1.0,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusHours(1),
                    RequestStatus.PENDING,
                    0.75);

            requestRepository.saveInTransaction(connection, request);
            assertTrue(riderRepository.reserveIfAvailable(connection, "RES001"));
            connection.rollback();
        }

        assertEquals(0, requestRepository.count());
        assertTrue(riderRepository.findById("RES001").isAvailable());
    }

    @Test
    void availableRiderCannotBeReservedTwice() {
        CustomDatasetFixture fixture = new CustomDatasetFixture();
        new DatasetDatabaseImporter(connectionFactory).importDataset(fixture.dataset());

        RiderRepository repository = new RiderRepository(connectionFactory);
        assertTrue(repository.reserveIfAvailable("RES001"));
        assertFalse(repository.reserveIfAvailable("RES001"));
        assertFalse(repository.findById("RES001").isAvailable());
    }

    @Test
    void concurrentReservationAttemptsOnlyOneCanReserveTheSameRider() throws Exception {
        CustomDatasetFixture fixture = new CustomDatasetFixture();
        new DatasetDatabaseImporter(connectionFactory).importDataset(fixture.dataset());

        RiderRepository repository = new RiderRepository(connectionFactory);
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            futures.add(executor.submit(() -> {
                startLatch.await();
                return repository.reserveIfAvailable("RES001");
            }));
        }

        startLatch.countDown();
        int successes = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) {
                successes++;
            }
        }
        executor.shutdown();

        assertEquals(1, successes);
        assertFalse(repository.findById("RES001").isAvailable());
    }

    private void saveTwoLocations() {
        locationRepository.save(new Location("LOC001", "Pizza King", "Greater Accra", LocationType.RESTAURANT, 5.6, -0.1));
        locationRepository.save(new Location("LOC002", "Upsa", "Greater Accra", LocationType.CAMPUS, 5.7, -0.2));
    }

    private static DatasetLoadResult snapshotDataset(DatabaseConnectionFactory factory) {
        return new DatasetLoadResult(
                new LocationRepository(factory).findAll(),
                new RoadRepository(factory).findAll(),
                new DeliveryRequestRepository(factory).findAll(),
                new RiderRepository(factory).findAll());
    }

    private static final class CustomDatasetFixture {
        private final DatasetLoadResult dataset;

        private CustomDatasetFixture() {
            CustomDynamicArray<Location> locations = new CustomDynamicArray<>();
            locations.add(new Location("LOC001", "Restaurant A", "Area", LocationType.RESTAURANT, 5.0, -0.1));
            locations.add(new Location("LOC002", "Hostel B", "Area", LocationType.HOSTEL_HALL, 5.1, -0.2));
            locations.add(new Location("LOC003", "Campus C", "Area", LocationType.CAMPUS, 5.2, -0.3));

            CustomDynamicArray<Road> roads = new CustomDynamicArray<>();
            roads.add(new Road("ROAD001", "LOC001", "LOC002", 1.0, 5.0, 1.0));
            roads.add(new Road("ROAD002", "LOC002", "LOC003", 2.0, 10.0, 1.0));
            roads.add(new Road("ROAD003", "LOC003", "LOC001", 2.5, 8.0, 1.0));

            CustomDynamicArray<DeliveryRequest> requests = new CustomDynamicArray<>();
            CustomDynamicArray<Rider> riders = new CustomDynamicArray<>();
            riders.add(new Rider("RES001", "Rider One", "LOC003", VehicleType.MOTORCYCLE, 3.0, true));

            this.dataset = new DatasetLoadResult(locations, roads, requests, riders);
        }

        private DatasetLoadResult dataset() {
            return dataset;
        }
    }
}
