/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl;


import ru.ssau.graphplus.api.DiagramModel;
import ru.ssau.graphplus.api.DiagramType;
import ru.ssau.graphplus.codegen.CodeSource;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;

import java.util.List;

public class DiagramCodeSource implements CodeSource {
    private final DiagramModel diagramModel;
    private final List<ConnectedShapesComplex> connectedShapesComplexes;
    private DiagramType diagramType;

    public DiagramModel getDiagramModel() {
        return diagramModel;
    }

    public DiagramCodeSource(DiagramModel diagramModel, List<ConnectedShapesComplex> connectedShapesComplexes, DiagramType diagramType) {
        this.diagramModel = diagramModel;
        this.connectedShapesComplexes = connectedShapesComplexes;
        this.diagramType = diagramType;
    }

    public List<ConnectedShapesComplex> getConnectedShapesComplexes() {
        return connectedShapesComplexes;
    }

    public DiagramType getDiagramType() {
        return diagramType;
    }
}
