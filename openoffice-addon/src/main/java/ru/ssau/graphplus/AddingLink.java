package ru.ssau.graphplus;

import ru.ssau.graphplus.events.Event;

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

    @Override
    public void onEvent(Event event) {

        System.out.println("elementModified");
    }
}
