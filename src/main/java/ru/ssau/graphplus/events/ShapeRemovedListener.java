package ru.ssau.graphplus.events;

/**
 * User: anton
 * Date: 3/19/13
 * Time: 1:02 AM
 */
public abstract class ShapeRemovedListener extends ShapeEventListener {

    protected ShapeRemovedListener() {
        shapeEventType = ShapeEventType.ShapeRemoved;
    }

    @Override
    public void onShapeEvent(ShapeEvent shapeEvent) {
        if (shapeEvent instanceof ShapeRemovedEvent){
            onShapeRemoved((ShapeRemovedEvent) shapeEvent);
        }
    }

    public abstract void onShapeRemoved(ShapeRemovedEvent shapeRemovedEvent);

}
