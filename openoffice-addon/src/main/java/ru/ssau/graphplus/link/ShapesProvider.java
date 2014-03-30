/*
 * Copyright (c) 2013. Anton Borisov
 */

package ru.ssau.graphplus.link;

import com.sun.star.drawing.XShape;

/**
 * User: anton
 * Date: 6/13/13
 * Time: 12:21 AM
 */
public interface ShapesProvider {
    Iterable<XShape> getShapes();
}
