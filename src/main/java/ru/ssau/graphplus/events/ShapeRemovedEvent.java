package ru.ssau.graphplus.events;

import com.sun.star.drawing.XShape;

import java.util.List;

/**
 * User: anton
 * Date: 3/19/13
 * Time: 12:57 AM
 */
public class ShapeRemovedEvent extends ShapeEvent {


    private final XShape xShape;

    boolean many;
    private List<Object> shapes;


    public List<Object> getShapes() {
        return shapes;
    }

    public boolean isMany() {
        return many;
    }

    public ShapeRemovedEvent(XShape xShape) {
        this.xShape = xShape;
        shapeEventType = ShapeEventType.ShapeRemoved;
    }

    public XShape getShape() {
        return xShape;
    }
}
