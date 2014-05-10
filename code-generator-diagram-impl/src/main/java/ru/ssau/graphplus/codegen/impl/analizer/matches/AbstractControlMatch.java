/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl.analizer.matches;

import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.codegen.impl.recognition.LinkTypeRecogniser;


public class AbstractControlMatch extends MatchBase {
    protected AbstractControlMatch(ShapeHelperWrapper shapeHelperWrapper, LinkTypeRecogniser linkTypeRecogniser) {
        super(shapeHelperWrapper, linkTypeRecogniser);
    }

    @Override
    public boolean matches(ConnectedShapesComplex connectedShapesComplex) {
        return super.matches(connectedShapesComplex) && getLinkType(connectedShapesComplex).equals(Link.LinkType.ControlFlow);
    }
}
