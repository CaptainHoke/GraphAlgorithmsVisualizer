package engineer.omnis.graphviz.components.graph;

import java.awt.*;

// I don't really like passing whole GraphComponent, but fixing this feels like over-engineering at this point
public interface AddableGraphChild extends UpdatableGraphChild {
    default void addToGraph(GraphComponent graph) {
        updateAppearance(graph.getAppearance());
        addToGraphImpl(graph);
    }

    void addToGraphImpl(GraphComponent graph);

    void removeFromGraph(GraphComponent graph);

    Rectangle getBoundingBox();
}
