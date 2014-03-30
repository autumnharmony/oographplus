package ru.ssau.graphplus;

import com.sun.star.drawing.XConnectorShape;
import ru.ssau.graphplus.events.ShapeEvent;
import ru.ssau.graphplus.events.ShapeEventType;

/**
 * User: anton
 * Date: 3/16/13
 * Time: 5:17 PM
 */
public class ConnectedShapesChanged extends ShapeEvent {

    private final XConnectorShape xConnectorShape;
    private final ConnectedShapes connectedShapes;

    public ConnectedShapesChanged(XConnectorShape xConnectorShape, ConnectedShapes aShapes) {
        this.xConnectorShape = xConnectorShape;
        this.connectedShapes = aShapes;
        shapeEventType = ShapeEventType.ConnectedShapesChanged;
    }

    public XConnectorShape getConnectorShape() {
        return xConnectorShape;
    }

    public ConnectedShapes getConnectedrShapes() {
        return connectedShapes;
    }
}
