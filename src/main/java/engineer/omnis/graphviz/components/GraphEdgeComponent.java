package engineer.omnis.graphviz.components;

import engineer.omnis.graphviz.components.graph.AddableGraphChild;
import engineer.omnis.graphviz.components.graph.GraphComponent;
import engineer.omnis.graphviz.components.graph.GraphComponentStyle;
import engineer.omnis.graphviz.components.graph.UpdatableGraphChild;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

// GraphEdgeComponent is not an AddableGraphChild, but GraphEdgePair is
// We still need to update graph edges' properties from within the GraphComponent
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class GraphEdgeComponent extends JComponent implements UpdatableGraphChild {
    @Getter
    @EqualsAndHashCode.Include
    private final GraphNodeComponent from;
    @Getter
    @EqualsAndHashCode.Include
    private final GraphNodeComponent to;
    @Getter
    private final int weight;
    @Setter
    private GraphEdgePair edgePair;
    private BasicStroke edgeStroke = new BasicStroke();
    private Color edgeColor = GraphComponentStyle.DEFAULT_COLOR;

    GraphEdgeComponent(GraphComponent parentGraph, GraphNodeComponent from, GraphNodeComponent to, int weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;

        setLayout(new BorderLayout());
        setName("Edge <" + from.getId() + " -> " + to.getId() + ">");

        Rectangle r = from.getBoundingBox(to);
        setBounds(r);

        Container graphContainer = SwingUtilities.getUnwrappedParent(from);
        if (!(graphContainer instanceof GraphComponent)) {
            throw new IllegalStateException("Couldn't find GraphComponent");
        }

        addMouseListener(new DefaultMouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                Graphics2D g2d = (Graphics2D) getGraphics();
                g2d.setStroke(edgeStroke);

                Point screenCoordinates = SwingUtilities.convertPoint(GraphEdgeComponent.this, e.getPoint(), graphContainer);

                // TODO: Add quadtree or smth
                for (AddableGraphChild element : parentGraph.getEdgePairsAt(screenCoordinates)) {
                    GraphEdgePair edgePairToTest = (GraphEdgePair) element;
                    Point2D localCoordinates = SwingUtilities.convertPoint(GraphEdgeComponent.this, e.getPoint(), edgePairToTest.getMainEdge());
                    Rectangle hitTestRectangle = new Rectangle((int) localCoordinates.getX(), (int) localCoordinates.getY(), 1, 1);

                    if (g2d.hit(hitTestRectangle, edgePairToTest.getEdgeShape(), true)) {
                        e.setSource(edgePairToTest);
                        graphContainer.dispatchEvent(e);
                        return;
                    }
                }

                // I want to let vertices be placed in areas occupied by edges' bounding boxes
                Point graphCoordinates = SwingUtilities.convertPoint(GraphEdgeComponent.this, e.getPoint(), graphContainer);
                MouseEvent redispatchedEvent = new MouseEvent(graphContainer, e.getID(), e.getWhen(),
                        e.getModifiersEx(), (int) graphCoordinates.getX(), (int) graphCoordinates.getY(),
                        1, e.isPopupTrigger(), e.getButton());
                Component graphNode = parentGraph.getGraphNodeAt(graphCoordinates.getLocation());
                Component dispatchComponent = graphNode != null ? graphNode : graphContainer;
                dispatchComponent.dispatchEvent(redispatchedEvent);
            }
        });
    }

    public Rectangle getBoundingBox() {
        return from.getBoundingBox(to);
    }

    public String getConnectedVertices() {
        return from.getId() + " -> " + to.getId();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setStroke(edgeStroke);
        g2d.setColor(edgeColor);
        g2d.draw(edgePair.getEdgeShape());
    }

    @Override
    public void updateAppearance(GraphComponentStyle appearance) {
        edgeStroke = new BasicStroke(appearance.edgeStrokeSize());
        edgeColor = appearance.edgeColor();
    }

    @Override
    public void updateColor(Color color) {
        edgeColor = color;
    }
}
