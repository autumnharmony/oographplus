/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.api.Node;

public interface ShapeHelperWrapper {

    boolean isTextShape(XShape start_);
    boolean isConnectorShape(XShape shape);

    Node.NodeType getNodeType(XShape shape);
}
