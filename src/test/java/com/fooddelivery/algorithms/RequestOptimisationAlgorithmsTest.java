package com.fooddelivery.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.fooddelivery.algorithms.result.GreedyCounterexampleResult;
import com.fooddelivery.algorithms.result.RequestSelectionResult;
import com.fooddelivery.algorithms.result.RiderAssignmentResult;
import com.fooddelivery.datastructures.graphs.CustomGraph;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.RequestStatus;
import com.fooddelivery.model.Rider;
import com.fooddelivery.model.VehicleType;

class RequestOptimisationAlgorithmsTest {
    @Test
    void capacityScalerUsesApprovedExactRule() {
        assertEquals(0, CapacityScaler.scale(0.0));
        assertEquals(1, CapacityScaler.scale(0.01));
        assertEquals(125, CapacityScaler.scale(1.25));
        assertEquals(275, CapacityScaler.scale(2.75));
        assertEquals(1000, CapacityScaler.scale(10.00));

        assertThrows(IllegalArgumentException.class, () -> CapacityScaler.scale(-0.01));
        assertThrows(IllegalArgumentException.class, () -> CapacityScaler.scale(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> CapacityScaler.scale(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> CapacityScaler.scale(1.234));
    }

    @Test
    void bruteForceHandlesEmptyZeroAndNoFitCases() {
        BruteForceRequestSelection selection = new BruteForceRequestSelection();

        RequestSelectionResult empty = selection.select(new CustomDynamicArray<>(), 0.0);
        assertEquals(0, empty.getSelectedRequests().size());
        assertEquals(1, empty.getStatesConsidered());

        RequestSelectionResult noFit = selection.select(AlgorithmTestFixtures.requests(request("REQ-A", 1.25, 5.0)), 1.00);
        assertEquals(0, noFit.getSelectedRequests().size());
        assertEquals(0.0, noFit.getTotalPriority(), 0.0001);
    }

    @Test
    void bruteForceSelectsOptimalFractionalCapacitySubsetWithTieBreakers() {
        DeliveryRequest a = request("REQ-A", 1.25, 5.0);
        DeliveryRequest b = request("REQ-B", 1.50, 6.0);
        DeliveryRequest c = request("REQ-C", 0.50, 4.0);

        RequestSelectionResult result = new BruteForceRequestSelection().select(
                AlgorithmTestFixtures.requests(a, b, c), 1.75);

        assertEquals("REQ-A,REQ-C", ids(result.getSelectedRequests()));
        assertEquals(1.75, result.getTotalCapacityUsed(), 0.0001);
        assertEquals(175, result.getTotalScaledCapacityUsed());
        assertEquals(9.0, result.getTotalPriority(), 0.0001);
        assertEquals(8, result.getStatesConsidered());
    }

    @Test
    void dynamicProgrammingMatchesBruteForceOnSmallInputsAndReconstructs() {
        CustomDynamicArray<DeliveryRequest> requests = AlgorithmTestFixtures.requests(
                request("REQ-A", 1.25, 5.0),
                request("REQ-B", 1.50, 6.0),
                request("REQ-C", 0.50, 4.0));

        RequestSelectionResult brute = new BruteForceRequestSelection().select(requests, 1.75);
        RequestSelectionResult dp = new DynamicProgrammingKnapsack().select(requests, 1.75);

        assertEquals(ids(brute.getSelectedRequests()), ids(dp.getSelectedRequests()));
        assertEquals(brute.getTotalPriority(), dp.getTotalPriority(), 0.0001);
        assertTrue(dp.getDpTable().length > 0);
        assertTrue(dp.getTotalScaledCapacityUsed() <= dp.getScaledCapacityLimit());
    }

    @Test
    void dynamicProgrammingTieBreaksByLowerCapacityThenLexicographicIds() {
        DeliveryRequest a = request("REQ-A", 1.00, 5.0);
        DeliveryRequest b = request("REQ-B", 1.25, 5.0);
        DeliveryRequest c = request("REQ-C", 1.00, 5.0);

        RequestSelectionResult lowerCapacity = new DynamicProgrammingKnapsack().select(
                AlgorithmTestFixtures.requests(a, b), 2.00);
        RequestSelectionResult lexicographic = new DynamicProgrammingKnapsack().select(
                AlgorithmTestFixtures.requests(c, a), 1.00);

        assertEquals("REQ-A", ids(lowerCapacity.getSelectedRequests()));
        assertEquals(100, lowerCapacity.getTotalScaledCapacityUsed());
        assertEquals("REQ-A", ids(lexicographic.getSelectedRequests()));
    }

    @Test
    void optimisationRejectsInvalidValuesAndUnsafeDpTables() {
        CustomDynamicArray<DeliveryRequest> invalidPriority = AlgorithmTestFixtures.requests(request("REQ-A", 1.0, -1.0));
        CustomDynamicArray<DeliveryRequest> invalidCapacity = AlgorithmTestFixtures.requests(request("REQ-A", 1.234, 1.0));

        assertThrows(IllegalArgumentException.class, () -> new BruteForceRequestSelection().select(null, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new BruteForceRequestSelection().select(invalidPriority, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new DynamicProgrammingKnapsack().select(invalidCapacity, 2.0));
        assertThrows(IllegalArgumentException.class, () -> new DynamicProgrammingKnapsack().select(
                AlgorithmTestFixtures.requests(request("REQ-A", 1.0, 1.0)), 20000.00));
    }

    @Test
    void greedyAssignmentSelectsFastestEligibleRiderAndExplainsRejections() {
        CustomGraph graph = GraphAlgorithmFixtures.routeChoiceGraph(GraphAlgorithmFixtures.graphRepresentations().get(0));
        CustomDynamicArray<Rider> riders = new CustomDynamicArray<>();
        riders.add(rider("R-Z", "B", 5.0, true));
        riders.add(rider("R-A", "C", 5.0, true));
        riders.add(rider("R-U", "A", 5.0, false));
        riders.add(rider("R-S", "A", 0.50, true));
        DeliveryRequest request = requestAt("REQ-1", "D", 1.0);

        RiderAssignmentResult result = new GreedyRiderAssignment().assign(graph, riders, request);

        assertTrue(result.isAssigned());
        assertEquals("R-A", result.getSelectedRider().getRiderId());
        assertEquals("C,D", GraphAlgorithmFixtures.ids(result.getRoute().getPath()));
        assertEquals(4, result.getEvaluations().size());
        assertEquals(4, graph.edgeCount());
        assertEquals(RequestStatus.PENDING, request.getStatus());
    }

    @Test
    void greedyAssignmentReturnsNoSuitableRiderWhenAllRejected() {
        CustomGraph graph = GraphAlgorithmFixtures.routeChoiceGraph(GraphAlgorithmFixtures.graphRepresentations().get(0));
        CustomDynamicArray<Rider> riders = new CustomDynamicArray<>();
        riders.add(rider("R-U", "A", 5.0, false));
        riders.add(rider("R-S", "A", 0.50, true));

        RiderAssignmentResult result = new GreedyRiderAssignment().assign(graph, riders, requestAt("REQ-1", "D", 1.0));

        assertFalse(result.isAssigned());
        assertEquals(2, result.getEvaluations().size());
    }

    @Test
    void greedyCounterexampleIsExecutableAndShowsBetterGlobalAssignment() {
        GreedyCounterexampleResult result = new GreedyCounterexampleScenario().demonstrate();

        assertEquals("REQ-1", result.getFirstRequestId());
        assertEquals("R-A", result.getGreedyFirstRiderId());
        assertTrue(result.demonstratesGreedyFailure());
        assertTrue(result.getBetterTotalEffectiveTime() < result.getGreedyTotalEffectiveTime());
    }

    @Test
    void requestSelectionResultsExposeIndependentSnapshotsAndPreserveObjects() {
        DeliveryRequest a = request("REQ-A", 1.00, 5.0);
        RequestSelectionResult result = new BruteForceRequestSelection().select(AlgorithmTestFixtures.requests(a), 1.0);
        CustomDynamicArray<DeliveryRequest> snapshot = result.getSelectedRequests();
        snapshot.clear();

        assertEquals(1, result.getSelectedRequests().size());
        assertSame(a, result.getSelectedRequests().get(0));
    }

    private static DeliveryRequest request(String id, double capacity, double priority) {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 8, 0);
        return new DeliveryRequest(id, "LOC-A", "LOC-B", "Food", 1, capacity,
                now, now.plusHours(1), RequestStatus.PENDING, priority);
    }

    private static DeliveryRequest requestAt(String id, String source, double capacity) {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 8, 0);
        String destination = source.equals("A") ? "B" : "A";
        return new DeliveryRequest(id, source, destination, "Food", 1, capacity,
                now, now.plusHours(1), RequestStatus.PENDING, 1.0);
    }

    private static Rider rider(String id, String location, double capacity, boolean available) {
        return new Rider(id, id, location, VehicleType.MOTORCYCLE, capacity, available);
    }

    private static String ids(CustomDynamicArray<DeliveryRequest> requests) {
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
