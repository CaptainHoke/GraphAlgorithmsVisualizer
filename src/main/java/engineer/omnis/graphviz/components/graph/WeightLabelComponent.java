package engineer.omnis.graphviz.components.graph;

import engineer.omnis.graphviz.MainFrame;
import engineer.omnis.graphviz.Utility;
import engineer.omnis.graphviz.components.GraphEdgeComponent;
import engineer.omnis.graphviz.components.GraphNodeComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class WeightLabelComponent extends JComponent implements AddableGraphChild {
    public static final int containerRadius = 20;
    private final JLabel label;

    public WeightLabelComponent(String labelText, GraphEdgeComponent edge) {
        label = new JLabel(labelText);
        setName("EdgeLabel <" + edge.getConnectedVertices() + ">");
        setupBounds(edge);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    private void setupBounds(GraphEdgeComponent edge) {
        Rectangle edgeBoundingBox = edge.getBoundingBox();
        GraphNodeComponent from = edge.getFrom();
        GraphNodeComponent to = edge.getTo();

        int textWidth = 32;
        int textHeight = 50;
        int textTopPadding = 32;
        int labelX = Utility.clamp(edgeBoundingBox.x + edgeBoundingBox.width / 2 - textWidth / 2,
                0, MainFrame.SCREEN_WIDTH - textWidth);
        int labelY = Utility.clamp(edgeBoundingBox.y + edgeBoundingBox.height / 2 - textHeight / 2 + textTopPadding,
                0, MainFrame.SCREEN_HEIGHT - textHeight);

        setBounds(labelX, labelY, containerRadius * 2, containerRadius * 2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Ellipse2D e = new Ellipse2D.Double(0.0, 0.0, containerRadius * 2, containerRadius * 2);
        g2d.setColor(GraphComponentStyle.DEFAULT_BACKGROUND);
        g2d.fill(e);


    }

    @Override
    public void addToGraphImpl(GraphComponent graph) {
        graph.add(this);
    }

    @Override
    public void removeFromGraph(GraphComponent graph) {
        graph.remove(this);
    }

    @Override
    public void updateAppearance(GraphComponentStyle appearance) {
        setFont(appearance.weightLabelFont());
        setForeground(appearance.edgeWeightLabelColor());
    }

    @Override
    public void updateColor(Color color) {
        // TODO: Refactor interfaces
    }

    @Override
    public Rectangle getBoundingBox() {
        return getBounds();
    }
}
