package com.fooddelivery.gui;

import com.fooddelivery.database.mapper.DatasetLoadResult;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.LocationType;
import com.fooddelivery.model.RequestStatus;
import com.fooddelivery.model.Rider;
import com.fooddelivery.model.Road;
import com.fooddelivery.model.VehicleType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuiApplicationServiceTest {
    @Test
    void placesOrderWithGeneratedGuiRequestId() {
        GuiApplicationService service = new GuiApplicationService(smallDataset());

        GuiOrderResult result = service.placeOrder("LOC001", "LOC003", "Food Delivery", "HIGH", "1.0");

        assertEquals("GUI001", result.getRequestId());
        assertTrue(result.isRouteAvailable());
        assertTrue(result.isRiderAssigned());
        assertEquals(RequestStatus.ASSIGNED, result.getStatus());
        assertEquals(1, service.getRecentRequests().size());
    }

    @Test
    void rejectsSameSourceAndDestination() {
        GuiApplicationService service = new GuiApplicationService(smallDataset());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.placeOrder("LOC001", "LOC001", "Food Delivery", "LOW", "1.0"));

        assertTrue(exception.getMessage().contains("cannot be the same"));
    }

    @Test
    void rejectsNonPositiveCapacity() {
        GuiApplicationService service = new GuiApplicationService(smallDataset());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.placeOrder("LOC001", "LOC002", "Food Delivery", "MEDIUM", "0"));

        assertTrue(exception.getMessage().contains("greater than 0"));
    }

    @Test
    void generatesSequentialGuiRequestIds() {
        GuiApplicationService service = new GuiApplicationService(smallDataset());

        GuiOrderResult first = service.placeOrder("LOC001", "LOC002", "Food Delivery", "LOW", "1.0");
        GuiOrderResult second = service.placeOrder("LOC001", "LOC003", "Food Delivery", "MEDIUM", "1.0");

        assertEquals("GUI001", first.getRequestId());
        assertEquals("GUI002", second.getRequestId());
    }

    @Test
    void exposesRouteNodeLabelsForVisualization() {
        GuiApplicationService service = new GuiApplicationService(smallDataset());

        GuiOrderResult result = service.placeOrder("LOC001", "LOC003", "Food Delivery", "HIGH", "1.0");

        assertEquals(2, result.getRouteNodeLabels().size());
        assertTrue(result.getRouteNodeLabels().get(0).contains("Restaurant A"));
        assertTrue(result.getRouteNodeLabels().get(1).contains("Campus C"));
    }

    @Test
    void findsDatasetBackedSampleRoute() {
        GuiApplicationService service = new GuiApplicationService(smallDataset());

        GuiSampleRoute sample = service.findSampleRoute().orElseThrow();

        assertEquals("LOC001", sample.getSourceLocationId());
        assertEquals("LOC002", sample.getDestinationLocationId());
    }

    private static DatasetLoadResult smallDataset() {
        CustomDynamicArray<Location> locations = new CustomDynamicArray<>();
        locations.add(new Location("LOC001", "Restaurant A", "Area", LocationType.RESTAURANT, 5.0, -0.1));
        locations.add(new Location("LOC002", "Hostel B", "Area", LocationType.HOSTEL_HALL, 5.1, -0.2));
        locations.add(new Location("LOC003", "Campus C", "Area", LocationType.CAMPUS, 5.2, -0.3));

        CustomDynamicArray<Road> roads = new CustomDynamicArray<>();
        roads.add(new Road("ROAD001", "LOC001", "LOC002", 1.0, 5.0, 1.0));
        roads.add(new Road("ROAD002", "LOC002", "LOC003", 2.0, 10.0, 1.0));
        roads.add(new Road("ROAD003", "LOC003", "LOC001", 2.5, 8.0, 1.0));

        CustomDynamicArray<DeliveryRequest> requests = new CustomDynamicArray<>();
        LocalDateTime submitted = LocalDateTime.of(2026, 1, 1, 8, 0);
        requests.add(new DeliveryRequest("SR001", "LOC001", "LOC002", "Food Delivery", 1,
                1.0, submitted, submitted.plusHours(2), RequestStatus.PENDING, 0.5));

        CustomDynamicArray<Rider> riders = new CustomDynamicArray<>();
        riders.add(new Rider("RES001", "Rider One", "LOC003", VehicleType.MOTORCYCLE, 3.0, true));

        return new DatasetLoadResult(locations, roads, requests, riders);
    }
}
