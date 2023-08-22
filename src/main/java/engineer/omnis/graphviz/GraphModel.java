package engineer.omnis.graphviz;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;

// I could've added unmodifiable proxies for GraphModel and Vertices/Edges for their use in algorithms,
// but it feels like overkill for the task at hand tbh
public class GraphModel<V, E> {
    private final Map<V, List<SimpleEntry<V, E>>> graphData = new HashMap<>();

    public void resetGraphState() {
        graphData.clear();
    }

    public boolean addVertex(V v) {
        Objects.requireNonNull(v);
        return graphData.putIfAbsent(v, new LinkedList<>()) == null;
    }

    public void connectVertices(V v1, V v2, E edge) {
        Objects.requireNonNull(v1);
        Objects.requireNonNull(v2);

        if (areConnected(v1, v2)) {
            return;
        }

        graphData.get(v1).add(new SimpleEntry<>(v2, edge));
    }

    public boolean areConnected(V v1, V v2) {
        return graphData.get(v1).stream().anyMatch(v -> v.equals(v2));
    }

    public void removeVertex(V v) {
        Objects.requireNonNull(v);
        graphData.remove(v);
        for (var vertexList : graphData.values()) {
            vertexList.removeIf(p -> p.getKey().equals(v));
        }
    }

    public List<SimpleEntry<V, E>> getNeighbors(V v) {
        return graphData.get(v);
    }

    public Set<V> getVertices() {
        return graphData.keySet();
    }

    public List<E> getEdges(V v) {
        if (!graphData.containsKey(v)) {
            return null;
        }

        return graphData.get(v).stream().map(SimpleEntry::getValue).toList();
    }

    public Optional<E> getEdgeBetween(V v1, V v2) {
        var neighbors = getNeighbors(v1);
        return neighbors == null ? Optional.empty()
                : neighbors.stream().filter(p -> p.getKey() == v2).findAny().map(SimpleEntry::getValue);
    }

    public void removeEdge(V v1, V v2) {
        Objects.requireNonNull(v1);
        Objects.requireNonNull(v2);
        List<SimpleEntry<V, E>> v1Neighbors = graphData.get(v1);
        if (v1Neighbors != null) {
            v1Neighbors.removeIf(p -> p.getKey().equals(v2));
        }
    }

    public void removeConnection(V v1, V v2) {
        removeEdge(v1, v2);
        removeEdge(v2, v1);
    }

    @Override
    public String toString() {
        String newLine = System.getProperty("line.separator");
        StringBuilder graphString = new StringBuilder();

        for (var vertex : graphData.keySet()) {
            graphString.append("Vertex: ").append(vertex).append(newLine);
            graphString.append("Neighbors: ");
            graphString.append(graphData.get(vertex).size() > 0 ? graphData.get(vertex).toString() : "None")
                    .append(newLine)
                    .append(newLine);
        }

        return graphString.toString();
    }
}
