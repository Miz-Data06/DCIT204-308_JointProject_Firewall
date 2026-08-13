package com.fooddelivery.model;

/**
 * Lifecycle states shared by delivery dispatch data structures and algorithms.
 */
public enum RequestStatus {
    PENDING,
    ASSIGNED,
    PICKED_UP,
    DELIVERED,
    CANCELLED
}
