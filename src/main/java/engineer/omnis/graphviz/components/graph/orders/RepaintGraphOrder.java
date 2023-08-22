package engineer.omnis.graphviz.components.graph.orders;

import engineer.omnis.graphviz.components.graph.UpdatableGraphChild;
import lombok.Getter;

public abstract class RepaintGraphOrder implements ExternalGraphOrder {
    @Getter
    private final UpdatableGraphChild element;

    public RepaintGraphOrder(UpdatableGraphChild element) {
        this.element = element;
    }
}
