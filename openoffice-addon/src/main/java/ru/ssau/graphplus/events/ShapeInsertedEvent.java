package ru.ssau.graphplus.events;

import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.commons.ConnectedShapes;
import ru.ssau.graphplus.commons.QI;

import java.util.Date;

/**
 * User: anton
 * Date: 3/14/13
 * Time: 1:57 AM
 */
public class ShapeInsertedEvent extends ShapeEvent {

    public XShape xShape;
    Date date;
    private ConnectedShapes connectedShapes;
    //private XConnectorShape xConnectorShape;

    public ShapeInsertedEvent(Object shape, Date date) {
        this(QI.XShape(shape));
        this.date = date;
    }

    public ShapeInsertedEvent(XConnectorShape xConnectorShape, ConnectedShapes connectedShapes) {
        this(xConnectorShape);
        this.connectedShapes = connectedShapes;
    }

    public ShapeInsertedEvent(XShape xShape) {
        this();
        this.xShape = xShape;
    }

    private ShapeInsertedEvent(){
        shapeEventType = ShapeEventType.ShapeInserted;
    }
}
