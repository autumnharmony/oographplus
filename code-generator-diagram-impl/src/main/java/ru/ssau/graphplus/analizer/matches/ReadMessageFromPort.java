/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.analizer.matches;

import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.recognition.LinkTypeRecogniser;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 4/24/14
 * Time: 11:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReadMessageFromPort extends AbstractDataMatch {
    protected ReadMessageFromPort(ShapeHelperWrapper shapeHelperWrapper, LinkTypeRecogniser linkTypeRecogniser) {
        super(shapeHelperWrapper, linkTypeRecogniser);
    }

    @Override
    public boolean matches(ConnectedShapesComplex connectedShapesComplex) {
        Node.NodeType nodeType = shapeHelperWrapper.getNodeType(connectedShapesComplex.fromShape);
        return super.matches(connectedShapesComplex) && (nodeType.equals(Node.NodeType.ServerPort) || nodeType.equals(Node.NodeType.ClientPort));
    }
}
