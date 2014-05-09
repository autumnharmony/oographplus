/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.recognition;

import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.api.DiagramType;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.commons.ShapeHelperWrapperImpl;

import java.util.Collection;
import java.util.Set;

public class DiagramTypeRecognitionImpl implements DiagramTypeRecognition {

    ShapeHelperWrapper shapeHelperWrapper = new ShapeHelperWrapperImpl();

    @Override
    public DiagramType recognise(Set<XShape> shapes) {

        for (XShape shape : shapes){

            if (shapeHelperWrapper.isConnectorShape(shape)) continue;
            Node.NodeType nodeType = shapeHelperWrapper.getNodeType(shape);

            if (nodeType.equals(Node.NodeType.MethodOfProcess)){
                return DiagramType.Process;
            }
        }
        return DiagramType.Channel;
    }
}
