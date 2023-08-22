package engineer.omnis.graphviz.graph.components;

import engineer.omnis.graphviz.graph.AddableGraphChild;
import engineer.omnis.graphviz.graph.GraphComponentStyle;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

// This class is needed because of 2 edges per vertex-to-vertex connection project requirement
// And edges should be children of graph itself
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GraphEdgePair implements AddableGraphChild {
    @Getter
    @EqualsAndHashCode.Include
    private final GraphEdgeComponent mainEdge;
    @EqualsAndHashCode.Include
    private final GraphEdgeComponent secondEdge;
    private final WeightLabelComponent weightLabel;
    @Getter
    private Shape edgeShape;

    public static GraphEdgePair createEdgePair(GraphComponent graph, GraphNodeComponent v1, GraphNodeComponent v2, int weight) {
        GraphEdgeComponent firstEdge = new GraphEdgeComponent(graph, v1, v2, weight);
        GraphEdgeComponent secondEdge = new GraphEdgeComponent(graph, v2, v1, weight);
        WeightLabelComponent weightLabel = new WeightLabelComponent(String.valueOf(weight), firstEdge);

        GraphEdgePair pair = new GraphEdgePair(firstEdge, secondEdge, weightLabel);
        pair.configureEdgeShape();
        firstEdge.setEdgePair(pair);
        secondEdge.setEdgePair(pair);

        return pair;
    }

    public int getWeight() {
        return mainEdge.getWeight();
    }

    private void configureEdgeShape() {
        GraphNodeComponent from = mainEdge.getFrom();
        GraphNodeComponent to = mainEdge.getTo();

        Point2D firstAbsolute = from.getCenterPos();
        Point2D secondAbsolute = to.getCenterPos();

        Point2D firstRelative = new Point2D.Double(
                firstAbsolute.getX() < secondAbsolute.getX() ? from.getRadius() : mainEdge.getWidth() - from.getRadius(),
                firstAbsolute.getY() < secondAbsolute.getY() ? from.getRadius() : mainEdge.getHeight() - from.getRadius());

        Point2D secondRelative = new Point2D.Double(
                firstAbsolute.getX() >= secondAbsolute.getX() ? from.getRadius() : mainEdge.getWidth() - from.getRadius(),
                firstAbsolute.getY() >= secondAbsolute.getY() ? from.getRadius() : mainEdge.getHeight() - from.getRadius());

        edgeShape = new Line2D.Double(firstRelative, secondRelative);
    }

    @Override
    public void addToGraphImpl(GraphComponent graph) {
        graph.add(weightLabel);
        graph.add(mainEdge);
        graph.add(secondEdge);
    }

    @Override
    public void removeFromGraph(GraphComponent graph) {
        graph.remove(mainEdge);
        graph.remove(secondEdge);
        graph.remove(weightLabel);
    }

    @Override
    public void updateAppearance(GraphComponentStyle appearance) {
        mainEdge.updateAppearance(appearance);
        secondEdge.updateAppearance(appearance);
        weightLabel.updateAppearance(appearance);
    }

    @Override
    public void updateColor(Color color) {
        mainEdge.updateColor(color);
        secondEdge.updateColor(color);
    }

    @Override
    public Rectangle getBoundingBox() {
        return mainEdge.getBoundingBox().union(secondEdge.getBoundingBox()).union(weightLabel.getBoundingBox());
    }
}
