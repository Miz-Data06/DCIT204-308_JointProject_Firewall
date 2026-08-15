package com.fooddelivery.database.mapper;

import com.fooddelivery.algorithms.PriorityScoreCalculator;
import com.fooddelivery.algorithms.result.PriorityScoreResult;
import com.fooddelivery.database.csv.CsvRecord;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.RequestStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class DeliveryRequestCsvMapper {
    public static final LocalDate BASE_DATE = LocalDate.of(2026, 1, 1);
    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("[h:mm a][H:mm]")
            .toFormatter(Locale.ENGLISH);

    private final PriorityScoreCalculator priorityScoreCalculator;

    public DeliveryRequestCsvMapper() {
        this(new PriorityScoreCalculator());
    }

    DeliveryRequestCsvMapper(PriorityScoreCalculator priorityScoreCalculator) {
        this.priorityScoreCalculator = priorityScoreCalculator;
    }

    public DeliveryRequest map(CsvRecord record) {
        int urgency = mapUrgency(LocationCsvMapper.required(record, "urgency"));
        LocalDateTime submitted = parseDateTime(LocationCsvMapper.required(record, "time_submitted", "timeSubmitted"));
        LocalDateTime deadline = parseDateTime(LocationCsvMapper.required(record, "deadline"));
        if (!deadline.isAfter(submitted)) {
            deadline = deadline.plusDays(1);
        }
        double priorityScore = calculatePriority(urgency, submitted, deadline);
        return new DeliveryRequest(
                LocationCsvMapper.required(record, "request_id", "requestId"),
                LocationCsvMapper.required(record, "source_location_id", "sourceLocationId"),
                LocationCsvMapper.required(record, "destination_location_id", "destinationLocationId"),
                LocationCsvMapper.required(record, "category"),
                urgency,
                capacityRequired(record),
                submitted,
                deadline,
                mapStatus(LocationCsvMapper.required(record, "status")),
                priorityScore);
    }

    static int mapUrgency(String value) {
        return switch (value) {
            case "LOW" -> 1;
            case "MEDIUM" -> 2;
            case "HIGH" -> 3;
            case "Low" -> 1;
            case "Medium" -> 2;
            case "High" -> 3;
            default -> throw new IllegalArgumentException("Unknown request urgency: " + value);
        };
    }

    static RequestStatus mapStatus(String value) {
        return switch (value) {
            case "PENDING" -> RequestStatus.PENDING;
            case "ASSIGNED" -> RequestStatus.ASSIGNED;
            case "IN_TRANSIT" -> RequestStatus.PICKED_UP;
            case "COMPLETED" -> RequestStatus.DELIVERED;
            case "CANCELLED" -> RequestStatus.CANCELLED;
            case "Pending" -> RequestStatus.PENDING;
            case "Assigned" -> RequestStatus.ASSIGNED;
            case "In Transit" -> RequestStatus.PICKED_UP;
            case "Delivered" -> RequestStatus.DELIVERED;
            case "Cancelled" -> RequestStatus.CANCELLED;
            default -> throw new IllegalArgumentException("Unknown request status: " + value);
        };
    }

    static LocalDateTime parseDateTime(String value) {
        String normal = value.toUpperCase(Locale.ENGLISH).replaceAll("\\s+", " ").trim();
        LocalTime time = LocalTime.parse(normal, TIME_FORMATTER);
        return LocalDateTime.of(BASE_DATE, time);
    }

    private double calculatePriority(int urgency, LocalDateTime submitted, LocalDateTime deadline) {
        double urgencyComponent = (urgency - 1.0) / 2.0;
        long deliveryMinutes = Math.max(1L, ChronoUnit.MINUTES.between(submitted, deadline));
        double deadlinePressureComponent = 1.0 - Math.min(deliveryMinutes, 180.0) / 180.0;
        double waitingTimeComponent = submitted.toLocalTime().toSecondOfDay() / 86_400.0;
        PriorityScoreResult result = priorityScoreCalculator.calculate(
                urgencyComponent,
                deadlinePressureComponent,
                waitingTimeComponent);
        return result.getPriorityScore();
    }

    private static double capacityRequired(CsvRecord record) {
        String value = record.hasHeader("capacity_required") ? record.get("capacity_required") : null;
        if (value == null || value.isBlank()) {
            return 1.0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid capacity_required at row " + record.rowNumber(), exception);
        }
    }
}
