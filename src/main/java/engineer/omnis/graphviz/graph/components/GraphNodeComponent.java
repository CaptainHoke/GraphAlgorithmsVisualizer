package engineer.omnis.graphviz.graph.components;

import engineer.omnis.graphviz.Utility;
import engineer.omnis.graphviz.graph.DefaultMouseListener;
import engineer.omnis.graphviz.graph.AddableGraphChild;
import engineer.omnis.graphviz.graph.GraphComponentStyle;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class GraphNodeComponent extends JPanel implements AddableGraphChild {
    @Getter
    private final int radius = 20;
    private final JLabel nodeLabel;
    private final Point pos;
    @Getter
    @EqualsAndHashCode.Include
    private char id = '@';
    private Color nodeColor = Color.WHITE;
    @Getter
    @Setter
    private boolean isActive = true;

    public GraphNodeComponent(char id, Point pos, Dimension contentPaneSize) {
        this.pos = pos;
        this.id = id;
        setLayout(new BorderLayout());
        setName("Vertex " + this.id);
        setOpaque(false);

        nodeLabel = new JLabel();
        nodeLabel.setName("VertexLabel " + this.id);
        nodeLabel.setText(Character.toString(this.id));
        nodeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(nodeLabel, BorderLayout.CENTER);

        setupBounds(pos, contentPaneSize);

        addMouseListener(new DefaultMouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                Container p = SwingUtilities.getUnwrappedParent(GraphNodeComponent.this);
                e.setSource(GraphNodeComponent.this);
                p.dispatchEvent(e);
            }
        });
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
        nodeColor = isActive() ? appearance.activeNodeColor() : appearance.inactiveNodeColor();
        nodeLabel.setFont(appearance.nodeLabelFont());
    }

    @Override
    public void updateColor(Color color) {
        nodeColor = color;
    }

    @Override
    public Rectangle getBoundingBox() {
        return getBounds();
    }

    public void setupBounds(Point center, Dimension d) {
        center.x = Utility.clamp(center.x - radius, 0, d.width - 2 * radius);
        center.y = Utility.clamp(center.y - radius, 0, d.height - 2 * radius);
        setBounds(center.x, center.y, radius * 2, radius * 2);
    }

    public Point2D getCenterPos() {
        return new Point2D.Double(pos.x + radius, pos.y + radius);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Ellipse2D e = new Ellipse2D.Double(0.0, 0.0, radius * 2, radius * 2);
        g2d.setColor(nodeColor);
        g2d.fill(e);
    }

    public Rectangle getBoundingBox(GraphNodeComponent other) {
        return getBounds().union(other.getBounds());
    }

    public double distanceTo(GraphNodeComponent other) {
        return getCenterPos().distanceSq(other.getCenterPos());
    }
}
