/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui;

import java.awt.*;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 4/3/14
 * Time: 12:18 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Layout {

    interface Obj {
        Dimension getDimension();
        void setPosition(Point position);
        Point getPosition();
        Rectangle getBound();
    }

    interface Stage {
        Dimension getDimenstion();
        Collection<Obj> getObjects();
        Point getPosition(Obj obj);
        Rectangle getBound(Obj obj);
    }

    void layout(Stage stage, Obj obj);



}
