/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.recognition.LinkTypeRecogniser;

public class PortMessageCode extends LinkCodeBase {
    public PortMessageCode(ConnectedShapesComplex connectedShapesComplex, LinkTypeRecogniser linkTypeRecogniser, ShapeHelperWrapper shapeHelper) {
        super(connectedShapesComplex, linkTypeRecogniser, shapeHelper);
    }

    @Override
    public String getCode() {

        StringBuffer buffer = new StringBuffer();

        boolean methodIsFrom = type(from()).equals(Node.NodeType.StartMethodOfProcess) || type(from()).equals(Node.NodeType.MethodOfProcess);

        boolean methodIsTo = false;
        if (methodIsFrom) {
            buffer.append(text(from()));
        } else {
            methodIsTo = type(from()).equals(Node.NodeType.ServerPort) || type(from()).equals(Node.NodeType.ClientPort);
            if (methodIsTo) {
                buffer.append(text(to()));
            }
        }

        buffer.append("(");
        buffer.append(methodIsFrom ? text(to()) : text(from()));
        buffer.append(methodIsFrom ? "!" : "");
        buffer.append(methodIsTo ? "?" : "");
        buffer.append(text(text()));
        buffer.append(")");

        return buffer.toString();
    }
}
