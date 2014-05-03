package ru.ssau.graphplus.api;

public interface Link extends
        DiagramElement {

    LinkType getType();

    Node getStartNode();

    Node getEndNode();

    void setStartNode(Node node1);

    void setEndNode(Node node2);

    boolean isConnected();

    void setProps();

    void link(Node node1, Node node2);

    String getName();

    void setName(String name);

    public enum LinkType {
        DataFlow,
        ControlFlow,
        MixedFlow,
    }


}
