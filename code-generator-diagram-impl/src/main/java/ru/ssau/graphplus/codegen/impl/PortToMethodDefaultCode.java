/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl;

import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.codegen.impl.recognition.LinkTypeRecogniser;

public class PortToMethodDefaultCode extends LinkCodeBase implements PortToMethodCode{


    public PortToMethodDefaultCode(ConnectedShapesComplex connectedShapesComplex, LinkTypeRecogniser linkTypeRecogniser, ShapeHelperWrapper shapeHelper) {
        super(connectedShapesComplex, linkTypeRecogniser, shapeHelper);
    }

    @Override
    public String getCode() {


        if (toNodeType().equals(Node.NodeType.StartMethodOfProcess)) {
            return text(from()) + getPortChar(from()) + " -> " + text(to()) + ";";
        } else if (toNodeType().equals(Node.NodeType.MethodOfProcess)) {
            return text(from()) + getPortChar(from()) + " x->Y | " + " -> " + text(to()) + ";";
        } else throw new IllegalStateException();
    }
}
