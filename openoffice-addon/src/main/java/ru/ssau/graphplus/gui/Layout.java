/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui;

import java.awt.*;
import java.util.Collection;

/**
 * Layouting <code>Obj</code> on <code>Stage</code>
 * It only affects position of Obj
 */
public interface Layout {

    /**
     * Object, which should be layouted
     */
    interface Obj {
        Dimension getDimension();
        void setPosition(Point position);
        Point getPosition();
        Rectangle getBound();
    }

    /**
     * Stage for positioning objects on
     */
    interface Stage {
        Dimension getDimenstion();
        Collection<Obj> getObjects();
        Point getPosition(Obj obj);
        Rectangle getBound(Obj obj);
    }

    /**
     * Change position of obj accroding to positions of other Objs already added on stage
     * @param stage
     * @param obj
     */
    void layout(Stage stage, Obj obj);

    /**
     * Change position of obj accroding to positions of other Objs already added on stage
     * @param obj
     */
    void layout(Obj obj);


}
