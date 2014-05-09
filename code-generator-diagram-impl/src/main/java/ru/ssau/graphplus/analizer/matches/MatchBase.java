/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.analizer.matches;

import com.google.inject.Inject;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.recognition.LinkTypeRecogniser;


public abstract class MatchBase implements Match {
    protected final ShapeHelperWrapper shapeHelperWrapper;
    protected final LinkTypeRecogniser linkTypeRecogniser;
    private ConnectedShapesComplex lastCalledMatchArgumentconnectedShapesComplex;

    @Inject
    protected MatchBase(ShapeHelperWrapper shapeHelperWrapper, LinkTypeRecogniser linkTypeRecogniser) {
        this.shapeHelperWrapper = shapeHelperWrapper;
        this.linkTypeRecogniser = linkTypeRecogniser;
    }

    protected Link.LinkType getLinkType(ConnectedShapesComplex shapesComplex) {
        if (shapesComplex.connector == null) {
            return linkTypeRecogniser.getType(shapesComplex.connector1, shapesComplex.textShape, shapesComplex.connector2);
        }
        else {
            return linkTypeRecogniser.getType(shapesComplex.connector, null, null);
        }
    }

    @Override
    public boolean matches(ConnectedShapesComplex connectedShapesComplex) {
        this.lastCalledMatchArgumentconnectedShapesComplex = connectedShapesComplex;
        printDescription();
        return true;
    }

    public String describe(){

        return getClass().toString()+ " " + getLinkType(lastCalledMatchArgumentconnectedShapesComplex).toString();
    }

    private void printDescription(){
        System.out.println(describe());
    }
}
