package ru.ssau.graphplus.analizer;

import com.google.inject.Inject;
import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.ConnectedShapesComplex;
import ru.ssau.graphplus.ShapeHelperWrapper;
import ru.ssau.graphplus.api.Node;


public class MethodToMethodOnFail extends AbstractMatch {

    @Inject
    public MethodToMethodOnFail(ShapeHelperWrapper shapeHelperWrapper1) {
        super(shapeHelperWrapper1);
    }

    @Override
    public boolean matches(ConnectedShapesComplex connected) {
        return super.matches(connected) && connected.getLinkText().equals("-");
    }
}
