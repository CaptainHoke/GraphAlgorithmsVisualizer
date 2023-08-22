package engineer.omnis.graphviz.graph.orders;

import engineer.omnis.graphviz.graph.components.GraphComponent;

@FunctionalInterface
public interface ExternalGraphOrder {
    void executeOn(GraphComponent graphComponent);
}
