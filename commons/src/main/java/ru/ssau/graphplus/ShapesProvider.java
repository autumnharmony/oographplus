/*
 * Copyright (c) 2013. Anton Borisov
 */

package ru.ssau.graphplus;

import com.sun.star.drawing.XShape;

public interface ShapesProvider {
    Iterable<XShape> getShapes();
}
