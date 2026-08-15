package com.fooddelivery.algorithms;

import com.fooddelivery.algorithms.result.RiderAssignmentResult;
import com.fooddelivery.algorithms.result.RiderEvaluation;
import com.fooddelivery.algorithms.result.RouteResult;
import com.fooddelivery.datastructures.graphs.CustomGraph;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.Rider;

/**
 * Greedy rider assignment using the committed Dijkstra fastest-route implementation.
 */
public class GreedyRiderAssignment {
    /**
     * Chooses the available sufficient-capacity rider with minimum route time to the request source.
     *
     * @param graph valid delivery graph
     * @param riders custom rider collection
     * @param request delivery request to source
     * @return assignment result, including explicit no-assignment state and per-rider evidence
     * @throws IllegalArgumentException for null graph, null collection, null riders, or null request
     *
     * Preconditions: graph contains eligible rider locations and request source when routes are attempted.
     * Edge cases: unavailable, insufficient, and unreachable riders are ignored with reasons.
     * Input mutation: none. Time: O(R * Dijkstra). Space: O(R + path/trace).
     * Food-delivery use: assign a rider by fastest pickup travel time without mutating dispatch state.
     */
    public RiderAssignmentResult assign(
            CustomGraph graph,
            CustomDynamicArray<Rider> riders,
            DeliveryRequest request) {
        GraphAlgorithmSupport.requireGraph(graph);
        if (riders == null) {
            throw new IllegalArgumentException("Riders must not be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null");
        }
        int requestCapacity = CapacityScaler.scale(request.getCapacityRequired());
        CustomDynamicArray<RiderEvaluation> evaluations = new CustomDynamicArray<>();
        DijkstraFastestRoute dijkstra = new DijkstraFastestRoute();
        Rider bestRider = null;
        RouteResult bestRoute = null;

        for (int i = 0; i < riders.size(); i++) {
            Rider rider = riders.get(i);
            if (rider == null) {
                throw new IllegalArgumentException("Rider entries must not be null");
            }
            if (!rider.isAvailable()) {
                evaluations.add(new RiderEvaluation(rider.getRiderId(), false, "unavailable",
                        Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
                continue;
            }
            if (CapacityScaler.scale(rider.getCarryingCapacity()) < requestCapacity) {
                evaluations.add(new RiderEvaluation(rider.getRiderId(), false, "insufficient capacity",
                        Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
                continue;
            }
            if (!isKnownLocation(graph, rider.getCurrentLocationId())
                    || !isKnownLocation(graph, request.getSourceLocationId())) {
                evaluations.add(new RiderEvaluation(rider.getRiderId(), false, "unreachable source",
                        Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
                continue;
            }

            RouteResult route = dijkstra.findRoute(graph, rider.getCurrentLocationId(), request.getSourceLocationId());
            if (!route.isReachable()) {
                evaluations.add(new RiderEvaluation(rider.getRiderId(), false, "unreachable source",
                        Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
                continue;
            }

            evaluations.add(new RiderEvaluation(rider.getRiderId(), true, "eligible",
                    route.getTotalEffectiveTime(), route.getTotalDistanceKm()));
            if (isBetter(rider, route, bestRider, bestRoute)) {
                bestRider = rider;
                bestRoute = route;
            }
        }

        return new RiderAssignmentResult(bestRider != null, bestRider, bestRoute, evaluations);
    }

    private static boolean isKnownLocation(CustomGraph graph, String locationId) {
        return locationId != null && !locationId.isBlank() && graph.containsVertex(locationId);
    }

    private static boolean isBetter(Rider candidate, RouteResult candidateRoute, Rider best, RouteResult bestRoute) {
        if (best == null) {
            return true;
        }
        int comparison = Double.compare(candidateRoute.getTotalEffectiveTime(), bestRoute.getTotalEffectiveTime());
        if (comparison != 0) {
            return comparison < 0;
        }
        return candidate.getRiderId().compareTo(best.getRiderId()) < 0;
    }
}
