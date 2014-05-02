/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.commons;

import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.api.Node;

public interface ShapeHelperWrapper {

    boolean isTextShape(XShape start_);
    boolean isConnectorShape(XShape shape);

    Node.NodeType getNodeType(XShape shape);

    String getText(XShape xShape);

    boolean isConnected(XConnectorShape connectorShape);
}
