/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;


import ru.ssau.graphplus.api.DiagramModel;

public class DiagramCodeSource implements CodeSource {
    private final DiagramModel diagramModel;

    public DiagramModel getDiagramModel() {
        return diagramModel;
    }

    public DiagramCodeSource(DiagramModel diagramModel) {
        this.diagramModel = diagramModel;
    }
}
