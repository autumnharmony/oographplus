/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.analizer.matches;

import com.google.inject.Inject;
import ru.ssau.graphplus.analizer.Match;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.recognition.LinkTypeRecogniser;


public abstract class MatchBase implements Match {
    protected final ShapeHelperWrapper shapeHelperWrapper;
    protected final LinkTypeRecogniser linkTypeRecogniser;

    @Inject
    protected MatchBase(ShapeHelperWrapper shapeHelperWrapper, LinkTypeRecogniser linkTypeRecogniser) {
        this.shapeHelperWrapper = shapeHelperWrapper;
        this.linkTypeRecogniser = linkTypeRecogniser;
    }

    protected Link.LinkType getLinkType(ConnectedShapesComplex shapesComplex){
       return linkTypeRecogniser.getType(shapesComplex.connector1, shapesComplex.textShape, shapesComplex.connector2);
    }
}
