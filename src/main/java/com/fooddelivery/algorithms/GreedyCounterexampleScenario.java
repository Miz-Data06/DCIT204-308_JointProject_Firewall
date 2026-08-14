package com.fooddelivery.algorithms;

import java.time.LocalDateTime;

import com.fooddelivery.algorithms.result.GreedyCounterexampleResult;
import com.fooddelivery.algorithms.result.RouteResult;
import com.fooddelivery.datastructures.graphs.AdjacencyListGraph;
import com.fooddelivery.datastructures.graphs.CustomGraph;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.LocationType;
import com.fooddelivery.model.RequestStatus;
import com.fooddelivery.model.Rider;
import com.fooddelivery.model.Road;
import com.fooddelivery.model.VehicleType;

/**
 * Narrow executable scenario showing why closest-rider greedy assignment can be globally worse.
 */
public class GreedyCounterexampleScenario {
    /**
     * Builds and evaluates a two-rider, two-request scenario.
     *
     * @return result showing greedy total time is worse than a global swap
     *
     * Input mutation: none. Time: O(1) fixed scenario. Space: O(1).
     * Food-delivery use: evidence that independent pickup-time greediness can hurt multi-request dispatch.
     */
    public GreedyCounterexampleResult demonstrate() {
        CustomGraph graph = buildGraph();
        Rider riderFastToFirst = rider("R-A", "A");
        Rider riderFlexible = rider("R-B", "B");
        DeliveryRequest firstRequest = request("REQ-1", "S1");
        DeliveryRequest secondRequest = request("REQ-2", "S2");

        CustomDynamicArray<Rider> riders = new CustomDynamicArray<>();
        riders.add(riderFastToFirst);
        riders.add(riderFlexible);

        GreedyRiderAssignment greedy = new GreedyRiderAssignment();
        String greedyFirstRider = greedy.assign(graph, riders, firstRequest).getSelectedRider().getRiderId();
        RouteResult greedyFirstRoute = route(graph, riderFastToFirst, firstRequest);
        RouteResult greedySecondRoute = route(graph, riderFlexible, secondRequest);
        double greedyTotal = greedyFirstRoute.getTotalEffectiveTime() + greedySecondRoute.getTotalEffectiveTime();

        RouteResult betterFirstRoute = route(graph, riderFlexible, firstRequest);
        RouteResult betterSecondRoute = route(graph, riderFastToFirst, secondRequest);
        double betterTotal = betterFirstRoute.getTotalEffectiveTime() + betterSecondRoute.getTotalEffectiveTime();

        return new GreedyCounterexampleResult(firstRequest.getRequestId(), greedyFirstRider, riderFlexible.getRiderId(),
                greedyTotal, riderFlexible.getRiderId(), riderFastToFirst.getRiderId(), betterTotal);
    }

    private static RouteResult route(CustomGraph graph, Rider rider, DeliveryRequest request) {
        return new DijkstraFastestRoute().findRoute(graph, rider.getCurrentLocationId(), request.getSourceLocationId());
    }

    private static CustomGraph buildGraph() {
        CustomGraph graph = new AdjacencyListGraph();
        addLocation(graph, "A");
        addLocation(graph, "B");
        addLocation(graph, "S1");
        addLocation(graph, "S2");
        addRoad(graph, "R-A-S1", "A", "S1", 1.0, 1.0, 1.0);
        addRoad(graph, "R-B-S1", "B", "S1", 2.0, 2.0, 1.0);
        addRoad(graph, "R-A-S2", "A", "S2", 2.0, 2.0, 1.0);
        addRoad(graph, "R-B-S2", "B", "S2", 100.0, 100.0, 1.0);
        return graph;
    }

    private static void addLocation(CustomGraph graph, String id) {
        graph.addVertex(new Location(id, id, "Legon", LocationType.RESTAURANT, 5.0, -0.1));
    }

    private static void addRoad(CustomGraph graph, String id, String from, String to, double distance, double time, double weight) {
        graph.addEdge(from, to, new Road(id, from, to, distance, time, weight));
    }

    private static Rider rider(String id, String location) {
        return new Rider(id, id, location, VehicleType.MOTORCYCLE, 5.0, true);
    }

    private static DeliveryRequest request(String id, String source) {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 8, 0);
        String destination = source.equals("S2") ? "S1" : "S2";
        return new DeliveryRequest(id, source, destination, "Food", 1, 1.0, now, now.plusHours(1),
                RequestStatus.PENDING, 1.0);
    }
}
