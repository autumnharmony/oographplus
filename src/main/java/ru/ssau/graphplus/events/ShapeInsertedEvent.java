package ru.ssau.graphplus.events;

import com.sun.star.document.EventObject;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.ConnectedShapes;
import ru.ssau.graphplus.QI;

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
        this.xShape = QI.XShape(shape);
//        this.xShape = xShape;
        this.date = date;
    }

    public ShapeInsertedEvent(EventObject eventObject, Date date) {
//        this.xShape = xShape;

        XShape xShape1 = QI.XShape(eventObject.Source);
        if (xShape1 !=null) {
            xShape = xShape1;
        }
        this.date = date;
    }

    public ShapeInsertedEvent(XConnectorShape xConnectorShape, ConnectedShapes connectedShapes) {
        this.xShape = xConnectorShape;
        this.connectedShapes = connectedShapes;
    }

    public ShapeInsertedEvent(XShape xShape) {
        this.xShape = xShape;
    }
}
