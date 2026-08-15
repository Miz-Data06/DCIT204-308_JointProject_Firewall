package com.fooddelivery.database;

import com.fooddelivery.database.mapper.DatasetLoadResult;
import com.fooddelivery.database.repository.DeliveryRequestRepository;
import com.fooddelivery.database.repository.LocationRepository;
import com.fooddelivery.database.repository.RiderRepository;
import com.fooddelivery.database.repository.RoadRepository;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.Rider;
import com.fooddelivery.model.Road;

public class DatasetDatabaseImporter {
    private final LocationRepository locationRepository;
    private final RoadRepository roadRepository;
    private final RiderRepository riderRepository;
    private final DeliveryRequestRepository deliveryRequestRepository;

    public DatasetDatabaseImporter(DatabaseConnectionFactory connectionFactory) {
        this.locationRepository = new LocationRepository(connectionFactory);
        this.roadRepository = new RoadRepository(connectionFactory);
        this.riderRepository = new RiderRepository(connectionFactory);
        this.deliveryRequestRepository = new DeliveryRequestRepository(connectionFactory);
    }

    public void importDataset(DatasetLoadResult dataset) {
        saveLocations(dataset.getLocations());
        saveRoads(dataset.getRoads());
        saveRiders(dataset.getRiders());
        saveRequests(dataset.getDeliveryRequests());
    }

    private void saveLocations(CustomDynamicArray<Location> locations) {
        for (int i = 0; i < locations.size(); i++) {
            locationRepository.save(locations.get(i));
        }
    }

    private void saveRoads(CustomDynamicArray<Road> roads) {
        for (int i = 0; i < roads.size(); i++) {
            roadRepository.save(roads.get(i));
        }
    }

    private void saveRiders(CustomDynamicArray<Rider> riders) {
        for (int i = 0; i < riders.size(); i++) {
            riderRepository.save(riders.get(i));
        }
    }

    private void saveRequests(CustomDynamicArray<DeliveryRequest> requests) {
        for (int i = 0; i < requests.size(); i++) {
            deliveryRequestRepository.save(requests.get(i));
        }
    }
}
