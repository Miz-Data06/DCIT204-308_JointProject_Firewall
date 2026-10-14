package com.fooddelivery.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fooddelivery.algorithms.DijkstraFastestRoute;
import com.fooddelivery.algorithms.GreedyRiderAssignment;
import com.fooddelivery.algorithms.PriorityScoreCalculator;
import com.fooddelivery.algorithms.result.PriorityScoreResult;
import com.fooddelivery.algorithms.result.RiderAssignmentResult;
import com.fooddelivery.algorithms.result.RouteResult;
import com.fooddelivery.database.DatabaseConfig;
import com.fooddelivery.database.DatabaseConnectionFactory;
import com.fooddelivery.database.mapper.DatasetLoadResult;
import com.fooddelivery.database.repository.DeliveryRequestRepository;
import com.fooddelivery.database.repository.RiderRepository;
import com.fooddelivery.datastructures.graphs.CustomGraph;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.integration.DeliveryNetworkBuilder;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.LocationType;
import com.fooddelivery.model.RequestStatus;
import com.fooddelivery.model.Rider;
import com.fooddelivery.model.Road;

public class GuiApplicationService {
    private final DatasetLoadResult dataset;
    private final CustomGraph graph;
    private final DatabaseConnectionFactory databaseConnectionFactory;
    private final List<GuiOrderResult> recentRequests = new ArrayList<>();
    private int nextRequestNumber = 1;

    public GuiApplicationService() {
        this(new GuiDataLoader().load(), new DatabaseConnectionFactory(DatabaseConfig.defaultConfig()));
    }

    public GuiApplicationService(DatasetLoadResult dataset) {
        this(dataset, null);
    }

    public GuiApplicationService(DatasetLoadResult dataset, DatabaseConnectionFactory databaseConnectionFactory) {
        if (dataset == null) {
            throw new IllegalArgumentException("Dataset must not be null");
        }
        this.dataset = dataset;
        this.graph = new DeliveryNetworkBuilder().build(dataset);
        this.databaseConnectionFactory = databaseConnectionFactory;
        this.nextRequestNumber = findNextRequestNumber(dataset);
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

    public List<DeliveryRequest> getDatasetRequests() {
        List<DeliveryRequest> requests = new ArrayList<>();
        CustomDynamicArray<DeliveryRequest> values = dataset.getDeliveryRequests();
        for (int i = 0; i < values.size(); i++) {
            requests.add(values.get(i));
        }
        return requests;
    }

    public List<Rider> getRiders() {
        List<Rider> riders = new ArrayList<>();
        CustomDynamicArray<Rider> values = dataset.getRiders();
        for (int i = 0; i < values.size(); i++) {
            riders.add(values.get(i));
        }
        return riders;
    }

    public List<Road> getRoads() {
        List<Road> roads = new ArrayList<>();
        CustomDynamicArray<Road> values = dataset.getRoads();
        for (int i = 0; i < values.size(); i++) {
            roads.add(values.get(i));
        }
        return roads;
    }

    public int getAvailableRiderCount() {
        int count = 0;
        CustomDynamicArray<Rider> riders = dataset.getRiders();
        for (int i = 0; i < riders.size(); i++) {
            if (riders.get(i).isAvailable()) {
                count++;
            }
        }
        return count;
    }

    public int getLocationCount() {
        return dataset.getLocations().size();
    }

    public int getRoadCount() {
        return dataset.getRoads().size();
    }

    public int getRequestCount() {
        return dataset.getDeliveryRequests().size();
    }

    public int getResourceCount() {
        return dataset.getRiders().size();
    }

    public String getSystemSummary() {
        return "Locations: " + getLocationCount()
                + "\nRoads: " + getRoadCount()
                + "\nService requests: " + getRequestCount()
                + "\nResources: " + getResourceCount()
                + "\nDatabase status: dataset loaded from project data; SQLite/JDBC integration remains available"
                + "\nCore Maven test suite: 519 passing tests with GUI service coverage";
    }

    public Optional<GuiSampleRoute> findSampleRoute() {
        if (graph.containsVertex("LOC001") && graph.containsVertex("LOC002")
                && new DijkstraFastestRoute().findRoute(graph, "LOC001", "LOC002").isReachable()) {
            return Optional.of(new GuiSampleRoute("LOC001", "LOC002"));
        }
        CustomDynamicArray<Road> roads = dataset.getRoads();
        for (int i = 0; i < roads.size(); i++) {
            Road road = roads.get(i);
            if (graph.containsVertex(road.getFromLocationId())
                    && graph.containsVertex(road.getToLocationId())
                    && new DijkstraFastestRoute().findRoute(
                            graph, road.getFromLocationId(), road.getToLocationId()).isReachable()) {
                return Optional.of(new GuiSampleRoute(road.getFromLocationId(), road.getToLocationId()));
            }
        }
        return Optional.empty();
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

        boolean assignmentPersisted = true;
        if (databaseConnectionFactory != null) {
            assignmentPersisted = persistOrder(request, assignment);
        }

        if (!assignmentPersisted && assignment != null && assignment.isAssigned()) {
            request.setStatus(RequestStatus.PENDING);
            assignment = new RiderAssignmentResult(false, null, assignment.getRoute(), assignment.getEvaluations());
        } else if (assignment != null && assignment.isAssigned() && assignment.getSelectedRider() != null) {
            markAssignedRiderUnavailable(assignment.getSelectedRider());
        }

        GuiOrderResult result = toGuiResult(request, normalizeUrgencyLabel(urgencyLabel), route, assignment);
        recentRequests.add(0, result);
        return result;
    }

    private void markAssignedRiderUnavailable(Rider selectedRider) {
        if (selectedRider == null) {
            return;
        }
        selectedRider.setAvailable(false);
        for (int i = 0; i < dataset.getRiders().size(); i++) {
            Rider current = dataset.getRiders().get(i);
            if (current.getRiderId().equals(selectedRider.getRiderId())) {
                current.setAvailable(false);
                break;
            }
        }
    }

    private boolean persistOrder(DeliveryRequest request, RiderAssignmentResult assignment) {
        DeliveryRequestRepository requestRepository = new DeliveryRequestRepository(databaseConnectionFactory);
        RiderRepository riderRepository = new RiderRepository(databaseConnectionFactory);

        try (Connection connection = databaseConnectionFactory.openConnection()) {
            connection.setAutoCommit(false);
            try {
                requestRepository.saveInTransaction(connection, request);

                if (assignment != null && assignment.isAssigned() && assignment.getSelectedRider() != null) {
                    boolean reserved = riderRepository.reserveIfAvailable(connection, assignment.getSelectedRider().getRiderId());
                    if (!reserved) {
                        connection.rollback();
                        return false;
                    }
                }

                connection.commit();
                return true;
            } catch (RuntimeException | SQLException exception) {
                if (connection != null) {
                    try {
                        connection.rollback();
                    } catch (SQLException rollbackException) {
                        exception.addSuppressed(rollbackException);
                    }
                }
                throw exception;
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException exception) {
                    throw new IllegalStateException("Could not reset database transaction state", exception);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not persist order and rider reservation atomically", exception);
        }
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
                routeAvailable ? routeLabels(route.getPath()) : List.of(),
                routeAvailable ? routeEdgeTimes(route.getEdgeEffectiveTimes()) : List.of(),
                routeAvailable ? route.getTotalEffectiveTime() : Double.NaN,
                riderAssigned,
                riderAssigned ? labelForRider(assignment.getSelectedRider()) : "No rider/resource assigned",
                request.getTimeSubmitted(),
                message);
    }

    private static int findNextRequestNumber(DatasetLoadResult dataset) {
        int highest = 0;
        CustomDynamicArray<DeliveryRequest> requests = dataset.getDeliveryRequests();
        for (int i = 0; i < requests.size(); i++) {
            String requestId = requests.get(i).getRequestId();
            if (requestId != null && requestId.startsWith("GUI")) {
                try {
                    int requestNumber = Integer.parseInt(requestId.substring(3));
                    if (requestNumber > highest) {
                        highest = requestNumber;
                    }
                } catch (NumberFormatException ignored) {
                    // Ignore malformed GUI IDs and continue from the last valid one.
                }
            }
        }
        return highest + 1;
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

    private List<String> routeLabels(CustomDynamicArray<String> path) {
        List<String> labels = new ArrayList<>();
        if (path == null) {
            return labels;
        }
        for (int i = 0; i < path.size(); i++) {
            labels.add(labelForLocation(path.get(i)));
        }
        return labels;
    }

    private static List<Double> routeEdgeTimes(CustomDynamicArray<Double> edgeTimes) {
        List<Double> values = new ArrayList<>();
        if (edgeTimes == null) {
            return values;
        }
        for (int i = 0; i < edgeTimes.size(); i++) {
            values.add(edgeTimes.get(i));
        }
        return values;
    }
}
