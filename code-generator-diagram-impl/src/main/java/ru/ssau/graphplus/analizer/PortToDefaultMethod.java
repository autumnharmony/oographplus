package ru.ssau.graphplus.analizer;

import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.ConnectedShapesComplex;
import ru.ssau.graphplus.ShapeHelperWrapper;

/**
 * Created by anton on 30.03.14.
 */
public class PortToDefaultMethod extends AbstractMatch {
    public PortToDefaultMethod(ShapeHelperWrapper shapeHelperWrapper1) {
        super(shapeHelperWrapper1);
    }

    @Override
    public boolean matches(ConnectedShapesComplex connected) {
        return super.matches(connected);
    }
}
