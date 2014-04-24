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
 * Time: 11:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class AbstractMethodToMethodMatch extends AbstractControlMatch {

    protected String text;

    protected AbstractMethodToMethodMatch(ShapeHelperWrapper shapeHelperWrapper, LinkTypeRecogniser linkTypeRecogniser) {
        super(shapeHelperWrapper, linkTypeRecogniser);
    }

    @Override
    public boolean matches(ConnectedShapesComplex connectedShapesComplex) {
        return shapeHelperWrapper.getNodeType(connectedShapesComplex.fromShape).equals(Node.NodeType.MethodOfProcess)
                && shapeHelperWrapper.getNodeType(connectedShapesComplex.toShape).equals(Node.NodeType.MethodOfProcess) &&
                connectedShapesComplex.getConnectorText().equals(text)
                && super.matches(connectedShapesComplex);
    }
}
