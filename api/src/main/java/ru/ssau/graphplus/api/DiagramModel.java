/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.api;

import java.util.Collection;

public interface DiagramModel {

    DiagramModel addDiagramElement(DiagramElement de);

    void removeDiagramElement(DiagramElement de);

    Collection<DiagramElement> getDiagramElements();

    Collection<Link> getLinks();

    Collection<Node> getNodes();



}
