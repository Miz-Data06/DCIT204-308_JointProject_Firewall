package com.fooddelivery.datastructures.graphs;

import com.fooddelivery.datastructures.hashing.CustomMap;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.Road;

/**
 * Undirected weighted graph backed by a custom adjacency matrix.
 */
public class AdjacencyMatrixGraph implements CustomGraph {
    private static final int DEFAULT_CAPACITY = 10;
    private static final String PAIR_SEPARATOR = "#|#";

    private Location[] vertices;
    private Edge[][] adjacencyMatrix;
    private final CustomMap<String, Integer> indexByLocationId;
    private final CustomMap<String, Road> roadsByPair;
    private final CustomMap<String, Road> roadsById;
    private final CustomDynamicArray<Road> roads;
    private int vertexCount;
    private int edgeCount;

    public AdjacencyMatrixGraph() {
        this(DEFAULT_CAPACITY);
    }

    public AdjacencyMatrixGraph(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("Initial capacity must be greater than zero");
        }

        vertices = new Location[initialCapacity];
        adjacencyMatrix = new Edge[initialCapacity][initialCapacity];
        indexByLocationId = new CustomMap<>();
        roadsByPair = new CustomMap<>();
        roadsById = new CustomMap<>();
        roads = new CustomDynamicArray<>();
    }

    /**
     * Returns the current matrix capacity in O(1) time.
     *
     * @return current vertex-slot capacity
     */
    public int capacity() {
        return vertices.length;
    }

    /**
     * Adds or updates a vertex in O(1) average time, or O(C^2) during matrix growth.
     *
     * @param location non-null location
     */
    @Override
    public void addVertex(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location must not be null");
        }

        String locationId = location.getLocationId();
        Integer existingIndex = indexByLocationId.get(locationId);
        if (existingIndex != null) {
            vertices[existingIndex] = location;
            return;
        }

        if (vertexCount == vertices.length) {
            growCapacity();
        }

        vertices[vertexCount] = location;
        indexByLocationId.put(locationId, vertexCount);
        vertexCount++;
    }

    /**
     * Adds an undirected road in O(1) average time after vertex-index lookup.
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

        int fromIndex = requireVertexIndex(fromId);
        int toIndex = requireVertexIndex(toId);
        validateRoadEndpoints(fromId, toId, road);

        String pairKey = pairKey(fromId, toId);
        if (roadsByPair.containsKey(pairKey)) {
            throw new IllegalArgumentException("An edge already exists for this endpoint pair");
        }
        if (roadsById.containsKey(road.getRoadId())) {
            throw new IllegalArgumentException("Road ID already exists");
        }

        adjacencyMatrix[fromIndex][toIndex] = toOutgoingEdge(fromId, toId, road);
        adjacencyMatrix[toIndex][fromIndex] = toOutgoingEdge(toId, fromId, road);
        roadsByPair.put(pairKey, road);
        roadsById.put(road.getRoadId(), road);
        roads.add(road);
        edgeCount++;
    }

    /**
     * Removes a vertex and every incident road in O(V^2 + incident-removal costs).
     *
     * @param locationId non-blank existing vertex ID
     * @return removed location
     */
    @Override
    public Location removeVertex(String locationId) {
        String id = requireNonBlank(locationId, "Location ID");
        int removedIndex = requireVertexIndex(id);
        Location removedLocation = vertices[removedIndex];
        CustomDynamicArray<Edge> incidentEdges = getNeighbours(id);
        for (int i = 0; i < incidentEdges.size(); i++) {
            removeEdge(id, incidentEdges.get(i).getDestinationId());
        }

        int oldVertexCount = vertexCount;
        indexByLocationId.remove(id);
        shiftVerticesLeft(removedIndex, oldVertexCount);
        shiftMatrixColumnsLeft(removedIndex, oldVertexCount);
        shiftMatrixRowsUp(removedIndex, oldVertexCount);
        clearVacatedMatrixSlots(oldVertexCount - 1);
        vertices[oldVertexCount - 1] = null;
        vertexCount--;
        updateShiftedIndexes(removedIndex);
        return removedLocation;
    }

    /**
     * Removes one undirected road in O(E) time because the road snapshot array may shift.
     *
     * @param fromLocationId non-blank endpoint
     * @param toLocationId non-blank endpoint
     * @return original road object
     */
    @Override
    public Road removeEdge(String fromLocationId, String toLocationId) {
        String fromId = requireNonBlank(fromLocationId, "From location ID");
        String toId = requireNonBlank(toLocationId, "To location ID");
        int fromIndex = requireVertexIndex(fromId);
        int toIndex = requireVertexIndex(toId);
        Edge edge = adjacencyMatrix[fromIndex][toIndex];
        if (edge == null) {
            throw new IllegalArgumentException("Edge does not exist");
        }

        String key = pairKey(fromId, toId);
        Road road = roadsByPair.get(key);
        adjacencyMatrix[fromIndex][toIndex] = null;
        adjacencyMatrix[toIndex][fromIndex] = null;
        roadsByPair.remove(key);
        roadsById.remove(road.getRoadId());
        removeRoadValue(roads, road);
        edgeCount--;
        return road;
    }

    /**
     * Returns outgoing edges sorted by destination ID in O(V + d^2) time.
     *
     * @param locationId non-blank existing vertex ID
     * @return independent neighbour snapshot
     */
    @Override
    public CustomDynamicArray<Edge> getNeighbours(String locationId) {
        int index = requireVertexIndex(requireNonBlank(locationId, "Location ID"));
        CustomDynamicArray<Edge> snapshot = new CustomDynamicArray<>();
        for (int column = 0; column < vertexCount; column++) {
            Edge edge = adjacencyMatrix[index][column];
            if (edge != null) {
                snapshot.add(edge);
            }
        }
        sortEdgesByDestination(snapshot);
        return snapshot;
    }

    /**
     * Returns effective travel time for a direct road in O(1) average time after lookup.
     *
     * @param fromLocationId non-blank endpoint
     * @param toLocationId non-blank endpoint
     * @return effective travel time
     */
    @Override
    public double getWeight(String fromLocationId, String toLocationId) {
        String fromId = requireNonBlank(fromLocationId, "From location ID");
        String toId = requireNonBlank(toLocationId, "To location ID");
        int fromIndex = requireVertexIndex(fromId);
        int toIndex = requireVertexIndex(toId);
        Edge edge = adjacencyMatrix[fromIndex][toIndex];
        if (edge == null) {
            throw new IllegalArgumentException("Edge does not exist");
        }
        return edge.getEffectiveTime();
    }

    /**
     * Checks vertex presence in O(1) average time and O(V) worst-case time.
     *
     * @param locationId non-blank location ID
     * @return true when present
     */
    @Override
    public boolean containsVertex(String locationId) {
        return indexByLocationId.containsKey(requireNonBlank(locationId, "Location ID"));
    }

    /**
     * Gets a stored vertex in O(1) average time and O(V) worst-case time.
     *
     * @param locationId non-blank location ID
     * @return stored location or null
     */
    @Override
    public Location getVertex(String locationId) {
        Integer index = indexByLocationId.get(requireNonBlank(locationId, "Location ID"));
        return index == null ? null : vertices[index];
    }

    /**
     * Returns locations sorted by location ID in O(V^2) time.
     *
     * @return independent vertex snapshot
     */
    @Override
    public CustomDynamicArray<Location> getVertices() {
        CustomDynamicArray<Location> snapshot = new CustomDynamicArray<>();
        for (int i = 0; i < vertexCount; i++) {
            snapshot.add(vertices[i]);
        }
        sortLocationsById(snapshot);
        return snapshot;
    }

    /**
     * Returns each undirected road once in O(E^2) time.
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
     * Checks undirected edge presence in O(1) average time after vertex-index lookup.
     *
     * @param fromLocationId non-blank endpoint
     * @param toLocationId non-blank endpoint
     * @return true when a direct road exists
     */
    @Override
    public boolean containsEdge(String fromLocationId, String toLocationId) {
        String fromId = requireNonBlank(fromLocationId, "From location ID");
        String toId = requireNonBlank(toLocationId, "To location ID");
        int fromIndex = requireVertexIndex(fromId);
        int toIndex = requireVertexIndex(toId);
        return adjacencyMatrix[fromIndex][toIndex] != null;
    }

    /**
     * Clears all graph content in O(C^2) time while preserving capacity.
     */
    @Override
    public void clear() {
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = null;
        }
        for (int row = 0; row < adjacencyMatrix.length; row++) {
            for (int column = 0; column < adjacencyMatrix[row].length; column++) {
                adjacencyMatrix[row][column] = null;
            }
        }
        indexByLocationId.clear();
        roadsByPair.clear();
        roadsById.clear();
        roads.clear();
        vertexCount = 0;
        edgeCount = 0;
    }

    private int requireVertexIndex(String locationId) {
        Integer index = indexByLocationId.get(locationId);
        if (index == null) {
            throw new IllegalArgumentException("Vertex does not exist");
        }
        return index;
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

    private void growCapacity() {
        if (vertices.length > Integer.MAX_VALUE / 2) {
            throw new IllegalStateException("Graph capacity cannot grow safely");
        }

        int newCapacity = vertices.length * 2;
        Location[] expandedVertices = new Location[newCapacity];
        Edge[][] expandedMatrix = new Edge[newCapacity][newCapacity];
        for (int i = 0; i < vertexCount; i++) {
            expandedVertices[i] = vertices[i];
        }
        for (int row = 0; row < vertexCount; row++) {
            for (int column = 0; column < vertexCount; column++) {
                expandedMatrix[row][column] = adjacencyMatrix[row][column];
            }
        }

        vertices = expandedVertices;
        adjacencyMatrix = expandedMatrix;
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

    private void shiftVerticesLeft(int removedIndex, int oldVertexCount) {
        for (int i = removedIndex; i < oldVertexCount - 1; i++) {
            vertices[i] = vertices[i + 1];
        }
    }

    private void shiftMatrixColumnsLeft(int removedIndex, int oldVertexCount) {
        for (int row = 0; row < oldVertexCount; row++) {
            for (int column = removedIndex; column < oldVertexCount - 1; column++) {
                adjacencyMatrix[row][column] = adjacencyMatrix[row][column + 1];
            }
        }
    }

    private void shiftMatrixRowsUp(int removedIndex, int oldVertexCount) {
        for (int row = removedIndex; row < oldVertexCount - 1; row++) {
            for (int column = 0; column < oldVertexCount; column++) {
                adjacencyMatrix[row][column] = adjacencyMatrix[row + 1][column];
            }
        }
    }

    private void clearVacatedMatrixSlots(int lastActiveIndex) {
        for (int i = 0; i <= lastActiveIndex; i++) {
            adjacencyMatrix[lastActiveIndex][i] = null;
            adjacencyMatrix[i][lastActiveIndex] = null;
        }
    }

    private void updateShiftedIndexes(int firstShiftedIndex) {
        for (int i = firstShiftedIndex; i < vertexCount; i++) {
            indexByLocationId.put(vertices[i].getLocationId(), i);
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
}
