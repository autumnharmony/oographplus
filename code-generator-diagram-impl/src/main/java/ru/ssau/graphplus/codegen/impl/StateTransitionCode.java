/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl;

import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.codegen.impl.recognition.LinkTypeRecogniser;

public class StateTransitionCode extends LinkCodeBase {

    public StateTransitionCode(ConnectedShapesComplex connectedShapesComplex, LinkTypeRecogniser linkTypeRecogniser, ShapeHelperWrapper shapeHelper) {
        super(connectedShapesComplex, linkTypeRecogniser, shapeHelper);
    }

    @Override
    public String getCode() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(type(from()).equals(Node.NodeType.StartMethodOfProcess) ? "+" : "");
        buffer.append(text(from()));
        buffer.append(type(from()).equals(Node.NodeType.ClientPort) ? "?" : "");
        buffer.append(type(from()).equals(Node.NodeType.ServerPort) ? "!" : "");
        buffer.append(type(from()).equals(Node.NodeType.StartMethodOfProcess) ? "!" : "");
        buffer.append(text(textShape()));
        buffer.append(" -> ");
        buffer.append(text(to()));
        buffer.append(";");
        return buffer.toString();
    }
}
