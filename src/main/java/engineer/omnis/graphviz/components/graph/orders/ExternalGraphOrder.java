package engineer.omnis.graphviz.components.graph.orders;

import engineer.omnis.graphviz.components.graph.GraphComponent;

@FunctionalInterface
public interface ExternalGraphOrder {
    void executeOn(GraphComponent graphComponent);
}
