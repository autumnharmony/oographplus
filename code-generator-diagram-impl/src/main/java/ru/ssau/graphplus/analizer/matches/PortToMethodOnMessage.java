/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.analizer.matches;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.recognition.LinkTypeRecogniser;

public class PortToMethodOnMessage extends AbstractControlMatch {


    @Inject
    protected PortToMethodOnMessage(ShapeHelperWrapper shapeHelperWrapper, LinkTypeRecogniser linkTypeRecogniser) {
        super(shapeHelperWrapper, linkTypeRecogniser);
    }

    @Override
    public boolean matches(ConnectedShapesComplex connectedShapesComplex) {
        return (shapeHelperWrapper.getNodeType(connectedShapesComplex.fromShape).equals(Node.NodeType.ClientPort)
                || shapeHelperWrapper.getNodeType(connectedShapesComplex.fromShape).equals(Node.NodeType.ServerPort))
                && shapeHelperWrapper.getNodeType(connectedShapesComplex.toShape).equals(Node.NodeType.MethodOfProcess)
                && !Strings.isNullOrEmpty(connectedShapesComplex.getConnectorText())
                && super.matches(connectedShapesComplex);
    }
}
