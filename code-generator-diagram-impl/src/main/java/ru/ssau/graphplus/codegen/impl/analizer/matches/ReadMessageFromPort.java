/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl.analizer.matches;

import ru.ssau.graphplus.codegen.impl.CodeProviderAnnotation;
import ru.ssau.graphplus.codegen.impl.PortMessageCode;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.codegen.impl.recognition.LinkTypeRecogniser;


@CodeProviderAnnotation(codeProvider = PortMessageCode.class)
public class ReadMessageFromPort extends AbstractDataMatch {
    public ReadMessageFromPort(ShapeHelperWrapper shapeHelperWrapper, LinkTypeRecogniser linkTypeRecogniser) {
        super(shapeHelperWrapper, linkTypeRecogniser);
    }

    @Override
    public boolean matches(ConnectedShapesComplex connectedShapesComplex) {
        Node.NodeType nodeType = shapeHelperWrapper.getNodeType(connectedShapesComplex.fromShape);
        return super.matches(connectedShapesComplex) && (nodeType.equals(Node.NodeType.ServerPort) || nodeType.equals(Node.NodeType.ClientPort));
    }
}
