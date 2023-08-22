package engineer.omnis.graphviz.algorithms;

import engineer.omnis.graphviz.GraphModel;
import engineer.omnis.graphviz.components.GraphEdgePair;
import engineer.omnis.graphviz.components.GraphNodeComponent;
import engineer.omnis.graphviz.components.graph.orders.ColorUpdateOrder;
import engineer.omnis.graphviz.components.graph.orders.ExternalGraphOrder;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static engineer.omnis.graphviz.Utility.BIG_INT;

public class GraphDijkstraStrategy extends GraphAlgorithmStrategy {
    private static final Color ACTIVE_NODE_COLOR = new Color(239, 86, 239);
    private static final Color NODE_QUEUE_COLOR = new Color(99, 12, 229);
    private final GraphModel<GraphNodeComponent, GraphEdgePair> graphModel;
    private final Map<GraphNodeComponent, Integer> shortestDistances = new HashMap<>();
    private final Set<GraphNodeComponent> shortestPathTreeSet = new HashSet<>();
    private GraphNodeComponent initialNode = null;

    public GraphDijkstraStrategy(GraphModel<GraphNodeComponent, GraphEdgePair> graphModel, Runnable preInitAction, Runnable postInitAction, Consumer<String> onCompleteAction, Runnable onAbortedAction) {
        super(preInitAction, postInitAction, onCompleteAction, onAbortedAction);
        this.graphModel = graphModel;
    }

    @Override
    public boolean isReadyToRun() {
        return initialNode != null;
    }

    @Override
    public boolean isFinished() {
        return shortestPathTreeSet.size() == shortestDistances.size();
    }

    @Override
    public List<ExternalGraphOrder> performStep() {
        List<ExternalGraphOrder> updateCommands = new ArrayList<>();

        GraphNodeComponent node = getMinDistVertex();
        shortestPathTreeSet.add(node);
        updateCommands.add(new ColorUpdateOrder(node, ACTIVE_NODE_COLOR));

        var neighbors = graphModel.getNeighbors(node);
        for (var v : neighbors) {
            if (shortestPathTreeSet.contains(v.getKey())) {
                continue;
            }

            updateCommands.add(new ColorUpdateOrder(v.getKey(), NODE_QUEUE_COLOR));
            int edgeWeight = v.getValue().getWeight();
            shortestDistances.put(v.getKey(),
                    Integer.min(shortestDistances.get(v.getKey()), edgeWeight + shortestDistances.get(node)));
        }

        return updateCommands;
    }

    private GraphNodeComponent getMinDistVertex() {
        return Collections.min(
                shortestDistances.entrySet().stream()
                        .filter(p -> !shortestPathTreeSet.contains(p.getKey())).toList(),
                Map.Entry.comparingByValue()).getKey();
    }

    @Override
    public void onVertexInput(GraphNodeComponent c) {
        initialNode = c;
        shortestDistances.put(c, 0);
        graphModel.getVertices().forEach(v -> shortestDistances.putIfAbsent(v, BIG_INT));
    }

    @Override
    public String getResult() {
        return "Dijkstra : " + shortestDistances.entrySet().stream()
                .filter(p -> p.getKey() != initialNode)
                .map(p -> p.getKey().getId() + "=" + p.getValue())
                .sorted()
                .collect(Collectors.joining(", "));
    }

}
