package engineer.omnis.graphviz.graph.orders;

import engineer.omnis.graphviz.graph.components.GraphComponent;

public class FinalizeAlgorithmOrder implements ExternalGraphOrder {
    @Override
    public void executeOn(GraphComponent graphComponent) {
        graphComponent.finalizeAlgorithmExecution();
    }
}
