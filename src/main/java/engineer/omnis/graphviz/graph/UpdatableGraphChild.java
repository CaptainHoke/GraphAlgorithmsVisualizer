package engineer.omnis.graphviz.graph;

import java.awt.*;

public interface UpdatableGraphChild {
    void updateAppearance(GraphComponentStyle appearance);

    void updateColor(Color color);

    Rectangle getBoundingBox();
}
