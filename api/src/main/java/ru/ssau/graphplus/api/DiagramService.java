/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.api;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 4/6/14
 * Time: 6:11 PM
 * To change this template use File | Settings | File Templates.
 */
public interface DiagramService {

    void addDiagramElement(DiagramElement diagramElement);
    void removeDiagramElement(DiagramElement diagramElement);

    void linkNodes(Node node1, Node node2, Link link);
}
