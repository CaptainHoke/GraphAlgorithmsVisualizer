package engineer.omnis.graphviz.graph.orders;

import engineer.omnis.graphviz.graph.UpdatableGraphChild;
import lombok.Getter;

public abstract class RepaintGraphOrder implements ExternalGraphOrder {
    @Getter
    private final UpdatableGraphChild element;

    public RepaintGraphOrder(UpdatableGraphChild element) {
        this.element = element;
    }
}
