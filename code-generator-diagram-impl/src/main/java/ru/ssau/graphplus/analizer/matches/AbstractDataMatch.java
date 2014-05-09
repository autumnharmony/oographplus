/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.analizer.matches;

import com.google.inject.Inject;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.recognition.LinkTypeRecogniser;

public class AbstractDataMatch extends MatchBase {

    @Inject
    protected AbstractDataMatch(ShapeHelperWrapper shapeHelperWrapper, LinkTypeRecogniser linkTypeRecogniser) {
        super(shapeHelperWrapper, linkTypeRecogniser);
    }

    @Override
    public boolean matches(ConnectedShapesComplex connectedShapesComplex) {
        return super.matches(connectedShapesComplex) && getLinkType(connectedShapesComplex).equals(Link.LinkType.DataFlow);
    }
}
