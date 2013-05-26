package ru.ssau.graphplus;

/**
 * User: anton
 * Date: 3/14/13
 * Time: 2:45 AM
 */
public interface DiagramEventHandler {
    public void elementAdded(ElementAddEvent event);

    public void elementModified(ElementModifyEvent event);
}
