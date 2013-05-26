package ru.ssau.graphplus.events;

/**
 * User: anton
 * Date: 3/16/13
 * Time: 4:01 PM
 */
public abstract class ShapeModifiedListener extends ShapeEventListener {

    public ShapeModifiedListener() {
        shapeEventType = ShapeEventType.ShapeModified;
    }

    public abstract void onShapeModified(ShapeModifiedEvent shapeEvent);

    @Override
    public final void onShapeEvent(ShapeEvent shapeEvent) {
        if (shapeEvent instanceof ShapeModifiedEvent) {
            onShapeModified((ShapeModifiedEvent) shapeEvent);
        }
    }

//    public void onShapeMod


}
