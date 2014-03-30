/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.recognition;


import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.api.DiagramModel;
import ru.ssau.graphplus.api.DiagramType;

import java.util.Set;

public interface DiagramTypeRecognition {
    DiagramType recognise(Set<XShape> shapes);
}
