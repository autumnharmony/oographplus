/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.analizer.matches;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import ru.ssau.graphplus.CodeProviderAnnotation;
import ru.ssau.graphplus.PortToMethodOnMessageCode;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.recognition.LinkTypeRecogniser;

@CodeProviderAnnotation(codeProvider = PortToMethodOnMessageCode.class)
public class PortToMethodOnMessage extends AbstractControlMatch {


    @Inject
    public PortToMethodOnMessage(ShapeHelperWrapper shapeHelperWrapper, LinkTypeRecogniser linkTypeRecogniser) {
        super(shapeHelperWrapper, linkTypeRecogniser);
    }

    @Override
    public boolean matches(ConnectedShapesComplex connectedShapesComplex) {
        Node.NodeType fromType = shapeHelperWrapper.getNodeType(connectedShapesComplex.fromShape);
        Node.NodeType toType = shapeHelperWrapper.getNodeType(connectedShapesComplex.toShape);
        return (fromType.equals(Node.NodeType.ClientPort) || fromType.equals(Node.NodeType.ServerPort))
                && (toType.equals(Node.NodeType.MethodOfProcess) || toType.equals(Node.NodeType.StartMethodOfProcess))
                && !Strings.isNullOrEmpty(connectedShapesComplex.getConnectorText())
                && super.matches(connectedShapesComplex);
    }
}
