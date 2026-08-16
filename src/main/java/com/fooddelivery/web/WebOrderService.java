package com.fooddelivery.web;

import com.fooddelivery.algorithms.DijkstraFastestRoute;
import com.fooddelivery.algorithms.GreedyRiderAssignment;
import com.fooddelivery.algorithms.PriorityScoreCalculator;
import com.fooddelivery.algorithms.result.PriorityScoreResult;
import com.fooddelivery.algorithms.result.RiderAssignmentResult;
import com.fooddelivery.algorithms.result.RouteResult;
import com.fooddelivery.database.AuditEventRecord;
import com.fooddelivery.database.DatabaseConfig;
import com.fooddelivery.database.DatabaseConnectionFactory;
import com.fooddelivery.database.DatabaseInitializer;
import com.fooddelivery.database.DatasetDatabaseImporter;
import com.fooddelivery.database.mapper.DatasetLoadResult;
import com.fooddelivery.database.mapper.DatasetLoader;
import com.fooddelivery.database.repository.AlgorithmRunRepository;
import com.fooddelivery.database.repository.AuditEventRepository;
import com.fooddelivery.database.repository.DeliveryRequestRepository;
import com.fooddelivery.database.repository.LocationRepository;
import com.fooddelivery.database.repository.RiderRepository;
import com.fooddelivery.database.repository.RoadRepository;
import com.fooddelivery.datastructures.graphs.CustomGraph;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.integration.DeliveryNetworkBuilder;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.LocationType;
import com.fooddelivery.model.RequestStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class WebOrderService {
    private final Path dataDirectory;
    private final DatabaseConnectionFactory connectionFactory;
    private final LocationRepository locationRepository;
    private final RoadRepository roadRepository;
    private final RiderRepository riderRepository;
    private final DeliveryRequestRepository requestRepository;
    private final AlgorithmRunRepository algorithmRunRepository;
    private final AuditEventRepository auditEventRepository;
    private final DatasetLoadResult dataset;
    private final CustomGraph graph;

    public WebOrderService() {
        this(DatabaseConfig.defaultConfig(), Path.of("data"));
    }

    public WebOrderService(DatabaseConfig databaseConfig, Path dataDirectory) {
        if (dataDirectory == null) {
            throw new IllegalArgumentException("Data directory must not be null.");
        }
        this.dataDirectory = dataDirectory;
        this.connectionFactory = new DatabaseConnectionFactory(databaseConfig);
        new DatabaseInitializer(connectionFactory).initialize();
        this.dataset = new DatasetLoader().load(dataDirectory);
        new DatasetDatabaseImporter(connectionFactory).importDataset(dataset);
        this.locationRepository = new LocationRepository(connectionFactory);
        this.roadRepository = new RoadRepository(connectionFactory);
        this.riderRepository = new RiderRepository(connectionFactory);
        this.requestRepository = new DeliveryRequestRepository(connectionFactory);
        this.algorithmRunRepository = new AlgorithmRunRepository(connectionFactory);
        this.auditEventRepository = new AuditEventRepository(connectionFactory);
        this.graph = new DeliveryNetworkBuilder().build(dataset);
    }

    public String healthJson() {
        return "{\"ok\":true,\"status\":\"ready\",\"message\":\"Food Delivery web demo is running\"}";
    }

    public String countsJson() {
        return "{"
                + "\"locations\":" + locationRepository.count() + ','
                + "\"roads\":" + roadRepository.count() + ','
                + "\"requests\":" + requestRepository.count() + ','
                + "\"riders\":" + riderRepository.count() + ','
                + "\"algorithmRuns\":" + algorithmRunCount() + ','
                + "\"auditEvents\":" + auditEventRepository.count()
                + "}";
    }

    public String locationsJson(boolean restaurantsOnly) {
        CustomDynamicArray<Location> locations = locationRepository.findAll();
        StringBuilder builder = new StringBuilder("[");
        int written = 0;
        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            if (restaurantsOnly && location.getType() != LocationType.RESTAURANT) {
                continue;
            }
            if (written++ > 0) {
                builder.append(',');
            }
            appendLocation(builder, location);
        }
        builder.append(']');
        return builder.toString();
    }

    public String recentOrdersJson() {
        CustomDynamicArray<DeliveryRequest> requests = requestRepository.findAll();
        StringBuilder builder = new StringBuilder("[");
        int written = 0;
        for (int i = requests.size() - 1; i >= 0 && written < 10; i--) {
            if (written++ > 0) {
                builder.append(',');
            }
            appendRequestSummary(builder, requests.get(i));
        }
        builder.append(']');
        return builder.toString();
    }

    public String orderJson(String requestId) {
        DeliveryRequest request = requestRepository.findById(requestId);
        if (request == null) {
            return WebJson.error("Order not found.");
        }
        StringBuilder builder = new StringBuilder("{\"ok\":true,\"order\":");
        appendRequestSummary(builder, request);
        builder.append('}');
        return builder.toString();
    }

    public synchronized WebOrderConfirmation placeOrder(Map<String, String> fields) {
        String sourceLocationId = required(fields, "sourceLocationId");
        String destinationLocationId = required(fields, "destinationLocationId");
        if (sourceLocationId.equals(destinationLocationId)) {
            throw new IllegalArgumentException("Source and destination cannot be the same.");
        }
        ensureLocation(sourceLocationId, "Source location");
        ensureLocation(destinationLocationId, "Destination location");

        String category = required(fields, "category");
        int urgency = parseUrgency(required(fields, "urgency"));
        double capacityRequired = parsePositiveDouble(required(fields, "capacityRequired"), "Capacity required");
        int deadlineMinutes = parseDeadlineMinutes(fields.get("deadlineMinutes"));

        LocalDateTime submitted = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime deadline = submitted.plusMinutes(deadlineMinutes);
        double priorityScore = calculatePriority(urgency, submitted, deadline);

        DeliveryRequest request = new DeliveryRequest(
                nextRequestId(),
                sourceLocationId,
                destinationLocationId,
                category,
                urgency,
                capacityRequired,
                submitted,
                deadline,
                RequestStatus.PENDING,
                priorityScore);

        RouteResult route = new DijkstraFastestRoute().findRoute(graph, sourceLocationId, destinationLocationId);
        RiderAssignmentResult assignment = new GreedyRiderAssignment().assign(graph, dataset.getRiders(), request);
        if (assignment.isAssigned()) {
            request.setStatus(RequestStatus.ASSIGNED);
        }
        requestRepository.save(request);
        auditEventRepository.save(new AuditEventRecord(
                "AUD-WEB-" + request.getRequestId(),
                "DeliveryRequest",
                request.getRequestId(),
                "WEB_ORDER_CREATED",
                submitted,
                "SYSTEM",
                "Web demo order placed and evaluated"));
        return new WebOrderConfirmation(request, route, assignment);
    }

    public String confirmationJson(WebOrderConfirmation confirmation) {
        StringBuilder builder = new StringBuilder("{\"ok\":true,\"order\":");
        appendRequestSummary(builder, confirmation.request());
        builder.append(",\"route\":{")
                .append("\"reachable\":").append(confirmation.route().isReachable()).append(',')
                .append("\"path\":").append(WebJson.stringArray(confirmation.route().getPath())).append(',')
                .append("\"totalDistanceKm\":").append(format(confirmation.route().getTotalDistanceKm())).append(',')
                .append("\"totalEffectiveTime\":").append(format(confirmation.route().getTotalEffectiveTime()))
                .append("},\"assignment\":");
        RiderAssignmentResult assignment = confirmation.assignment();
        if (assignment.isAssigned()) {
            builder.append('{')
                    .append("\"assigned\":true,")
                    .append("\"riderId\":").append(WebJson.quote(assignment.getSelectedRider().getRiderId())).append(',')
                    .append("\"vehicleType\":").append(WebJson.quote(assignment.getSelectedRider().getVehicleType().name())).append(',')
                    .append("\"pickupEffectiveTime\":").append(format(assignment.getRoute().getTotalEffectiveTime()))
                    .append('}');
        } else {
            builder.append("{\"assigned\":false,\"message\":\"No eligible rider was available for this request.\"}");
        }
        builder.append('}');
        return builder.toString();
    }

    private int algorithmRunCount() {
        int databaseCount = algorithmRunRepository.count();
        if (databaseCount > 0) {
            return databaseCount;
        }
        Path file = dataDirectory.resolve("algorithm_runs.csv");
        try {
            if (!Files.exists(file)) {
                return 0;
            }
            try (java.util.stream.Stream<String> lines = Files.lines(file)) {
                long count = lines.count();
                return (int) Math.max(0L, count - 1L);
            }
        } catch (IOException exception) {
            return 0;
        }
    }

    private String nextRequestId() {
        CustomDynamicArray<DeliveryRequest> requests = requestRepository.findAll();
        int max = 0;
        for (int i = 0; i < requests.size(); i++) {
            String requestId = requests.get(i).getRequestId();
            if (requestId.startsWith("WEB")) {
                try {
                    max = Math.max(max, Integer.parseInt(requestId.substring(3)));
                } catch (NumberFormatException ignored) {
                    // Ignore non-standard web ids when finding the next generated id.
                }
            }
        }
        return String.format("WEB%03d", max + 1);
    }

    private void ensureLocation(String locationId, String fieldName) {
        if (locationRepository.findById(locationId) == null) {
            throw new IllegalArgumentException(fieldName + " does not exist: " + locationId);
        }
    }

    private static String required(Map<String, String> fields, String name) {
        String value = fields.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required.");
        }
        return value.trim();
    }

    private static int parseUrgency(String value) {
        return switch (value.trim().toLowerCase()) {
            case "low", "1" -> 1;
            case "medium", "2" -> 2;
            case "high", "3" -> 3;
            default -> throw new IllegalArgumentException("Urgency must be Low, Medium, or High.");
        };
    }

    private static double parsePositiveDouble(String value, String fieldName) {
        try {
            double parsed = Double.parseDouble(value);
            if (!Double.isFinite(parsed) || parsed <= 0.0) {
                throw new IllegalArgumentException(fieldName + " must be positive.");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " must be numeric.", exception);
        }
    }

    private static int parseDeadlineMinutes(String value) {
        if (value == null || value.isBlank()) {
            return 90;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed <= 0) {
                throw new IllegalArgumentException("Deadline minutes must be positive.");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Deadline minutes must be a whole number.", exception);
        }
    }

    private static double calculatePriority(int urgency, LocalDateTime submitted, LocalDateTime deadline) {
        double urgencyComponent = (urgency - 1.0) / 2.0;
        long minutes = Math.max(1L, ChronoUnit.MINUTES.between(submitted, deadline));
        double deadlinePressureComponent = 1.0 - Math.min(minutes, 180.0) / 180.0;
        double waitingTimeComponent = submitted.toLocalTime().toSecondOfDay() / 86_400.0;
        PriorityScoreResult result = new PriorityScoreCalculator().calculate(
                urgencyComponent,
                deadlinePressureComponent,
                waitingTimeComponent);
        return result.getPriorityScore();
    }

    private static void appendLocation(StringBuilder builder, Location location) {
        builder.append('{')
                .append("\"id\":").append(WebJson.quote(location.getLocationId())).append(',')
                .append("\"name\":").append(WebJson.quote(location.getName())).append(',')
                .append("\"area\":").append(WebJson.quote(location.getArea())).append(',')
                .append("\"type\":").append(WebJson.quote(location.getType().name()))
                .append('}');
    }

    private static void appendRequestSummary(StringBuilder builder, DeliveryRequest request) {
        builder.append('{')
                .append("\"requestId\":").append(WebJson.quote(request.getRequestId())).append(',')
                .append("\"sourceLocationId\":").append(WebJson.quote(request.getSourceLocationId())).append(',')
                .append("\"destinationLocationId\":").append(WebJson.quote(request.getDestinationLocationId())).append(',')
                .append("\"category\":").append(WebJson.quote(request.getCategory())).append(',')
                .append("\"urgency\":").append(request.getUrgency()).append(',')
                .append("\"capacityRequired\":").append(format(request.getCapacityRequired())).append(',')
                .append("\"status\":").append(WebJson.quote(request.getStatus().name())).append(',')
                .append("\"priorityScore\":").append(format(request.getPriorityScore()))
                .append('}');
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.6f", value);
    }
}
