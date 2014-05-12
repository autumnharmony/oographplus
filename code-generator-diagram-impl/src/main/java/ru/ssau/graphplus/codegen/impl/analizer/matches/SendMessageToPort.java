/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl.analizer.matches;

import com.google.inject.Inject;
import ru.ssau.graphplus.codegen.impl.CodeProviderAnnotation;
import ru.ssau.graphplus.codegen.impl.PortMessageCode;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.codegen.impl.recognition.LinkTypeRecogniser;

@CodeProviderAnnotation(codeProvider = PortMessageCode.class)
public class SendMessageToPort extends AbstractDataMatch {

    @Inject
    public SendMessageToPort(ShapeHelperWrapper shapeHelperWrapper, LinkTypeRecogniser linkTypeRecogniser) {
        super(shapeHelperWrapper, linkTypeRecogniser);
    }

    @Override
    public boolean matches(ConnectedShapesComplex connectedShapesComplex) {
        Node.NodeType toType = shapeHelperWrapper.getNodeType(connectedShapesComplex.toShape);
        Node.NodeType fromType = shapeHelperWrapper.getNodeType(connectedShapesComplex.fromShape);
        return super.matches(connectedShapesComplex) && (toType.equals(Node.NodeType.ClientPort) || toType.equals(Node.NodeType.ServerPort)) && (fromType.equals(Node.NodeType.MethodOfProcess) || fromType.equals(Node.NodeType.StartMethodOfProcess));
    }
}
