package ru.ssau.graphplus;

/**
 * User: anton
 * Date: 3/14/13
 * Time: 2:51 AM
 */
public class AddingLink implements DiagramEventHandler {
    @Override
    public void elementAdded(ElementAddEvent event) {
        System.out.println("elementAdded");
    }

    @Override
    public void elementModified(ElementModifyEvent event) {
        System.out.println("elementModified");
    }
}
