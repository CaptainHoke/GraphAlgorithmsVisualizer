package engineer.omnis.graphviz.components.graph;

import java.awt.*;

public interface UpdatableGraphChild {
    void updateAppearance(GraphComponentStyle appearance);

    void updateColor(Color color);

    Rectangle getBoundingBox();
}
