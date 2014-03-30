package ru.ssau.graphplus;

import ru.ssau.graphplus.events.EventListener;

/**
 * User: anton
 * Date: 3/14/13
 * Time: 2:45 AM
 */
public interface DiagramEventHandler extends EventListener {
    public void elementAdded(ElementAddEvent event);
    public void elementModified(ElementModifyEvent event);

}
