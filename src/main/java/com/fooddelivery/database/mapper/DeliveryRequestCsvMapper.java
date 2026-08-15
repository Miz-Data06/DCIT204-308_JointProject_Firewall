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
        LocalDateTime submitted = parseDateTime(LocationCsvMapper.required(record, "timeSubmitted"));
        LocalDateTime deadline = parseDateTime(LocationCsvMapper.required(record, "deadline"));
        if (!deadline.isAfter(submitted)) {
            deadline = deadline.plusDays(1);
        }
        double priorityScore = calculatePriority(urgency, submitted, deadline);
        return new DeliveryRequest(
                LocationCsvMapper.required(record, "requestId"),
                LocationCsvMapper.required(record, "sourceLocationId"),
                LocationCsvMapper.required(record, "destinationLocationId"),
                LocationCsvMapper.required(record, "category"),
                urgency,
                1.0,
                submitted,
                deadline,
                mapStatus(LocationCsvMapper.required(record, "status")),
                priorityScore);
    }

    static int mapUrgency(String value) {
        return switch (value) {
            case "Low" -> 1;
            case "Medium" -> 2;
            case "High" -> 3;
            default -> throw new IllegalArgumentException("Unknown request urgency: " + value);
        };
    }

    static RequestStatus mapStatus(String value) {
        return switch (value) {
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
}
