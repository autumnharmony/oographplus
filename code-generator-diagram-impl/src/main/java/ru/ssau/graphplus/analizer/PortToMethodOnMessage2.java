package ru.ssau.graphplus.analizer;

import ru.ssau.graphplus.ConnectedShapesComplex;
import ru.ssau.graphplus.ShapeHelperWrapper;
import ru.ssau.graphplus.api.Node;

public class PortToMethodOnMessage2 extends AbstractMatch {

    public PortToMethodOnMessage2(ShapeHelperWrapper shapeHelperWrapper1) {
        super(shapeHelperWrapper1);
        setType1(Node.NodeType.ServerPort);
        setType2(Node.NodeType.MethodOfProcess);
    }


    @Override
    public boolean matches(ConnectedShapesComplex connected) {
        return super.matches(connected);
    }
}
