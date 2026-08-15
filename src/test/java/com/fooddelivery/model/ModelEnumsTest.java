package com.fooddelivery.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ModelEnumsTest {
    @Test
    void requestStatusValuesMatchSharedContractInOrder() {
        RequestStatus[] expected = {
                RequestStatus.PENDING,
                RequestStatus.ASSIGNED,
                RequestStatus.PICKED_UP,
                RequestStatus.DELIVERED,
                RequestStatus.CANCELLED
        };

        assertArrayEquals(expected, RequestStatus.values());
    }

    @Test
    void vehicleTypeValuesMatchSharedContractInOrder() {
        VehicleType[] expected = {
                VehicleType.MOTORCYCLE,
                VehicleType.CAR,
                VehicleType.CARGO_VEHICLE
        };

        assertArrayEquals(expected, VehicleType.values());
    }

    @Test
    void locationTypeValuesMatchNormalizedDatasetValuesInOrder() {
        LocationType[] expected = {
                LocationType.CAMPUS,
                LocationType.HOSTEL_HALL,
                LocationType.JUNCTION,
                LocationType.LANDMARK,
                LocationType.MARKET,
                LocationType.RESIDENTIAL_AREA,
                LocationType.RESTAURANT
        };

        assertArrayEquals(expected, LocationType.values());
    }

    @Test
    void valueOfWorksForEveryExpectedConstant() {
        for (RequestStatus status : RequestStatus.values()) {
            assertEquals(status, RequestStatus.valueOf(status.name()));
        }
        for (VehicleType vehicleType : VehicleType.values()) {
            assertEquals(vehicleType, VehicleType.valueOf(vehicleType.name()));
        }
        for (LocationType locationType : LocationType.values()) {
            assertEquals(locationType, LocationType.valueOf(locationType.name()));
        }
    }
}
