package ru.ssau.graphplus;

import ru.ssau.graphplus.events.ShapeEvent;
import ru.ssau.graphplus.events.ShapeEventListener;
import ru.ssau.graphplus.events.ShapeEventType;

/**
 * User: anton
 * Date: 3/16/13
 * Time: 5:23 PM
 */
public abstract class ConnectedShapesChangeListener extends ShapeEventListener {

    public ConnectedShapesChangeListener() {
        shapeEventType = ShapeEventType.ConnectedShapesChanged;
    }

    @Override
    public void onShapeEvent(ShapeEvent shapeEvent) {

        if (shapeEvent instanceof ConnectedShapesChanged){
            ConnectedShapesChanged connectedShapesChanged = (ConnectedShapesChanged) shapeEvent;

            onConnectedShapesChange(connectedShapesChanged);
        }
    }

    abstract void onConnectedShapesChange(ConnectedShapesChanged connectedShapesChanged);
}
