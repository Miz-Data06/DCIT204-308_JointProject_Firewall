package com.fooddelivery.gui;

import java.nio.file.Files;
import java.nio.file.Path;

import com.fooddelivery.database.DatabaseConfig;
import com.fooddelivery.database.DatabaseConnectionFactory;
import com.fooddelivery.database.DatabaseInitializer;
import com.fooddelivery.database.mapper.DatasetLoadResult;
import com.fooddelivery.database.mapper.DatasetLoader;
import com.fooddelivery.database.repository.DeliveryRequestRepository;
import com.fooddelivery.database.repository.LocationRepository;
import com.fooddelivery.database.repository.RiderRepository;
import com.fooddelivery.database.repository.RoadRepository;

public class GuiDataLoader {
    private final Path dataDirectory;

    public GuiDataLoader() {
        this(Path.of("data"));
    }

    public GuiDataLoader(Path dataDirectory) {
        if (dataDirectory == null) {
            throw new IllegalArgumentException("Data directory must not be null");
        }
        this.dataDirectory = dataDirectory;
    }

    public DatasetLoadResult load() {
        DatabaseConfig config = DatabaseConfig.defaultConfig();
        Path databasePath = config.databasePath();
        if (Files.exists(databasePath)) {
            try {
                DatabaseConnectionFactory connectionFactory = new DatabaseConnectionFactory(config);
                new DatabaseInitializer(connectionFactory).initialize();
                return loadFromDatabase(connectionFactory);
            } catch (RuntimeException exception) {
                // Fall back to the canonical CSV dataset if the database is unavailable or empty.
            }
        }
        return new DatasetLoader().load(dataDirectory);
    }

    private static DatasetLoadResult loadFromDatabase(DatabaseConnectionFactory connectionFactory) {
        LocationRepository locationRepository = new LocationRepository(connectionFactory);
        RoadRepository roadRepository = new RoadRepository(connectionFactory);
        RiderRepository riderRepository = new RiderRepository(connectionFactory);
        DeliveryRequestRepository deliveryRequestRepository = new DeliveryRequestRepository(connectionFactory);

        if (locationRepository.count() == 0
                && roadRepository.count() == 0
                && riderRepository.count() == 0
                && deliveryRequestRepository.count() == 0) {
            throw new IllegalStateException("Database has no imported dataset.");
        }

        return new DatasetLoadResult(
                locationRepository.findAll(),
                roadRepository.findAll(),
                deliveryRequestRepository.findAll(),
                riderRepository.findAll());
    }
}
