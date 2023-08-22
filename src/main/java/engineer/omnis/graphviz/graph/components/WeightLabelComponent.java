package engineer.omnis.graphviz.graph.components;

import engineer.omnis.graphviz.graph.AddableGraphChild;
import engineer.omnis.graphviz.graph.GraphComponentStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public class WeightLabelComponent extends JComponent implements AddableGraphChild {
    public static final int CONTAINER_RADIUS = 15;
    private final JLabel label;

    public WeightLabelComponent(String labelText, GraphEdgeComponent edge) {
        setLayout(new BorderLayout());

        label = new JLabel(labelText);
        label.setName("WeightLabel " + labelText);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);

        setName("EdgeLabel <" + edge.getConnectedVertices() + ">");
        setupBounds(edge);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    private void setupBounds(GraphEdgeComponent edge) {
        GraphNodeComponent from = edge.getFrom();
        GraphNodeComponent to = edge.getTo();

        double distanceFromSource = 0.2;
        Point2D fromPos = from.getCenterPos();
        Point2D toPos = to.getCenterPos();

        fromPos.setLocation(fromPos.getX() - CONTAINER_RADIUS, fromPos.getY() - CONTAINER_RADIUS);
        toPos.setLocation(toPos.getX() - CONTAINER_RADIUS, toPos.getY() - CONTAINER_RADIUS);

        double xLength = toPos.getX() - fromPos.getX();
        double yLength = toPos.getY() - fromPos.getY();
        double x = (xLength) * distanceFromSource + fromPos.getX();
        double y = (yLength) * distanceFromSource + fromPos.getY();

        int labelX = (int) x;
        int labelY = (int) y;

        setBounds(labelX, labelY, CONTAINER_RADIUS * 2, CONTAINER_RADIUS * 2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Ellipse2D e = new Ellipse2D.Double(0.0, 0.0, CONTAINER_RADIUS * 2, CONTAINER_RADIUS * 2);
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
        label.setFont(appearance.weightLabelFont());
        label.setForeground(appearance.edgeWeightLabelColor());
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
