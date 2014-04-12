package ru.ssau.graphplus.analizer;

import com.google.inject.Inject;
import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.ConnectedShapesComplex;
import ru.ssau.graphplus.ShapeHelperWrapper;
import ru.ssau.graphplus.api.Node;

/**
 * Created by anton on 30.03.14.
 */
public class AbstractMatch implements Match {


    protected final ShapeHelperWrapper shapeHelperWrapper;
    private Node.NodeType type1;
    private Node.NodeType type2;


    @Inject
    public AbstractMatch (ShapeHelperWrapper shapeHelperWrapper1){
        this.shapeHelperWrapper = shapeHelperWrapper1;
    }

    public void setType1(Node.NodeType type1) {
        this.type1 = type1;
    }

    public void setType2(Node.NodeType type2) {
        this.type2 = type2;
    }


    @Override
    public boolean matches(ConnectedShapesComplex connected) {
        Node.NodeType nodeType1 = shapeHelperWrapper.getNodeType(connected.getFromShape());
        Node.NodeType nodeType2 = shapeHelperWrapper.getNodeType(connected.getToShape());
        return nodeType1.equals(type1) && nodeType2.equals(type2);
    }
}
