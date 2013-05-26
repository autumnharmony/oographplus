package ru.ssau.graphplus;

import java.io.Serializable;

public interface DiagramElement extends Serializable, Refreshable {
    String getName();

    String getId();

    void setId(String id);


}
