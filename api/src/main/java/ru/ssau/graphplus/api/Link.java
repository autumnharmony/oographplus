package ru.ssau.graphplus.api;

public interface Link extends
        DiagramElement {

    LinkType getType();

    Node getStartNode();

    Node getEndNode();

    void setStartNode(Node node1);

    void setEndNode(Node node2);




    public enum LinkType {
        DataFlow,
        ControlFlow,
        MixedFlow,
    }


}