/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.analizer.matches;

import ru.ssau.graphplus.commons.ConnectedShapesComplex;

public interface Match {
//    boolean matches(XShape xShape1, XShape xShape2);
    boolean matches(ConnectedShapesComplex connectedShapesComplex);
}
