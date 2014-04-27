/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import com.google.inject.Inject;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.recognition.LinkTypeRecogniser;

public class PortToMethodOnMessageCode extends LinkCodeBase {


    public PortToMethodOnMessageCode(ConnectedShapesComplex connectedShapesComplex, LinkTypeRecogniser linkTypeRecogniser, ShapeHelperWrapper shapeHelper) {
        super(connectedShapesComplex, linkTypeRecogniser, shapeHelper);
    }

    @Override
    public String getCode() {
        //+method() -> method1;
//        Node.NodeType fromType = shapeHelper.getNodeType(connectedShapesComplex.fromShape);
        String port = shapeHelper.getText(connectedShapesComplex.fromShape);
        String method = shapeHelper.getText(connectedShapesComplex.toShape);
        String message = connectedShapesComplex.getConnectorText();


        return port + getPortChar(from()) + message + " -> "+method+";";
    }


}
