package com.fooddelivery.algorithms;

import java.time.LocalDateTime;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.RequestStatus;

final class AlgorithmTestFixtures {
    static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 1, 1, 8, 0);

    private AlgorithmTestFixtures() {
    }

    static DeliveryRequest request(String id, int minutesOffset, int deadlineOffset, double priority) {
        return new DeliveryRequest(
                id,
                "LOC-A",
                "LOC-B",
                "Food",
                1,
                1.0,
                BASE_TIME.plusMinutes(minutesOffset),
                BASE_TIME.plusMinutes(deadlineOffset),
                RequestStatus.PENDING,
                priority);
    }

    static CustomDynamicArray<DeliveryRequest> requests(DeliveryRequest... values) {
        CustomDynamicArray<DeliveryRequest> requests = new CustomDynamicArray<>();
        for (DeliveryRequest value : values) {
            requests.add(value);
        }
        return requests;
    }

    static String ids(CustomDynamicArray<DeliveryRequest> requests) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < requests.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(requests.get(i).getRequestId());
        }
        return builder.toString();
    }
}
