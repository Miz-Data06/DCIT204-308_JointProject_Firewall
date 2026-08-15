package com.fooddelivery.database.mapper;

import com.fooddelivery.database.csv.CsvReader;
import com.fooddelivery.database.csv.CsvRecord;
import com.fooddelivery.datastructures.hashing.CustomMap;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.LocationType;
import com.fooddelivery.model.RequestStatus;
import com.fooddelivery.model.Rider;
import com.fooddelivery.model.Road;
import com.fooddelivery.model.VehicleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatasetMapperTest {
    @TempDir
    Path tempDirectory;

    @Test
    void mapsLocationTypes() throws IOException {
        CsvRecord record = record("locationId,name,area,type,latitude,longitude\n"
                + "LOC010,Hall,Greater Accra,Hostel/Hall,5.5,-0.2\n");

        Location location = new LocationCsvMapper().map(record);

        assertEquals("LOC010", location.getLocationId());
        assertEquals(LocationType.HOSTEL_HALL, location.getType());
    }

    @Test
    void generatesStableRoadIdsByRowOrder() throws IOException {
        CsvRecord record = record("fromLocationId,toLocationId,distance,travel_Time,roadConditionWeight\n"
                + "LOC001,LOC002,2.5,7.0,1.5\n");

        Road road = new RoadCsvMapper().map(record, 12);

        assertEquals("ROAD012", road.getRoadId());
        assertEquals(2.5, road.getDistanceKm());
        assertEquals(10.5, road.getEffectiveTime());
    }

    @Test
    void mapsRequestStatusTimeAndPriorityDefaults() throws IOException {
        CsvRecord record = record("requestId,source,sourceLocationId,destination,destinationLocationId,category,urgency,"
                + "timeSubmitted,deadline,status\n"
                + "SR999,Pizza,LOC001,Hall,LOC002,Food Delivery,High,11:50 PM,12:10 AM,In Transit\n");

        DeliveryRequest request = new DeliveryRequestCsvMapper().map(record);

        assertEquals(3, request.getUrgency());
        assertEquals(RequestStatus.PICKED_UP, request.getStatus());
        assertEquals(1.0, request.getCapacityRequired());
        assertEquals(LocalDateTime.of(2026, 1, 1, 23, 50), request.getTimeSubmitted());
        assertEquals(LocalDateTime.of(2026, 1, 2, 0, 10), request.getDeadline());
        assertTrue(request.getPriorityScore() >= 0.0);
        assertTrue(request.getPriorityScore() <= 1.0);
    }

    @Test
    void mapsRiderHomeLocationByLocationName() throws IOException {
        CsvRecord record = record("resource_Id,type,homeLocation,capacity,available_Status\n"
                + "RES010,Car Delivery,Madina,3.25,In Use\n");
        CustomMap<String, String> lookup = new CustomMap<>();
        lookup.put("Madina", "LOC013");

        Rider rider = new RiderCsvMapper(lookup).map(record);

        assertEquals("RES010", rider.getRiderId());
        assertEquals("Rider RES010", rider.getName());
        assertEquals("LOC013", rider.getCurrentLocationId());
        assertEquals(VehicleType.CAR, rider.getVehicleType());
        assertEquals(3.25, rider.getCarryingCapacity());
        assertEquals(false, rider.isAvailable());
    }

    @Test
    void rejectsUnknownRiderHomeLocation() throws IOException {
        CsvRecord record = record("resource_Id,type,homeLocation,capacity,available_Status\n"
                + "RES010,Cargo,Unknown,3.25,Available\n");

        assertThrows(IllegalArgumentException.class, () -> new RiderCsvMapper(new CustomMap<>()).map(record));
    }

    @Test
    void loadsCoreDatasetCounts() {
        DatasetLoadResult result = new DatasetLoader().load(Path.of("data"));

        assertEquals(150, result.getLocations().size());
        assertEquals(300, result.getRoads().size());
        assertEquals(900, result.getDeliveryRequests().size());
        assertEquals(90, result.getRiders().size());
        assertEquals("ROAD001", result.getRoads().get(0).getRoadId());
    }

    private CsvRecord record(String content) throws IOException {
        Path file = tempDirectory.resolve("record.csv");
        Files.writeString(file, content);
        return new CsvReader().read(file).get(0);
    }
}
