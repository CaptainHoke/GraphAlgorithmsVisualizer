package engineer.omnis.graphviz.algorithms;

import engineer.omnis.graphviz.GraphModel;
import engineer.omnis.graphviz.graph.components.GraphEdgePair;
import engineer.omnis.graphviz.graph.components.GraphNodeComponent;

import java.util.function.Consumer;

@FunctionalInterface
public interface GraphStrategyConstructor {
    GraphAlgorithmStrategy apply(GraphModel<GraphNodeComponent, GraphEdgePair> graphModel, Runnable preInitAction, Runnable postInitAction, Consumer<String> onCompleteAction, Runnable onAbortedAction);
}
