package com.fooddelivery.web;

import com.fooddelivery.database.DatabaseConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebOrderServiceTest {
    @TempDir
    Path tempDirectory;

    @Test
    void placesWebOrderWithRouteAndAssignmentEvidence() {
        WebOrderService service = service();

        WebOrderConfirmation confirmation = service.placeOrder(Map.of(
                "sourceLocationId", "LOC001",
                "destinationLocationId", "LOC011",
                "category", "Food Delivery",
                "urgency", "High",
                "capacityRequired", "1.00",
                "deadlineMinutes", "90"));

        assertEquals("WEB001", confirmation.request().getRequestId());
        assertTrue(confirmation.request().getPriorityScore() >= 0.0);
        assertTrue(confirmation.route().isReachable());
        assertTrue(confirmation.route().getPath().size() >= 2);
        assertTrue(service.confirmationJson(confirmation).contains("\"requestId\":\"WEB001\""));
        assertTrue(service.recentOrdersJson().contains("WEB001"));
        assertTrue(service.countsJson().contains("\"auditEvents\":1"));
    }

    @Test
    void generatedWebIdsAdvanceWithoutCollidingWithDatasetIds() {
        WebOrderService service = service();

        WebOrderConfirmation first = service.placeOrder(validFields("LOC001", "LOC011"));
        WebOrderConfirmation second = service.placeOrder(validFields("LOC002", "LOC012"));

        assertEquals("WEB001", first.request().getRequestId());
        assertEquals("WEB002", second.request().getRequestId());
    }

    @Test
    void rejectsInvalidOrderInput() {
        WebOrderService service = service();

        assertThrows(IllegalArgumentException.class, () -> service.placeOrder(validFields("LOC001", "LOC001")));
        assertThrows(IllegalArgumentException.class, () -> service.placeOrder(Map.of(
                "sourceLocationId", "LOC001",
                "destinationLocationId", "LOC011",
                "category", "Food Delivery",
                "urgency", "Urgent",
                "capacityRequired", "1.00")));
    }

    private WebOrderService service() {
        return new WebOrderService(new DatabaseConfig(tempDirectory.resolve("web-test.db")), Path.of("data"));
    }

    private static Map<String, String> validFields(String source, String destination) {
        return Map.of(
                "sourceLocationId", source,
                "destinationLocationId", destination,
                "category", "Food Delivery",
                "urgency", "Medium",
                "capacityRequired", "1.00",
                "deadlineMinutes", "90");
    }
}
