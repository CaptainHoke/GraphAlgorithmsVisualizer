package engineer.omnis.graphviz;

import engineer.omnis.graphviz.algorithms.*;
import engineer.omnis.graphviz.components.graph.EditModeOption;
import engineer.omnis.graphviz.components.graph.GraphComponent;
import engineer.omnis.graphviz.components.graph.GraphComponentStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 600;
    private GraphComponent graph = null;
    private JLabel modeLabel;
    private JLabel infoLabel;

    public MainFrame() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setName("Graph-Algorithms Visualizer");
        setTitle("Graph-Algorithms Visualizer");

        installUiElements();
        setVisible(true);
        pack();
        setLocationRelativeTo(null);
        setSize(SCREEN_WIDTH, SCREEN_HEIGHT);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                super.windowOpened(e);

                graph.setContentPaneDimensions(getInsets());
                graph.revalidate();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);

                graph.abortAlgorithmExecution();
            }
        });

        getRootPane().registerKeyboardAction(e -> {
            if (graph == null) {
                return;
            }
            graph.abortAlgorithmExecution();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    private void installUiElements() {
        setLayout(new BorderLayout());
        graph = new GraphComponent(GraphComponentStyle.getDefaultStyle());
        add(graph, BorderLayout.CENTER);

        JPanel glass = ((JPanel) getGlassPane());
        glass.setVisible(true);
        SpringLayout layout = new SpringLayout();
        glass.setLayout(layout);

        installMenuBar();
        installInfoLabel();
        installModeLabel();
        setGraphEditModeOption(EditModeOption.ADD_VERTEX);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    private void installInfoLabel() {
        infoLabel = new JLabel("Algorithm output will be shown here", SwingConstants.CENTER);
        infoLabel.setVisible(true);
        infoLabel.setName("Display");
        infoLabel.setOpaque(true);
        infoLabel.setBackground(new Color(60, 63, 65));
        infoLabel.setForeground(new Color(187, 187, 187));
        infoLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
    }

    @SuppressWarnings("checkstyle:magicnumber")
    private void installMenuBar() {
        String menuBarName = "MenuBar";
        JMenuBar menuBar = new JMenuBar();
        menuBar.setName(menuBarName);

        String modeMenuName = "Mode";
        JMenu modeMenu = new JMenu(modeMenuName);
        modeMenu.setName(modeMenuName);

        for (EditModeOption m : EditModeOption.values()) {
            JMenuItem menuItem = new JMenuItem(m.toString());
            menuItem.setName(m.toString());
            menuItem.addActionListener(e -> setGraphEditModeOption(m));
            modeMenu.add(menuItem);
        }

        String fileMenuName = "File";
        JMenu fileMenu = new JMenu(fileMenuName);
        fileMenu.setName(fileMenuName);

        String newItemName = "New";
        JMenuItem newMenuItem = new JMenuItem(newItemName);
        newMenuItem.setName(newItemName);
        newMenuItem.addActionListener(e -> graph.resetGraphState());

        String exitItemName = "Exit";
        JMenuItem exitMenuItem = new JMenuItem(exitItemName);
        exitMenuItem.setName(exitItemName);
        exitMenuItem.addActionListener(e -> dispose());

        fileMenu.add(newMenuItem);
        fileMenu.add(exitMenuItem);

        // Algos
        String algosMenuName = "Algorithms";
        JMenu algorithmsMenu = new JMenu(algosMenuName);
        algorithmsMenu.setName(algosMenuName);

        for (Algorithm algo : Algorithm.values()) {
            JMenuItem menuItem = new JMenuItem(algo.toString());
            menuItem.setName(algo.toString());
            menuItem.addActionListener(e -> initiateAlgorithmVisualization(algo));
            algorithmsMenu.add(menuItem);
        }

        // Presets
        String graphPresetsMenuName = "Presets";
        JMenu presetsMenu = new JMenu(graphPresetsMenuName);
        JMenuItem fullyConnected = new JMenuItem("Fully connected-6");
        fullyConnected.addActionListener(e -> graph.generateFullyConnectedGraph(6));
        presetsMenu.add(fullyConnected);

        menuBar.add(fileMenu);
        menuBar.add(modeMenu);
        menuBar.add(algorithmsMenu);
        menuBar.add(presetsMenu);
        setJMenuBar(menuBar);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    private void installModeLabel() {
        modeLabel = new JLabel("Placeholder");
        modeLabel.setName("Mode");
        modeLabel.setForeground(Color.white);

        JPanel glass = ((JPanel) getGlassPane());
        SpringLayout layout = (SpringLayout) glass.getLayout();
        layout.putConstraint(SpringLayout.WEST, modeLabel, 10, SpringLayout.WEST, glass);
        layout.putConstraint(SpringLayout.SOUTH, modeLabel, -5, SpringLayout.SOUTH, glass);

        glass.add(modeLabel);
    }

    private void setGraphEditModeOption(EditModeOption editModeOption) {
        if (graph.isClosedForModification()) {
            return;
        }

        graph.setEditModeOption(editModeOption);
        hideInfoLabel();
        modeLabel.setText(graph.getControlMode().toString());
    }

    private void initiateAlgorithmVisualization(Algorithm algorithm) {
        if (graph.isClosedForModification()) {
            return;
        }

        setGraphEditModeOption(EditModeOption.NONE);
        showInfoLabel();
        GraphAlgorithmStrategy strategy = switch (algorithm) {
            case DFS -> getDefaultAlgorithmStrategy(GraphDFSStrategy::new);
            case BFS -> getDefaultAlgorithmStrategy(GraphBFSStrategy::new);
            case DIJKSTRA -> getDefaultAlgorithmStrategy(GraphDijkstraStrategy::new);
            case PRIM -> getDefaultAlgorithmStrategy(GraphPrimStrategy::new);
        };
        graph.initiateAlgorithmVisualization(strategy);
    }

    private void updateInfoLabel(String text) {
        infoLabel.setText(text);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    private void showInfoLabel() {
        JPanel glass = (JPanel) getGlassPane();
        if (infoLabel.getParent() == glass) {
            return;
        }

        SpringLayout layout = (SpringLayout) glass.getLayout();
        layout.removeLayoutComponent(infoLabel);
        layout.removeLayoutComponent(modeLabel);
        layout.putConstraint(SpringLayout.WEST, infoLabel, 0, SpringLayout.WEST, glass);
        layout.putConstraint(SpringLayout.EAST, infoLabel, 0, SpringLayout.EAST, glass);
        layout.putConstraint(SpringLayout.SOUTH, infoLabel, 0, SpringLayout.SOUTH, glass);
        layout.putConstraint(SpringLayout.WEST, modeLabel, 10, SpringLayout.WEST, glass);
        layout.putConstraint(SpringLayout.SOUTH, modeLabel, -5, SpringLayout.NORTH, infoLabel);

        glass.add(infoLabel);
        glass.revalidate();
        glass.repaint();
    }

    @SuppressWarnings("checkstyle:magicnumber")
    private void hideInfoLabel() {
        JPanel glass = (JPanel) getGlassPane();
        SpringLayout layout = (SpringLayout) glass.getLayout();
        layout.removeLayoutComponent(infoLabel);
        layout.removeLayoutComponent(modeLabel);
        layout.putConstraint(SpringLayout.WEST, modeLabel, 10, SpringLayout.WEST, glass);
        layout.putConstraint(SpringLayout.SOUTH, modeLabel, -5, SpringLayout.SOUTH, glass);
        glass.remove(infoLabel);
        glass.revalidate();
        glass.repaint();
    }

    private GraphAlgorithmStrategy getDefaultAlgorithmStrategy(GraphStrategyConstructor constructor) {
        return constructor.apply(
                graph.getGraphModel(),
                () -> updateInfoLabel("Please choose a starting vertex"),
                () -> updateInfoLabel("Please wait..."),
                this::updateInfoLabel,
                () -> updateInfoLabel("Algorithm execution aborted"));
    }

    private enum Algorithm {
        DFS("Depth-First Search"),
        BFS("Breadth-First Search"),
        DIJKSTRA("Dijkstra's Algorithm"),
        PRIM("Prim's Algorithm");

        private final String displayName;

        Algorithm(String s) {
            displayName = s;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}
