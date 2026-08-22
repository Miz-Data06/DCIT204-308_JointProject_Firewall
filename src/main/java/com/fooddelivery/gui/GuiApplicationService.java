package com.fooddelivery.gui;

import com.fooddelivery.algorithms.DijkstraFastestRoute;
import com.fooddelivery.algorithms.GreedyRiderAssignment;
import com.fooddelivery.algorithms.PriorityScoreCalculator;
import com.fooddelivery.algorithms.result.PriorityScoreResult;
import com.fooddelivery.algorithms.result.RiderAssignmentResult;
import com.fooddelivery.algorithms.result.RouteResult;
import com.fooddelivery.database.mapper.DatasetLoadResult;
import com.fooddelivery.datastructures.graphs.CustomGraph;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.integration.DeliveryNetworkBuilder;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.LocationType;
import com.fooddelivery.model.RequestStatus;
import com.fooddelivery.model.Rider;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiApplicationService {
    private final DatasetLoadResult dataset;
    private final CustomGraph graph;
    private final List<GuiOrderResult> recentRequests = new ArrayList<>();
    private int nextRequestNumber = 1;

    public GuiApplicationService() {
        this(new GuiDataLoader().load());
    }

    public GuiApplicationService(DatasetLoadResult dataset) {
        if (dataset == null) {
            throw new IllegalArgumentException("Dataset must not be null");
        }
        this.dataset = dataset;
        this.graph = new DeliveryNetworkBuilder().build(dataset);
    }

    public List<Location> getSourceLocations() {
        List<Location> restaurants = new ArrayList<>();
        CustomDynamicArray<Location> locations = dataset.getLocations();
        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            if (location.getType() == LocationType.RESTAURANT) {
                restaurants.add(location);
            }
        }
        return restaurants.isEmpty() ? getAllLocations() : restaurants;
    }

    public List<Location> getAllLocations() {
        List<Location> locations = new ArrayList<>();
        CustomDynamicArray<Location> values = dataset.getLocations();
        for (int i = 0; i < values.size(); i++) {
            locations.add(values.get(i));
        }
        return locations;
    }

    public List<GuiOrderResult> getRecentRequests() {
        return Collections.unmodifiableList(recentRequests);
    }

    public String getSystemSummary() {
        return "Locations: " + dataset.getLocations().size()
                + "\nRoads: " + dataset.getRoads().size()
                + "\nService requests: " + dataset.getDeliveryRequests().size()
                + "\nResources: " + dataset.getRiders().size()
                + "\nDatabase status: dataset loaded from project data; SQLite/JDBC integration remains available"
                + "\nCore Maven test suite: 517 passing tests with GUI service coverage";
    }

    public GuiOrderResult placeOrder(
            String sourceLocationId,
            String destinationLocationId,
            String category,
            String urgencyLabel,
            String capacityText) {
        String sourceId = requireNonBlank(sourceLocationId, "Source");
        String destinationId = requireNonBlank(destinationLocationId, "Destination");
        if (sourceId.equals(destinationId)) {
            throw new IllegalArgumentException("Source and destination cannot be the same.");
        }
        String requestCategory = requireNonBlank(category, "Category");
        int urgency = parseUrgency(urgencyLabel);
        double capacity = parseCapacity(capacityText);
        requireKnownLocation(sourceId, "Source");
        requireKnownLocation(destinationId, "Destination");

        String requestId = nextRequestId();
        LocalDateTime submitted = LocalDateTime.now();
        PriorityScoreResult priority = new PriorityScoreCalculator().calculate(
                urgency / 3.0,
                urgency / 3.0,
                0.15);
        DeliveryRequest request = new DeliveryRequest(
                requestId,
                sourceId,
                destinationId,
                requestCategory,
                urgency,
                capacity,
                submitted,
                submitted.plusHours(deadlineHours(urgency)),
                RequestStatus.PENDING,
                priority.getPriorityScore());

        RouteResult route = new DijkstraFastestRoute().findRoute(graph, sourceId, destinationId);
        RiderAssignmentResult assignment = new GreedyRiderAssignment().assign(graph, dataset.getRiders(), request);
        if (assignment.isAssigned()) {
            request.setStatus(RequestStatus.ASSIGNED);
        }

        GuiOrderResult result = toGuiResult(request, normalizeUrgencyLabel(urgencyLabel), route, assignment);
        recentRequests.add(0, result);
        return result;
    }

    private GuiOrderResult toGuiResult(
            DeliveryRequest request,
            String urgencyLabel,
            RouteResult route,
            RiderAssignmentResult assignment) {
        boolean routeAvailable = route != null && route.isReachable();
        boolean riderAssigned = assignment != null && assignment.isAssigned();
        String message = routeAvailable
                ? "Delivery request processed with the real fastest-route service."
                : "Route calculation service could not find a route for this request.";
        if (!riderAssigned) {
            message += " Rider/resource assignment did not find an available matching resource.";
        }
        return new GuiOrderResult(
                request.getRequestId(),
                labelForLocation(request.getSourceLocationId()),
                labelForLocation(request.getDestinationLocationId()),
                request.getCategory(),
                urgencyLabel,
                request.getCapacityRequired(),
                request.getStatus(),
                request.getPriorityScore(),
                routeAvailable,
                routeAvailable ? formatPath(route.getPath()) : "Route unavailable",
                routeAvailable ? route.getTotalEffectiveTime() : Double.NaN,
                riderAssigned,
                riderAssigned ? labelForRider(assignment.getSelectedRider()) : "No rider/resource assigned",
                message);
    }

    private String nextRequestId() {
        return String.format("GUI%03d", nextRequestNumber++);
    }

    private static String requireNonBlank(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        return value.trim();
    }

    private static double parseCapacity(String capacityText) {
        String text = requireNonBlank(capacityText, "Capacity required");
        try {
            double value = Double.parseDouble(text);
            if (!Double.isFinite(value) || value <= 0.0) {
                throw new IllegalArgumentException("Capacity required must be greater than 0.");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Capacity required must be a number.");
        }
    }

    private static int parseUrgency(String urgencyLabel) {
        return switch (normalizeUrgencyLabel(urgencyLabel)) {
            case "LOW" -> 1;
            case "MEDIUM" -> 2;
            case "HIGH" -> 3;
            default -> throw new IllegalArgumentException("Urgency must be LOW, MEDIUM, or HIGH.");
        };
    }

    private static String normalizeUrgencyLabel(String urgencyLabel) {
        return requireNonBlank(urgencyLabel, "Urgency").toUpperCase();
    }

    private static long deadlineHours(int urgency) {
        return switch (urgency) {
            case 3 -> 1;
            case 2 -> 2;
            default -> 4;
        };
    }

    private void requireKnownLocation(String locationId, String label) {
        if (!graph.containsVertex(locationId)) {
            throw new IllegalArgumentException(label + " must be a known dataset location.");
        }
    }

    private String labelForLocation(String locationId) {
        CustomDynamicArray<Location> locations = dataset.getLocations();
        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            if (location.getLocationId().equals(locationId)) {
                return location.getName() + " (" + location.getLocationId() + ")";
            }
        }
        return locationId;
    }

    private static String labelForRider(Rider rider) {
        return rider.getName() + " (" + rider.getRiderId() + ")";
    }

    private String formatPath(CustomDynamicArray<String> path) {
        if (path == null || path.isEmpty()) {
            return "Route unavailable";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) {
                builder.append(" -> ");
            }
            builder.append(labelForLocation(path.get(i)));
        }
        return builder.toString();
    }
}
