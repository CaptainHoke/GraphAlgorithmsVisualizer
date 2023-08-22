package engineer.omnis.graphviz.components.graph;

import engineer.omnis.graphviz.GraphModel;
import engineer.omnis.graphviz.MainFrame;
import engineer.omnis.graphviz.Utility;
import engineer.omnis.graphviz.algorithms.GraphAlgorithmStrategy;
import engineer.omnis.graphviz.components.DefaultMouseListener;
import engineer.omnis.graphviz.components.GraphEdgePair;
import engineer.omnis.graphviz.components.GraphNodeComponent;
import engineer.omnis.graphviz.components.graph.orders.ExternalGraphOrder;
import engineer.omnis.graphviz.components.graph.orders.FinalizeAlgorithmOrder;
import engineer.omnis.graphviz.components.graph.orders.RepaintGraphOrder;

import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class GraphComponent extends JPanel {
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("-?\\d+");
    private static final int ORDER_EXECUTION_DELAY = 500;
    private final GraphComponentStyle appearance;
    private final GraphModel<GraphNodeComponent, GraphEdgePair> graphModel;
    private final Set<GraphNodeComponent> activeNodes = new HashSet<>();
    private final LinkedList<AddableGraphChild> graphEdgeStorage = new LinkedList<>();
    private final Queue<List<ExternalGraphOrder>> commandList = new LinkedList<>();
    private final List<UpdatableGraphChild> dirtyComponents = new ArrayList<>();
    private final Timer graphUpdateTimer;
    private Dimension contentPaneDimensions;
    private GraphComponentMode componentMode = GraphComponentMode.EDIT_MODE;
    private EditModeOption editModeOption = EditModeOption.ADD_VERTEX;
    private GraphAlgorithmStrategy currentAlgorithm = null;
    private AlgorithmExecutionTask algorithmTask = null;

    public GraphComponent(GraphComponentStyle visualAppearance) {
        appearance = visualAppearance;
        graphModel = new GraphModel<>();
        setName("Graph");
        setLayout(null);
        setPreferredSize(new Dimension(MainFrame.SCREEN_WIDTH, MainFrame.SCREEN_HEIGHT));

        addMouseListener(new DefaultMouseListener() {
            // mouseClicked ignores some mouse clicks, which is not good from UX perspective in my opinion
            @Override
            public void mousePressed(MouseEvent e) {
                switch (componentMode) {
                    case EDIT_MODE -> {
                        restoreDirtyComponentsAppearance();
                        handleMouseClickOnGraphEdit(e);
                    }
                    case INPUT_MODE -> {
                        restoreDirtyComponentsAppearance();
                        handleMouseClickOnAlgorithmInput(e);
                    }
                    case ALGORITHM_MODE -> {
                        // Do nothing. Just observe
                    }
                    default -> {
                        throw new UnsupportedOperationException("Unsupported mode");
                    }
                }
            }
        });

        graphUpdateTimer = new Timer(ORDER_EXECUTION_DELAY, e -> {
            while (commandList.peek() != null) {
                var commands = commandList.poll();
                if (commands.size() == 0) {
                    continue;
                }

                applyGraphUpdateOrders(commands);
                break;
            }
        });
    }

    @SuppressWarnings("checkstyle:magicnumber")
    public void generateFullyConnectedGraph(int n) {
        if (n > 9) {
            throw new IllegalArgumentException("TODO");
        }
        resetGraphState();

        double angle = Math.PI / 2;

        int offset = 50;
        int distToCenter = (Math.min(contentPaneDimensions.height, contentPaneDimensions.width)) / 2 - offset;

        Consumer<Point> centerToScreenConverter = (p) -> {
            p.x = Utility.remap(p.x, -distToCenter, distToCenter, contentPaneDimensions.width / 2 - distToCenter, contentPaneDimensions.width / 2 + distToCenter);
            p.y = Utility.remap(p.y, -distToCenter, distToCenter, contentPaneDimensions.height / 2 + distToCenter, contentPaneDimensions.height / 2 - distToCenter);
        };

        for (int i = 0; i < n; ++i) {
            int x = (int) (Math.sin(angle) * distToCenter);
            int y = (int) (Math.cos(angle) * distToCenter);
            Point p = new Point(x, y);
            centerToScreenConverter.accept(p);
            addNewVertex(Character.forDigit(i, 10), p);
            angle += Math.PI * 2 / n;
        }

        List<GraphNodeComponent> vertices = graphModel.getVertices().stream().toList();

        for (int i = 0; i < vertices.size(); ++i) {
            for (int j = i + 1; j < vertices.size(); ++j) {
                connectVertices(vertices.get(i), vertices.get(j), 1);
            }
        }
    }

    public GraphComponentStyle getAppearance() {
        return appearance;
    }

    public void resetGraphState() {
        // TODO: Replace with annotations
        if (isClosedForModification()) {
            return;
        }

        removeAll();
        graphModel.resetGraphState();
        activeNodes.clear();
        graphEdgeStorage.clear();
        dirtyComponents.clear();
        resetAlgorithmState();

        repaint();
    }

    public void restoreDirtyComponentsAppearance() {
        performActionOnComponentsAndRepaint(dirtyComponents, false, (c) -> c.updateAppearance(getAppearance()));
        dirtyComponents.clear();
    }

    public void addNewVertex(char vertexId, Point pos) {
        if (isClosedForModification()) {
            return;
        }

        GraphNodeComponent newNode = new GraphNodeComponent(vertexId, pos, contentPaneDimensions);
        if (!graphModel.addVertex(newNode)) {
            return;
        }

        resetActiveNodes();
        activeNodes.add(newNode);
        performActionOnComponentsAndRepaint(List.of(newNode), true, c -> c.addToGraph(this));
    }

    public void connectVertices(GraphNodeComponent first, GraphNodeComponent second, int weight) {
        if (isClosedForModification()) {
            return;
        }

        if (graphModel.areConnected(first, second) || graphModel.areConnected(second, first)) {
            return;
        }

        if (first.distanceTo(second) < first.getRadius() + second.getRadius() + WeightLabelComponent.containerRadius * 2) {
            return;
        }

        GraphEdgePair edgePair = GraphEdgePair.createEdgePair(this, first, second, weight);
        graphModel.connectVertices(first, second, edgePair);
        graphModel.connectVertices(second, first, edgePair);

        graphEdgeStorage.add(edgePair);
        performActionOnComponentsAndRepaint(List.of(edgePair), true, c -> c.addToGraph(this));
    }

    public void removeVertex(GraphNodeComponent vertex) {
        if (isClosedForModification()) {
            return;
        }

        List<AddableGraphChild> componentsToRemove = new ArrayList<>(graphModel.getEdges(vertex));
        graphModel.removeVertex(vertex);
        graphEdgeStorage.removeAll(componentsToRemove);

        componentsToRemove.add(vertex);
        performActionOnComponentsAndRepaint(componentsToRemove, true, c -> c.removeFromGraph(this));
    }

    private void removeEdge(GraphEdgePair edgePair) {
        if (isClosedForModification()) {
            return;
        }

        graphModel.removeConnection(edgePair.getMainEdge().getFrom(), edgePair.getMainEdge().getTo());
        graphEdgeStorage.removeIf(e -> e.equals(edgePair));
        performActionOnComponentsAndRepaint(List.of(edgePair), true, c -> c.removeFromGraph(this));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        var parent = SwingUtilities.getWindowAncestor(this);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(appearance.backgroundColor());
        var rf = new Rectangle2D.Float(0, 0, parent.getWidth(), parent.getHeight());
        g2d.fill(rf);
    }

    public void addActiveNode(GraphNodeComponent graphNode) {
        if (isClosedForModification()) {
            return;
        }

        graphNode.setActive(true);
        activeNodes.add(graphNode);
        performActionOnComponentsAndRepaint(List.of(graphNode), false, (c) -> c.updateAppearance(getAppearance()));
    }

    public void resetActiveNodes() {
        if (isClosedForModification()) {
            return;
        }

        for (var node : activeNodes) {
            node.setActive(false);
        }
        performActionOnComponentsAndRepaint(activeNodes.stream().toList(), false, (c) -> c.updateAppearance(getAppearance()));
        activeNodes.clear();
    }

    public void setContentPaneDimensions(Insets insets) {
        var parent = SwingUtilities.getWindowAncestor(this);
        contentPaneDimensions = new Dimension(parent.getWidth() - insets.left - insets.right,
                parent.getHeight() - insets.top - insets.bottom);
    }

    public EditModeOption getControlMode() {
        return editModeOption;
    }

    public void setEditModeOption(EditModeOption editModeOption) {
        restoreDirtyComponentsAppearance();
        this.editModeOption = editModeOption;
        resetActiveNodes();
    }

    private void processUserInput(String title, String msg, Predicate<String> validator, Consumer<String> action) {
        var parent = SwingUtilities.getRootPane(this);

        String userInput = "";
        while (userInput != null && !validator.test(userInput)) {
            userInput = JOptionPane.showInputDialog(
                    parent,
                    msg,
                    title,
                    JOptionPane.QUESTION_MESSAGE);
        }

        if (userInput != null) {
            action.accept(userInput);
        }
    }

    @SuppressWarnings("checkstyle:needbraces")
    private void handleMouseClickOnGraphEdit(MouseEvent e) {
        if (!(e.getSource() instanceof GraphNodeComponent)) {
            resetActiveNodes();
        }

        switch (editModeOption) {
            case ADD_VERTEX -> {
                if (e.getSource() instanceof GraphNodeComponent || e.getSource() instanceof GraphEdgePair) return;

                processUserInput("Vertex", "Enter the Vertex ID (Should be 1 char):",
                        s -> s.trim().length() == 1, input -> addNewVertex(input.charAt(0), e.getPoint()));
            }
            case ADD_EDGES -> {
                if (!(e.getSource() instanceof GraphNodeComponent)) return;

                if (activeNodes.isEmpty()) {
                    addActiveNode((GraphNodeComponent) e.getSource());
                } else if (activeNodes.size() == 1) {
                    GraphNodeComponent[] nodes = activeNodes.toArray(new GraphNodeComponent[0]);
                    if (nodes[0].equals(e.getSource())) return;

                    processUserInput("Edge", "Enter the Edge Weight:",
                            s -> NUMERIC_PATTERN.matcher(s).matches(), input -> {
                                connectVertices(nodes[0], (GraphNodeComponent) e.getSource(), Integer.parseInt(input));
                                resetActiveNodes();
                            });
                }
            }
            case NONE -> {
                if (e.getSource() instanceof GraphNodeComponent) {
                    resetActiveNodes();
                    addActiveNode((GraphNodeComponent) e.getSource());
                }
            }
            case REMOVE_VERTEX -> {
                if (!(e.getSource() instanceof GraphNodeComponent)) return;

                removeVertex((GraphNodeComponent) e.getSource());
            }
            case REMOVE_EDGE -> {
                if (!(e.getSource() instanceof GraphEdgePair)) return;

                removeEdge((GraphEdgePair) e.getSource());
            }
            default -> throw new UnsupportedOperationException("Wrong edit mode");
        }
    }

    public GraphModel<GraphNodeComponent, GraphEdgePair> getGraphModel() {
        return graphModel;
    }

    private void applyGraphUpdateOrders(List<ExternalGraphOrder> commands) {
        for (var command : commands) {
            command.executeOn(this);
        }

        List<UpdatableGraphChild> components =
                commands.stream().filter(c -> c instanceof RepaintGraphOrder)
                        .map(c -> ((RepaintGraphOrder) c).getElement()).toList();
        dirtyComponents.addAll(components);
        performActionOnComponentsAndRepaint(components, false, (c) -> {
        });
    }

    private void handleMouseClickOnAlgorithmInput(MouseEvent e) {
        if (!currentAlgorithm.isAcceptingInput()) {
            return;
        }

        if (e.getSource() instanceof GraphNodeComponent) {
            currentAlgorithm.onVertexInput((GraphNodeComponent) e.getSource());
        } else if (e.getSource() instanceof GraphEdgePair) {
            currentAlgorithm.onEdgeInput((GraphEdgePair) e.getSource());
        } else {
            currentAlgorithm.onAborted();
            resetAlgorithmState();
        }

        if (currentAlgorithm != null && currentAlgorithm.isReadyToRun()) {
            currentAlgorithm.setAcceptingInput(false);
            currentAlgorithm.postInit();
            componentMode = GraphComponentMode.ALGORITHM_MODE;

            algorithmTask = new AlgorithmExecutionTask();
            algorithmTask.execute();
        }
    }

    private <T extends UpdatableGraphChild> void performActionOnComponentsAndRepaint(List<T> components, boolean revalidate, Consumer<T> perComponentFunction) {
        Rectangle boundingBox = new Rectangle();

        for (var c : components) {
            if (c == null) {
                continue;
            }
            boundingBox = boundingBox.union(c.getBoundingBox());
            perComponentFunction.accept(c);
        }

        if (revalidate) {
            revalidate();
        }
        repaint(boundingBox);
    }

    public Component getGraphNodeAt(Point pos) {
        for (var component : getComponents()) {
            if (component != null && component.isVisible() && component instanceof GraphNodeComponent) {
                Point localCoordinates = SwingUtilities.convertPoint(this, pos, component);
                if (component.contains(localCoordinates)) {
                    return component;
                }
            }
        }

        return null;
    }

    public List<AddableGraphChild> getEdgePairsAt(Point pos) {
        List<AddableGraphChild> edgePairs = new LinkedList<>();
        for (var edgePair : graphEdgeStorage) {
            if (edgePair.getBoundingBox().contains(pos)) {
                edgePairs.add(edgePair);
            }
        }
        return edgePairs;
    }

    public void initiateAlgorithmVisualization(GraphAlgorithmStrategy algorithm) {
        if (isClosedForModification()) {
            return;
        }

        restoreDirtyComponentsAppearance();
        algorithm.preInit();
        currentAlgorithm = algorithm;
        componentMode = GraphComponentMode.INPUT_MODE;
    }

    public void finalizeAlgorithmExecution() {
        if (algorithmTask.isDone()) {
            currentAlgorithm.onComplete(currentAlgorithm.getResult());
        } else {
            currentAlgorithm.onAborted();
        }
        resetAlgorithmState();
    }

    public void resetAlgorithmState() {
        commandList.clear();
        currentAlgorithm = null;
        algorithmTask = null;
        graphUpdateTimer.stop();
        componentMode = GraphComponentMode.EDIT_MODE;
    }

    public boolean isClosedForModification() {
        return componentMode != GraphComponentMode.EDIT_MODE;
    }

    public void abortAlgorithmExecution() {
        if (algorithmTask == null) {
            return;
        }
        algorithmTask.cancel(true);
        graphUpdateTimer.stop();
        finalizeAlgorithmExecution();
        resetAlgorithmState();
    }

    private enum GraphComponentMode {
        EDIT_MODE,
        INPUT_MODE,
        ALGORITHM_MODE
    }

    private class AlgorithmExecutionTask extends SwingWorker<String, List<ExternalGraphOrder>> {
        @Override
        protected void process(List<List<ExternalGraphOrder>> commands) {
            super.process(commands);
            commandList.addAll(commands);
        }

        @Override
        protected String doInBackground() {
            graphUpdateTimer.start();

            while (!currentAlgorithm.isFinished() && !isCancelled()) {
                publish(new LinkedList<>(currentAlgorithm.performStep()));
            }
            publish(List.of(new FinalizeAlgorithmOrder()));

            return currentAlgorithm.getResult();
        }
    }

}
