/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.recognition.LinkTypeRecogniser;

public class PortToMethodDefaultCode extends LinkCodeBase {


    public PortToMethodDefaultCode(ConnectedShapesComplex connectedShapesComplex, LinkTypeRecogniser linkTypeRecogniser, ShapeHelperWrapper shapeHelper) {
        super(connectedShapesComplex, linkTypeRecogniser, shapeHelper);
    }

    @Override
    public String getCode() {
//        1->(3|4): port: Type ! -> method; или port: Type ! x->Y | -> method;
//        2->(3|4): port: Type ? -> method; или port: Type ! x->Y | -> method;


        if (toNodeType().equals(Node.NodeType.StartMethodOfProcess)) {
            return text(from()) + getPortChar(from()) + " -> " + text(to()) + ";";
        } else if (toNodeType().equals(Node.NodeType.MethodOfProcess)) {
            return text(from()) + getPortChar(from()) + " x->Y | " + " -> " + text(to()) + ";";
        } else throw new IllegalStateException();
    }
}
