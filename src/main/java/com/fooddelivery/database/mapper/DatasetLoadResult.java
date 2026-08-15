package com.fooddelivery.database.mapper;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.Rider;
import com.fooddelivery.model.Road;

public final class DatasetLoadResult {
    private final CustomDynamicArray<Location> locations;
    private final CustomDynamicArray<Road> roads;
    private final CustomDynamicArray<DeliveryRequest> deliveryRequests;
    private final CustomDynamicArray<Rider> riders;

    public DatasetLoadResult(
            CustomDynamicArray<Location> locations,
            CustomDynamicArray<Road> roads,
            CustomDynamicArray<DeliveryRequest> deliveryRequests,
            CustomDynamicArray<Rider> riders) {
        this.locations = locations;
        this.roads = roads;
        this.deliveryRequests = deliveryRequests;
        this.riders = riders;
    }

    public CustomDynamicArray<Location> getLocations() {
        return locations;
    }

    public CustomDynamicArray<Road> getRoads() {
        return roads;
    }

    public CustomDynamicArray<DeliveryRequest> getDeliveryRequests() {
        return deliveryRequests;
    }

    public CustomDynamicArray<Rider> getRiders() {
        return riders;
    }
}
