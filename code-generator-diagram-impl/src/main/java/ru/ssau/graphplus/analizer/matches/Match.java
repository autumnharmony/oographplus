/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.analizer.matches;

import ru.ssau.graphplus.commons.ConnectedShapesComplex;

public interface Match {
    boolean matches(ConnectedShapesComplex connectedShapesComplex);
}
