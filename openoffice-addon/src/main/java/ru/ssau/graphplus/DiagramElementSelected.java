package ru.ssau.graphplus;

import ru.ssau.graphplus.api.DiagramElement;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.events.Event;

/**
 * Created by 1 on 04.06.14.
 */
public class DiagramElementSelected implements Event {

    protected final DiagramElement diagramElement;

    public DiagramElementSelected(DiagramElement diagramElement) {
        this.diagramElement = diagramElement;
    }

    public DiagramElement getDiagramElement() {
        return diagramElement;
    }

    public static DiagramElementSelected create(DiagramElement diagramElement){
        if (diagramElement instanceof Node){
            return new NodeSelectedEvent((Node)diagramElement);
        }
        else if (diagramElement instanceof Link){
            return new LinkSelectedEvent((Link)diagramElement);
        }
        else {
            throw new IllegalArgumentException();
        }
    }
}
