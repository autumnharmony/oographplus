/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.api;

public interface DiagramService {

    void addDiagramElement(DiagramElement diagramElement);
    void removeDiagramElement(DiagramElement diagramElement);

    Node createNode(String name, Node.NodeType nodeType);
    Link createLink(String name, Link.LinkType linkType);

    void insertNode(Node node);
    void insertLink(Link link);

    void linkNodes(Node node1, Node node2, Link link);
}
