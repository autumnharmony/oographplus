
package ru.ssau.graphplus;

import com.sun.star.frame.XFrame;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * This class which objects are containers for diagram model,diagram controller, dispatch objects per frame
 */
public class FrameObject {

    private final XFrame xFrame;
    private final DiagramController diagramController;
    private final DiagramModel diagramModel;

    private final MyDispatch dispatch;

    public FrameObject(XFrame xFrame, DiagramController diagramController, DiagramModel diagramModel, MyDispatch myDispatch) {
        this.xFrame = xFrame;
        this.diagramController = diagramController;
        this.diagramModel = diagramModel;
        this.dispatch = myDispatch;
        frameObjects.put(xFrame, this);
    }

    private static Map<XFrame, FrameObject> frameObjects = new WeakHashMap<>();

    public static Map<XFrame,FrameObject> getFrameObjects(){
        return Collections.unmodifiableMap(frameObjects);
    }

    public XFrame getFrame() {
        return xFrame;
    }

    public DiagramController getController() {
        return diagramController;
    }

    public MyDispatch getDispatch() {
        return dispatch;
    }
}
