/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;


import ru.ssau.graphplus.api.DiagramModel;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;

import java.util.List;

public class DiagramCodeSource implements CodeSource {
    private final DiagramModel diagramModel;
    private final List<ConnectedShapesComplex> connectedShapesComplexes;

    public DiagramModel getDiagramModel() {
        return diagramModel;
    }

    public DiagramCodeSource(DiagramModel diagramModel, List<ConnectedShapesComplex> connectedShapesComplexes) {
        this.diagramModel = diagramModel;
        this.connectedShapesComplexes = connectedShapesComplexes;
    }

    public List<ConnectedShapesComplex> getConnectedShapesComplexes() {
        return connectedShapesComplexes;
    }
}
