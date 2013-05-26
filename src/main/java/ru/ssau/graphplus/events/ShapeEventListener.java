package ru.ssau.graphplus.events;

/**
 * User: anton
 * Date: 3/16/13
 * Time: 4:52 PM
 */
public abstract class ShapeEventListener {

    protected ShapeEventType shapeEventType;

    public ShapeEventType getEventType() {
        return shapeEventType;
    }

    abstract public void onShapeEvent(ShapeEvent shapeEvent);
}
