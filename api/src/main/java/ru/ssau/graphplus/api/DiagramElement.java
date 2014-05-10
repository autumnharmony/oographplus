package ru.ssau.graphplus.api;

import com.sun.star.awt.Point;
import com.sun.star.awt.Rectangle;



public interface DiagramElement  {

    /**
     * User defined name of diagram element
     * @return
     */
    String getName();
    void setName(String name);


    /**
     * Unique id for inner purposes
     * @return
     */
    String getId();


    void setId(String id);

    Rectangle getBound();

    void setPosition(Point position);

    Point getPosition();
}
