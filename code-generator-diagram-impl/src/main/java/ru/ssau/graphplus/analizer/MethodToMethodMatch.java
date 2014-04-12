/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.analizer;

import com.google.inject.Inject;
import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.ShapeHelperWrapper;
import ru.ssau.graphplus.api.Node;

public class MethodToMethodMatch extends AbstractMatch {

    @Inject
    public MethodToMethodMatch(ShapeHelperWrapper shapeHelperWrapper1){
        super(shapeHelperWrapper1);
    }


}
