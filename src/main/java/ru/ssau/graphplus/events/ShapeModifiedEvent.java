package ru.ssau.graphplus.events;

/**
 * User: anton
 * Date: 3/14/13
 * Time: 2:01 AM
 */
public class ShapeModifiedEvent extends ShapeEvent {

    public ShapeModifiedEvent() {
       shapeEventType = ShapeEventType.ShapeModified;
    }
}
