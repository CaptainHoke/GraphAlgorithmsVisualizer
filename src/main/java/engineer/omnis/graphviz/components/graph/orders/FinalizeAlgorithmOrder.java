package engineer.omnis.graphviz.components.graph.orders;

import engineer.omnis.graphviz.components.graph.GraphComponent;

public class FinalizeAlgorithmOrder implements ExternalGraphOrder {
    @Override
    public void executeOn(GraphComponent graphComponent) {
        graphComponent.finalizeAlgorithmExecution();
    }
}
