package engineer.omnis.graphviz.components.graph;

public enum EditModeOption {
    ADD_VERTEX("Add a Vertex"),
    ADD_EDGES("Add an Edge"),
    REMOVE_VERTEX("Remove a Vertex"),
    REMOVE_EDGE("Remove an Edge"),
    NONE("None");

    private final String displayName;

    EditModeOption(String s) {
        displayName = s;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
