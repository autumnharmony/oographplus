/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.analizer.matches;

import com.google.inject.Inject;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.recognition.LinkTypeRecogniser;

public class AbstractMethodToMethodMatch extends AbstractControlMatch {

    protected String text;

    @Inject
    public AbstractMethodToMethodMatch(ShapeHelperWrapper shapeHelperWrapper, LinkTypeRecogniser linkTypeRecogniser) {
        super(shapeHelperWrapper, linkTypeRecogniser);
    }

    @Override
    public boolean matches(ConnectedShapesComplex connectedShapesComplex) {
        Node.NodeType fromType = shapeHelperWrapper.getNodeType(connectedShapesComplex.fromShape);
        Node.NodeType toType = shapeHelperWrapper.getNodeType(connectedShapesComplex.toShape);
        return (fromType.equals(Node.NodeType.MethodOfProcess) || fromType.equals(Node.NodeType.StartMethodOfProcess))
                && (toType.equals(Node.NodeType.MethodOfProcess) || toType.equals(Node.NodeType.StartMethodOfProcess)) &&
                connectedShapesComplex.getConnectorText().equals(text)
                && super.matches(connectedShapesComplex);
    }
}
