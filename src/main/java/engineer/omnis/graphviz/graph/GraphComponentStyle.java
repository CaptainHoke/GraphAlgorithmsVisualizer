package engineer.omnis.graphviz.graph;

import java.awt.*;

public record GraphComponentStyle(Color backgroundColor, Color inactiveNodeColor, Color activeNodeColor,
                                  Color edgeWeightLabelColor, Color edgeColor, int edgeStrokeSize,
                                  Font weightLabelFont, Font nodeLabelFont) {
    public static final Color DEFAULT_COLOR = new Color(255, 0, 255);
    public static final Color DEFAULT_BACKGROUND = new Color(43, 43, 43);
    public static final Color DEFAULT_INACTIVE_NODE_COLOR = new Color(204, 120, 50);
    public static final Color DEFAULT_ACTIVE_NODE_COLOR = new Color(255, 197, 108);
    public static final Color DEFAULT_WEIGHT_TEXT_COLOR = new Color(160, 185, 35);
    public static final Color DEFAULT_EDGE_COLOR = Color.WHITE;

    @SuppressWarnings("checkstyle:magicnumber")
    public static GraphComponentStyle getDefaultStyle() {
        return new GraphComponentStyle(
                DEFAULT_BACKGROUND,
                DEFAULT_INACTIVE_NODE_COLOR,
                DEFAULT_ACTIVE_NODE_COLOR,
                DEFAULT_WEIGHT_TEXT_COLOR,
                DEFAULT_EDGE_COLOR,
                6,
                new Font(Font.SANS_SERIF, Font.PLAIN, 24),
                new Font(Font.SANS_SERIF, Font.PLAIN, 24));
    }
}
