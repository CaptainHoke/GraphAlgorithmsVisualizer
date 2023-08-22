package engineer.omnis.graphviz.algorithms;

import engineer.omnis.graphviz.graph.components.GraphEdgePair;
import engineer.omnis.graphviz.graph.components.GraphNodeComponent;
import engineer.omnis.graphviz.graph.orders.ExternalGraphOrder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor
public abstract class GraphAlgorithmStrategy {
    private final Runnable preInitAction;
    private final Runnable postInitAction;
    private final Consumer<String> onCompleteAction;
    private final Runnable onAbortedAction;
    @Getter
    @Setter
    private boolean finished = false;
    @Getter
    @Setter
    private boolean acceptingInput = true;

    public void preInit() {
        preInitAction.run();
    }

    public void postInit() {
        postInitAction.run();
    }

    public void onComplete(String result) {
        onCompleteAction.accept(result);
    }

    public void onAborted() {
        onAbortedAction.run();
    }

    public boolean isReadyToRun() {
        return false;
    }

    public abstract List<ExternalGraphOrder> performStep();

    public String getResult() {
        return "";
    }

    public void onVertexInput(GraphNodeComponent c) {
        System.out.println("Got vertex input");
    }

    public void onEdgeInput(GraphEdgePair c) {
        System.out.println("Got edge input");
    }
}
