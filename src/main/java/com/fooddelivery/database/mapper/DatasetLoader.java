package com.fooddelivery.database.mapper;

import com.fooddelivery.database.csv.CsvReader;
import com.fooddelivery.database.csv.CsvRecord;
import com.fooddelivery.datastructures.hashing.CustomMap;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.Rider;
import com.fooddelivery.model.Road;

import java.nio.file.Path;

public class DatasetLoader {
    private final CsvReader csvReader;

    public DatasetLoader() {
        this(new CsvReader());
    }

    DatasetLoader(CsvReader csvReader) {
        this.csvReader = csvReader;
    }

    public DatasetLoadResult load(Path dataDirectory) {
        if (dataDirectory == null) {
            throw new IllegalArgumentException("Data directory must not be null");
        }
        CustomDynamicArray<Location> locations = loadLocations(resolve(dataDirectory, "locations.csv", "Locations.csv"));
        CustomMap<String, String> locationIdByName = locationIdByName(locations);
        CustomDynamicArray<Road> roads = loadRoads(resolve(dataDirectory, "roads.csv", "Roads_Edges.csv"));
        CustomDynamicArray<DeliveryRequest> requests = loadRequests(resolve(dataDirectory, "service_requests.csv", "Service Request.csv"));
        CustomDynamicArray<Rider> riders = loadRiders(resolve(dataDirectory, "resources.csv", "Resource.csv"), locationIdByName);
        return new DatasetLoadResult(locations, roads, requests, riders);
    }

    private CustomDynamicArray<Location> loadLocations(Path path) {
        CustomDynamicArray<CsvRecord> records = csvReader.read(path);
        CustomDynamicArray<Location> locations = new CustomDynamicArray<>(records.size() == 0 ? 1 : records.size());
        LocationCsvMapper mapper = new LocationCsvMapper();
        for (int i = 0; i < records.size(); i++) {
            locations.add(mapper.map(records.get(i)));
        }
        return locations;
    }

    private CustomDynamicArray<Road> loadRoads(Path path) {
        CustomDynamicArray<CsvRecord> records = csvReader.read(path);
        CustomDynamicArray<Road> roads = new CustomDynamicArray<>(records.size() == 0 ? 1 : records.size());
        RoadCsvMapper mapper = new RoadCsvMapper();
        for (int i = 0; i < records.size(); i++) {
            roads.add(mapper.map(records.get(i), i + 1));
        }
        return roads;
    }

    private CustomDynamicArray<DeliveryRequest> loadRequests(Path path) {
        CustomDynamicArray<CsvRecord> records = csvReader.read(path);
        CustomDynamicArray<DeliveryRequest> requests = new CustomDynamicArray<>(records.size() == 0 ? 1 : records.size());
        DeliveryRequestCsvMapper mapper = new DeliveryRequestCsvMapper();
        for (int i = 0; i < records.size(); i++) {
            requests.add(mapper.map(records.get(i)));
        }
        return requests;
    }

    private CustomDynamicArray<Rider> loadRiders(Path path, CustomMap<String, String> locationIdByName) {
        CustomDynamicArray<CsvRecord> records = csvReader.read(path);
        CustomDynamicArray<Rider> riders = new CustomDynamicArray<>(records.size() == 0 ? 1 : records.size());
        RiderCsvMapper mapper = new RiderCsvMapper(locationIdByName);
        for (int i = 0; i < records.size(); i++) {
            riders.add(mapper.map(records.get(i)));
        }
        return riders;
    }

    private static CustomMap<String, String> locationIdByName(CustomDynamicArray<Location> locations) {
        CustomMap<String, String> lookup = new CustomMap<>();
        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            lookup.put(location.getName(), location.getLocationId());
        }
        return lookup;
    }

    private static Path resolve(Path dataDirectory, String preferredFileName, String fallbackFileName) {
        Path preferred = dataDirectory.resolve(preferredFileName);
        return java.nio.file.Files.exists(preferred) ? preferred : dataDirectory.resolve(fallbackFileName);
    }
}
