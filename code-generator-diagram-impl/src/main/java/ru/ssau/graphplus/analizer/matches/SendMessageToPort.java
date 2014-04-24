/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.analizer.matches;

import com.google.inject.Inject;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.recognition.LinkTypeRecogniser;

public class SendMessageToPort extends AbstractDataMatch {

    @Inject
    protected SendMessageToPort(ShapeHelperWrapper shapeHelperWrapper, LinkTypeRecogniser linkTypeRecogniser) {
        super(shapeHelperWrapper, linkTypeRecogniser);
    }

    @Override
    public boolean matches(ConnectedShapesComplex connectedShapesComplex) {
        Node.NodeType nodeType = shapeHelperWrapper.getNodeType(connectedShapesComplex.toShape);
        return super.matches(connectedShapesComplex) && (nodeType.equals(Node.NodeType.ClientPort) || nodeType.equals(Node.NodeType.ServerPort));
    }
}
