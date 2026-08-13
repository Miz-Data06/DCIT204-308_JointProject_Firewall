package com.fooddelivery.datastructures.graphs;

import com.fooddelivery.datastructures.hashing.CustomMap;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.Road;

/**
 * Undirected weighted graph backed by custom adjacency lists.
 */
public class AdjacencyListGraph implements CustomGraph {
    private static final String PAIR_SEPARATOR = "#|#";

    private final CustomMap<String, VertexRecord> verticesById;
    private final CustomDynamicArray<String> vertexIds;
    private final CustomMap<String, Road> roadsByPair;
    private final CustomMap<String, Road> roadsById;
    private final CustomDynamicArray<Road> roads;
    private int vertexCount;
    private int edgeCount;

    public AdjacencyListGraph() {
        verticesById = new CustomMap<>();
        vertexIds = new CustomDynamicArray<>();
        roadsByPair = new CustomMap<>();
        roadsById = new CustomMap<>();
        roads = new CustomDynamicArray<>();
    }

    /**
     * Adds or updates a vertex in O(1) average time, excluding table resizing.
     *
     * @param location non-null location
     */
    @Override
    public void addVertex(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location must not be null");
        }

        String locationId = location.getLocationId();
        VertexRecord existing = verticesById.get(locationId);
        if (existing != null) {
            existing.location = location;
            return;
        }

        verticesById.put(locationId, new VertexRecord(location));
        vertexIds.add(locationId);
        vertexCount++;
    }

    /**
     * Adds an undirected road in O(1) average lookup time plus adjacency append work.
     *
     * @param fromLocationId non-blank endpoint
     * @param toLocationId non-blank endpoint
     * @param road non-null road matching the supplied endpoint pair
     */
    @Override
    public void addEdge(String fromLocationId, String toLocationId, Road road) {
        String fromId = requireNonBlank(fromLocationId, "From location ID");
        String toId = requireNonBlank(toLocationId, "To location ID");
        if (road == null) {
            throw new IllegalArgumentException("Road must not be null");
        }
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Edge endpoints must differ");
        }

        VertexRecord fromRecord = requireVertex(fromId);
        VertexRecord toRecord = requireVertex(toId);
        validateRoadEndpoints(fromId, toId, road);

        String pairKey = pairKey(fromId, toId);
        if (roadsByPair.containsKey(pairKey)) {
            throw new IllegalArgumentException("An edge already exists for this endpoint pair");
        }
        if (roadsById.containsKey(road.getRoadId())) {
            throw new IllegalArgumentException("Road ID already exists");
        }

        fromRecord.neighbours.add(toOutgoingEdge(fromId, toId, road));
        toRecord.neighbours.add(toOutgoingEdge(toId, fromId, road));
        roadsByPair.put(pairKey, road);
        roadsById.put(road.getRoadId(), road);
        roads.add(road);
        edgeCount++;
    }

    /**
     * Removes a vertex and all incident roads.
     *
     * @param locationId non-blank existing vertex ID
     * @return removed location
     */
    @Override
    public Location removeVertex(String locationId) {
        String id = requireNonBlank(locationId, "Location ID");
        VertexRecord record = requireVertex(id);

        while (!record.neighbours.isEmpty()) {
            Edge edge = record.neighbours.get(0);
            removeEdge(id, edge.getDestinationId());
        }

        Location removedLocation = record.location;
        verticesById.remove(id);
        removeStringValue(vertexIds, id);
        vertexCount--;
        return removedLocation;
    }

    /**
     * Removes one undirected road in O(d) adjacency removal time plus road-array cleanup.
     *
     * @param fromLocationId non-blank endpoint
     * @param toLocationId non-blank endpoint
     * @return original road object
     */
    @Override
    public Road removeEdge(String fromLocationId, String toLocationId) {
        String fromId = requireNonBlank(fromLocationId, "From location ID");
        String toId = requireNonBlank(toLocationId, "To location ID");
        VertexRecord fromRecord = requireVertex(fromId);
        VertexRecord toRecord = requireVertex(toId);
        String key = pairKey(fromId, toId);
        Road road = roadsByPair.get(key);
        if (road == null) {
            throw new IllegalArgumentException("Edge does not exist");
        }

        removeOutgoingEdge(fromRecord.neighbours, toId);
        removeOutgoingEdge(toRecord.neighbours, fromId);
        roadsByPair.remove(key);
        roadsById.remove(road.getRoadId());
        removeRoadValue(roads, road);
        edgeCount--;
        return road;
    }

    /**
     * Returns outgoing edges sorted by destination ID in O(d + d^2) time.
     *
     * @param locationId non-blank existing vertex ID
     * @return independent neighbour snapshot
     */
    @Override
    public CustomDynamicArray<Edge> getNeighbours(String locationId) {
        VertexRecord record = requireVertex(requireNonBlank(locationId, "Location ID"));
        CustomDynamicArray<Edge> snapshot = new CustomDynamicArray<>();
        for (int i = 0; i < record.neighbours.size(); i++) {
            snapshot.add(record.neighbours.get(i));
        }
        sortEdgesByDestination(snapshot);
        return snapshot;
    }

    /**
     * Returns effective travel time for a direct road.
     *
     * @param fromLocationId non-blank endpoint
     * @param toLocationId non-blank endpoint
     * @return effective travel time
     */
    @Override
    public double getWeight(String fromLocationId, String toLocationId) {
        String fromId = requireNonBlank(fromLocationId, "From location ID");
        String toId = requireNonBlank(toLocationId, "To location ID");
        requireVertex(fromId);
        requireVertex(toId);
        Road road = roadsByPair.get(pairKey(fromId, toId));
        if (road == null) {
            throw new IllegalArgumentException("Edge does not exist");
        }
        return road.getEffectiveTime();
    }

    /**
     * Checks vertex presence in O(1) average time and O(V) worst-case time.
     *
     * @param locationId non-blank location ID
     * @return true when present
     */
    @Override
    public boolean containsVertex(String locationId) {
        return verticesById.containsKey(requireNonBlank(locationId, "Location ID"));
    }

    /**
     * Gets a stored vertex in O(1) average time and O(V) worst-case time.
     *
     * @param locationId non-blank location ID
     * @return stored location or null
     */
    @Override
    public Location getVertex(String locationId) {
        VertexRecord record = verticesById.get(requireNonBlank(locationId, "Location ID"));
        return record == null ? null : record.location;
    }

    /**
     * Returns locations sorted by location ID in O(V + V^2) time.
     *
     * @return independent vertex snapshot
     */
    @Override
    public CustomDynamicArray<Location> getVertices() {
        CustomDynamicArray<Location> snapshot = new CustomDynamicArray<>();
        for (int i = 0; i < vertexIds.size(); i++) {
            snapshot.add(verticesById.get(vertexIds.get(i)).location);
        }
        sortLocationsById(snapshot);
        return snapshot;
    }

    /**
     * Returns each undirected road once in O(E + E^2) time.
     *
     * @return independent road snapshot
     */
    @Override
    public CustomDynamicArray<Road> getEdges() {
        CustomDynamicArray<Road> snapshot = new CustomDynamicArray<>();
        for (int i = 0; i < roads.size(); i++) {
            snapshot.add(roads.get(i));
        }
        sortRoads(snapshot);
        return snapshot;
    }

    @Override
    public int vertexCount() {
        return vertexCount;
    }

    @Override
    public int edgeCount() {
        return edgeCount;
    }

    /**
     * Checks undirected edge presence in O(1) average time and O(E) worst-case time.
     *
     * @param fromLocationId non-blank endpoint
     * @param toLocationId non-blank endpoint
     * @return true when a direct road exists
     */
    @Override
    public boolean containsEdge(String fromLocationId, String toLocationId) {
        String fromId = requireNonBlank(fromLocationId, "From location ID");
        String toId = requireNonBlank(toLocationId, "To location ID");
        requireVertex(fromId);
        requireVertex(toId);
        return roadsByPair.containsKey(pairKey(fromId, toId));
    }

    /**
     * Clears all graph content.
     */
    @Override
    public void clear() {
        verticesById.clear();
        vertexIds.clear();
        roadsByPair.clear();
        roadsById.clear();
        roads.clear();
        vertexCount = 0;
        edgeCount = 0;
    }

    private VertexRecord requireVertex(String locationId) {
        VertexRecord record = verticesById.get(locationId);
        if (record == null) {
            throw new IllegalArgumentException("Vertex does not exist");
        }
        return record;
    }

    private String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank");
        }
        return value;
    }

    private void validateRoadEndpoints(String fromLocationId, String toLocationId, Road road) {
        boolean forward = road.getFromLocationId().equals(fromLocationId) && road.getToLocationId().equals(toLocationId);
        boolean reverse = road.getFromLocationId().equals(toLocationId) && road.getToLocationId().equals(fromLocationId);
        if (!forward && !reverse) {
            throw new IllegalArgumentException("Road endpoints must match supplied endpoints");
        }
    }

    private Edge toOutgoingEdge(String sourceId, String destinationId, Road road) {
        return new Edge(
                road.getRoadId(),
                sourceId,
                destinationId,
                road.getDistanceKm(),
                road.getNormalTravelTimeMinutes(),
                road.getRoadConditionWeight()
        );
    }

    private String pairKey(String firstLocationId, String secondLocationId) {
        String smaller = smallerEndpoint(firstLocationId, secondLocationId);
        String larger = largerEndpoint(firstLocationId, secondLocationId);
        return smaller + PAIR_SEPARATOR + larger;
    }

    private String smallerEndpoint(String firstLocationId, String secondLocationId) {
        return firstLocationId.compareTo(secondLocationId) <= 0 ? firstLocationId : secondLocationId;
    }

    private String largerEndpoint(String firstLocationId, String secondLocationId) {
        return firstLocationId.compareTo(secondLocationId) <= 0 ? secondLocationId : firstLocationId;
    }

    private void removeOutgoingEdge(CustomDynamicArray<Edge> neighbours, String destinationId) {
        for (int i = 0; i < neighbours.size(); i++) {
            if (neighbours.get(i).getDestinationId().equals(destinationId)) {
                neighbours.remove(i);
                return;
            }
        }
        throw new IllegalArgumentException("Outgoing edge does not exist");
    }

    private void removeStringValue(CustomDynamicArray<String> values, String target) {
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).equals(target)) {
                values.remove(i);
                return;
            }
        }
    }

    private void removeRoadValue(CustomDynamicArray<Road> values, Road target) {
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).getRoadId().equals(target.getRoadId())) {
                values.remove(i);
                return;
            }
        }
    }

    private void sortEdgesByDestination(CustomDynamicArray<Edge> edges) {
        for (int i = 1; i < edges.size(); i++) {
            Edge current = edges.get(i);
            int j = i - 1;
            while (j >= 0 && edges.get(j).getDestinationId().compareTo(current.getDestinationId()) > 0) {
                edges.set(j + 1, edges.get(j));
                j--;
            }
            edges.set(j + 1, current);
        }
    }

    private void sortLocationsById(CustomDynamicArray<Location> locations) {
        for (int i = 1; i < locations.size(); i++) {
            Location current = locations.get(i);
            int j = i - 1;
            while (j >= 0 && locations.get(j).getLocationId().compareTo(current.getLocationId()) > 0) {
                locations.set(j + 1, locations.get(j));
                j--;
            }
            locations.set(j + 1, current);
        }
    }

    private void sortRoads(CustomDynamicArray<Road> roadSnapshot) {
        for (int i = 1; i < roadSnapshot.size(); i++) {
            Road current = roadSnapshot.get(i);
            int j = i - 1;
            while (j >= 0 && compareRoads(roadSnapshot.get(j), current) > 0) {
                roadSnapshot.set(j + 1, roadSnapshot.get(j));
                j--;
            }
            roadSnapshot.set(j + 1, current);
        }
    }

    private int compareRoads(Road first, Road second) {
        String firstSmaller = smallerEndpoint(first.getFromLocationId(), first.getToLocationId());
        String secondSmaller = smallerEndpoint(second.getFromLocationId(), second.getToLocationId());
        int smallerComparison = firstSmaller.compareTo(secondSmaller);
        if (smallerComparison != 0) {
            return smallerComparison;
        }

        String firstLarger = largerEndpoint(first.getFromLocationId(), first.getToLocationId());
        String secondLarger = largerEndpoint(second.getFromLocationId(), second.getToLocationId());
        int largerComparison = firstLarger.compareTo(secondLarger);
        if (largerComparison != 0) {
            return largerComparison;
        }

        return first.getRoadId().compareTo(second.getRoadId());
    }

    private static final class VertexRecord {
        private Location location;
        private final CustomDynamicArray<Edge> neighbours;

        private VertexRecord(Location location) {
            this.location = location;
            neighbours = new CustomDynamicArray<>();
        }
    }
}
