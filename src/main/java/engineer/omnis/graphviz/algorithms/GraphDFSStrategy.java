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

public class GraphDFSStrategy extends GraphAlgorithmStrategy {
    private static final Color ACTIVE_NODE_COLOR = new Color(239, 86, 239);
    private static final Color NODE_QUEUE_COLOR = new Color(99, 12, 229);
    private final GraphModel<GraphNodeComponent, GraphEdgePair> graphModel;
    private final Stack<GraphNodeComponent> nextNodes = new Stack<>();
    private final Set<GraphNodeComponent> visited = new HashSet<>();
    private final List<Character> visitOrder = new ArrayList<>();
    private boolean updatingNeighbors = false;
    private List<GraphNodeComponent> currentNeighbors = new ArrayList<>();

    public GraphDFSStrategy(GraphModel<GraphNodeComponent, GraphEdgePair> graphModel, Runnable preInitAction, Runnable postInitAction, Consumer<String> onCompleteAction, Runnable onAbortedAction) {
        super(preInitAction, postInitAction, onCompleteAction, onAbortedAction);
        this.graphModel = graphModel;
    }

    @Override
    public boolean isReadyToRun() {
        return !nextNodes.empty();
    }

    // Probably could've used "regular" algorithm code and emit GraphUpdateCommands as they arose
    // instead of this "performStep-ified" approach, but I wanted to try out SwingWorkers
    // with publish and process methods
    @Override
    public List<ExternalGraphOrder> performStep() {
        if (nextNodes.empty()) {
            setFinished(true);
            return Collections.emptyList();
        }

        List<ExternalGraphOrder> updateCommands = new ArrayList<>();

        if (updatingNeighbors) {
            updateCommands.addAll(currentNeighbors.stream().map(node -> new ColorUpdateOrder(node, NODE_QUEUE_COLOR)).toList());
            updatingNeighbors = false;
        } else {
            // Visiting
            GraphNodeComponent currentVertex = nextNodes.empty() ? null : nextNodes.pop();

            if (currentVertex != null) {
                updateCommands.add(new ColorUpdateOrder(currentVertex, ACTIVE_NODE_COLOR));
                visitOrder.add(currentVertex.getId());
                visited.add(currentVertex);

                var neighbors = graphModel.getNeighbors(currentVertex)
                        .stream()
                        .filter(pair -> !visited.contains(pair.getKey()) && !nextNodes.contains(pair.getKey()))
                        .sorted(Comparator.comparing(pair -> pair.getValue().getWeight(), Comparator.reverseOrder()))
                        .toList();
                currentNeighbors = new ArrayList<>(neighbors.stream().map(AbstractMap.SimpleEntry::getKey).toList());
                nextNodes.addAll(currentNeighbors);
                updatingNeighbors = true;
            }
        }

        return updateCommands;
    }

    @Override
    public void onVertexInput(GraphNodeComponent c) {
        nextNodes.add(c);
        visited.add(c);
    }

    @Override
    public String getResult() {
        return "DFS : " + visitOrder.stream().map(Object::toString).collect(Collectors.joining(" -> "));
    }
}
