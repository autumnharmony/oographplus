package ru.ssau.graphplus;

import ru.ssau.graphplus.api.DiagramElement;
import ru.ssau.graphplus.api.Node;

/**
 * Created by 1 on 04.06.14.
 */
public class NodeSelectedEvent extends DiagramElementSelected {
    public NodeSelectedEvent(Node diagramElement) {
        super(diagramElement);
    }

    public Node getNode(){
        return (Node) getDiagramElement();
    }


}
