package ru.ssau.graphplus.events;

import com.sun.star.drawing.XShape;

/**
 * User: anton
 * Date: 3/14/13
 * Time: 2:01 AM
 */
public class ShapeModifiedEvent extends ShapeEvent {

    public ShapeModifiedEvent() {
        shapeEventType = ShapeEventType.ShapeModified;
    }

    XShape xShape;

    public ShapeModifiedEvent(XShape xShape) {
        this();
        this.xShape = xShape;
    }

    public XShape getShape() {
        return xShape;
    }
}
