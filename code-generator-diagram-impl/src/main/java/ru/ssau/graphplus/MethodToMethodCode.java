/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.recognition.LinkTypeRecogniser;


public class MethodToMethodCode extends LinkCodeBase {

    public MethodToMethodCode(ConnectedShapesComplex connectedShapesComplex, LinkTypeRecogniser linkTypeRecogniser, ShapeHelperWrapper shapeHelper) {
        super(connectedShapesComplex, linkTypeRecogniser, shapeHelper);
    }

    @Override
    public String getCode() {

        StringBuffer buffer = new StringBuffer();
        if (type(from()).equals(Node.NodeType.StartMethodOfProcess)){
            buffer.append("+");
        }

        buffer.append(text(from())).append("()").append(" -> ").append(text(text()).equals("-") ? "|" : "").append(text(to()));
        return  buffer.toString();
    }
}
