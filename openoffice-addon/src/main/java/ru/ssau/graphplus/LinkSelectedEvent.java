package ru.ssau.graphplus;

import ru.ssau.graphplus.api.Link;

/**
 * Created by 1 on 04.06.14.
 */
public class LinkSelectedEvent extends DiagramElementSelected {
    public LinkSelectedEvent(Link diagramElement) {
        super(diagramElement);
    }
}
