package engineer.omnis.graphviz.algorithms;

import engineer.omnis.graphviz.GraphModel;
import engineer.omnis.graphviz.graph.components.GraphEdgePair;
import engineer.omnis.graphviz.graph.components.GraphNodeComponent;
import engineer.omnis.graphviz.graph.orders.ColorUpdateOrder;
import engineer.omnis.graphviz.graph.orders.ExternalGraphOrder;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static engineer.omnis.graphviz.Utility.BIG_INT;

// TODO: Algo styles
public class GraphPrimStrategy extends GraphAlgorithmStrategy {
    private static final Color CURR_NODE_COLOR = new Color(239, 86, 239);
    private static final Color PARENT_NODE_COLOR = new Color(99, 12, 229);
    private static final Color MST_EDGE_COLOR = new Color(0, 239, 239);
    private final GraphModel<GraphNodeComponent, GraphEdgePair> graphModel;
    private final Set<GraphNodeComponent> mstVertices = new HashSet<>();
    private final Map<GraphNodeComponent, Integer> minWeightEdgeValues = new HashMap<>();
    private final List<EdgeFromTo> mstEdges = new ArrayList<>();
    private final PriorityQueue<QueueEntry> nextNodesQueue = new PriorityQueue<>();
    private GraphNodeComponent initialNode = null;

    public GraphPrimStrategy(GraphModel<GraphNodeComponent, GraphEdgePair> graphModel, Runnable preInitAction, Runnable postInitAction, Consumer<String> onCompleteAction, Runnable onAbortedAction) {
        super(preInitAction, postInitAction, onCompleteAction, onAbortedAction);
        this.graphModel = graphModel;
    }

    @Override
    public boolean isReadyToRun() {
        return !nextNodesQueue.isEmpty();
    }

    @Override
    public boolean isFinished() {
        return nextNodesQueue.isEmpty();
    }

    @Override
    public List<ExternalGraphOrder> performStep() {
        List<ExternalGraphOrder> updateCommands = new ArrayList<>();
        QueueEntry queueEntry = nextNodesQueue.poll();

        if (Objects.requireNonNull(queueEntry).minEdgeWeight > minWeightEdgeValues.get(queueEntry.node) || mstVertices.contains(queueEntry.node)) {
            return updateCommands;
        }

        updateCommands.add(new ColorUpdateOrder(queueEntry.parentNode, PARENT_NODE_COLOR));
        updateCommands.add(new ColorUpdateOrder(queueEntry.node, CURR_NODE_COLOR));
        var edgeToColor = graphModel.getEdgeBetween(queueEntry.node, queueEntry.parentNode);
        edgeToColor.ifPresent(e -> {
            updateCommands.add(new ColorUpdateOrder(e, MST_EDGE_COLOR));
            mstEdges.add(new EdgeFromTo(queueEntry.parentNode.getId(), queueEntry.node.getId()));
        });

        mstVertices.add(queueEntry.node);
        var neighbors = graphModel.getNeighbors(queueEntry.node);

        for (var n : neighbors) {
            GraphEdgePair edge = n.getValue();
            if (edge.getWeight() < minWeightEdgeValues.get(n.getKey())) {
                minWeightEdgeValues.put(n.getKey(), edge.getWeight());
            }

            nextNodesQueue.add(new QueueEntry(n.getKey(), queueEntry.node, n.getValue().getWeight()));
        }

        return updateCommands;
    }

    @Override
    public void onVertexInput(GraphNodeComponent c) {
        initialNode = c;
        minWeightEdgeValues.put(c, 0);
        graphModel.getVertices().forEach(v -> minWeightEdgeValues.putIfAbsent(v, BIG_INT));

        graphModel.getNeighbors(initialNode).stream()
                .map(n -> new QueueEntry(n.getKey(), initialNode, n.getValue().getWeight()))
                .collect(Collectors.toCollection(() -> nextNodesQueue));
    }

    @Override
    public String getResult() {
        return "Prim : " + mstEdges.stream()
                .map(e -> e.vTo + "=" + e.vFrom)
                .sorted().collect(Collectors.joining(", "));
    }

    private record EdgeFromTo(char vFrom, char vTo) {
    }

    private record QueueEntry(GraphNodeComponent node, GraphNodeComponent parentNode, int minEdgeWeight)
            implements Comparable<QueueEntry> {
        @Override
        public int compareTo(QueueEntry o) {
            return Integer.compare(minEdgeWeight, o.minEdgeWeight);
        }
    }
}
