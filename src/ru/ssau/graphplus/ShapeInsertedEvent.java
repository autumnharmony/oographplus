package ru.ssau.graphplus;

import com.sun.star.document.EventObject;
import com.sun.star.drawing.XShape;

import java.util.Date;

/**
 * User: anton
 * Date: 3/14/13
 * Time: 1:57 AM
 */
public class ShapeInsertedEvent {

    XShape xShape;
    Date date;

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
}
