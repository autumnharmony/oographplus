package ru.ssau.graphplus;

import com.sun.star.lang.EventObject;

/**
* User: anton
* Date: 5/18/13
* Time: 6:10 PM
*/
public interface InputMode {
    void onInput(EventObject eventObject);

    void setDiagramController(DiagramController diagramController);
}
