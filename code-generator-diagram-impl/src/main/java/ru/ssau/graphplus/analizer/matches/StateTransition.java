/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.analizer.matches;

import ru.ssau.graphplus.CodeProviderAnnotation;
import ru.ssau.graphplus.StateTransitionCode;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.recognition.LinkTypeRecogniser;

@CodeProviderAnnotation(codeProvider = StateTransitionCode.class)
public class StateTransition extends AbstractControlMatch {

    public StateTransition(ShapeHelperWrapper shapeHelperWrapper, LinkTypeRecogniser linkTypeRecogniser) {
        super(shapeHelperWrapper, linkTypeRecogniser);
    }

    @Override
    public boolean matches(ConnectedShapesComplex connectedShapesComplex) {
        Node.NodeType nodeType = shapeHelperWrapper.getNodeType(connectedShapesComplex.fromShape);
        return super.matches(connectedShapesComplex) && (nodeType.equals(Node.NodeType.ClientPort) || nodeType.equals(Node.NodeType.ServerPort) || nodeType.equals(Node.NodeType.StartMethodOfProcess));
    }
}
