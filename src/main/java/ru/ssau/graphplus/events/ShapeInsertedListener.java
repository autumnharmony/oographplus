package ru.ssau.graphplus.events;

/**
 * User: anton
 * Date: 3/16/13
 * Time: 4:01 PM
 */
public abstract class ShapeInsertedListener extends ShapeEventListener {
    public ShapeInsertedListener() {
        shapeEventType = ShapeEventType.ShapeInserted;
    }

    @Override
    public final void onShapeEvent(ShapeEvent shapeEvent) {
        if (shapeEvent instanceof ShapeInsertedEvent) {
            onShapeInserted((ShapeInsertedEvent) shapeEvent);
        }
    }

    abstract public void onShapeInserted(ShapeInsertedEvent shapeInsertedEvent);


}
