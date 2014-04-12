package ru.ssau.graphplus.analizer;

import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.ConnectedShapesComplex;
import ru.ssau.graphplus.ShapeHelperWrapper;
import ru.ssau.graphplus.api.Node;

public class PortToMethodOnMessage extends AbstractMatch {

    public PortToMethodOnMessage(ShapeHelperWrapper shapeHelperWrapper1) {
        super(shapeHelperWrapper1);
        setType1(Node.NodeType.ClientPort);
        setType2(Node.NodeType.MethodOfProcess);
    }


    @Override
    public boolean matches(ConnectedShapesComplex connected) {
        return super.matches(connected);
    }
}
