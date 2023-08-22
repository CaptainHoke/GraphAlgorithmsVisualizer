package engineer.omnis.graphviz.components.graph.orders;

import engineer.omnis.graphviz.components.graph.GraphComponent;
import engineer.omnis.graphviz.components.graph.UpdatableGraphChild;

import java.awt.*;

public class ColorUpdateOrder extends RepaintGraphOrder {
    private final Color newColor;

    public ColorUpdateOrder(UpdatableGraphChild element, Color newColor) {
        super(element);
        this.newColor = newColor;
    }

    @Override
    public void executeOn(GraphComponent graphComponent) {
        getElement().updateColor(newColor);
    }
}
