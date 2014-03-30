/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.analizer;

import com.google.inject.Inject;
import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.ShapeHelperWrapper;
import ru.ssau.graphplus.api.Node;

public class MethodToMethodMatch implements Match {

    private final ShapeHelperWrapper shapeHelperWrapper;

    @Inject
    public MethodToMethodMatch(ShapeHelperWrapper shapeHelperWrapper1){
        this.shapeHelperWrapper = shapeHelperWrapper1;
    }

    @Override
    public boolean matches(XShape xShape1, XShape xShape2) {
        return shapeHelperWrapper.getNodeType(xShape1).equals(Node.NodeType.MethodOfProcess) && shapeHelperWrapper.getNodeType(xShape2).equals(Node.NodeType.MethodOfProcess);
    }
}
