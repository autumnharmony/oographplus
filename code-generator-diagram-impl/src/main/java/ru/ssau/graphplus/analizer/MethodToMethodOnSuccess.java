package ru.ssau.graphplus.analizer;

import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.ConnectedShapesComplex;
import ru.ssau.graphplus.ShapeHelperWrapper;
import ru.ssau.graphplus.api.Node;

/**
 * Created by anton on 30.03.14.
 */
public class MethodToMethodOnSuccess extends AbstractMatch {


    public MethodToMethodOnSuccess(ShapeHelperWrapper shapeHelperWrapper1) {
        super(shapeHelperWrapper1);
        setType1(Node.NodeType.MethodOfProcess);
        setType2(Node.NodeType.MethodOfProcess);
    }

    @Override
    public boolean matches(ConnectedShapesComplex connected) {
        return super.matches(connected) && connected.getLinkText().equals("+");
    }
}
