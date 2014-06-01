/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.commons;

import com.google.inject.Inject;
import com.sun.star.awt.Point;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.*;
import ru.ssau.graphplus.api.Node;

public class ShapeHelperWrapperImpl implements ShapeHelperWrapper {

    private MiscHelperWrapper miscHelper;

    public boolean isTextShape(XShape start_) {
        return ShapeHelper.isTextShape(start_);
    }

    public ShapeHelperWrapperImpl() {
        miscHelper = new MiscHelperWrapperImpl();
    }

    @Inject
    public ShapeHelperWrapperImpl(MiscHelperWrapper miscHelper) {
        this.miscHelper = miscHelper;
    }

    @Override
    public boolean isConnectorShape(XShape shape) {
        return ShapeHelper.isConnectorShape(shape);
    }



    void shift(Point[] p){

        Point t2 = p[p.length -1];
        for (int i = 0; i<p.length; i++){
            Point t = p[i];
            p[i] = t2;
            t2 = t;
        }
    }



    public String getText(XShape xShape) {
        return ShapeHelper.getText(xShape);
    }

    @Override
    public boolean isConnected(XConnectorShape connectorShape) {
        try {
            Object startShape = QI.XPropertySet(connectorShape).getPropertyValue("StartShape");
            Object endShape = QI.XPropertySet(connectorShape).getPropertyValue("EndShape");
            return startShape != null && endShape != null;
        } catch (UnknownPropertyException | WrappedTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Node.NodeType getNodeType(XShape shape) {
        String nodeType;

        try {
            nodeType = miscHelper.getNodeType(shape);
            return Node.NodeType.valueOf(nodeType);
        } catch (Exception ex) {
            // so sad
        }

        String shapeType = shape.getShapeType();
        if (shapeType.contains("Rectangle")) {
            //  procedure or process
            try {
                int cornerRadius = OOoUtils.getIntProperty(shape, "CornerRadius");
                if (cornerRadius != 0) {
                    // rounded
                    return Node.NodeType.MethodOfProcess;
                } else {
                    // not rounded
                    return Node.NodeType.StartMethodOfProcess;
                }
            } catch (UnknownPropertyException e) {
                throw new com.sun.star.uno.RuntimeException(e.getMessage(), e);
            } catch (WrappedTargetException e) {
                throw new com.sun.star.uno.RuntimeException(e.getMessage(), e);
            }
        }

        if (shapeType.contains("PolyPolygonShape")) {
            // client or server
            Object polyPolygon = null;
            try {
                polyPolygon = QI.XPropertySet(shape).getPropertyValue("PolyPolygon");
                Point[][] points = (Point[][]) polyPolygon;
                if (points.length > 1) {
                    throw new com.sun.star.uno.RuntimeException("Error", new com.sun.star.lang.IllegalArgumentException("Strange polygon argument, i can't get type"));
                }
                Point[] p = points[0];
                while (!normalized(p)){
                    shift(points[0]);
                }


                if (p[2].X > p[1].X && p[2].X > p[3].X && p[2].Y < p[1].Y && p[2].Y > p[3].Y) {
                    // >
                    // client
                    return Node.NodeType.ClientPort;
                }

                if ((p[2].X < p[1].X && p[2].X < p[3].X) && p[2].Y > p[1].Y && p[2].Y < p[3].Y) {
                    // <
                    // server
                    return Node.NodeType.ServerPort;
                }


            } catch (UnknownPropertyException | WrappedTargetException e) {
                throw new com.sun.star.uno.RuntimeException(e.getMessage(), e);
            }

        }
        return null;
    }

    private boolean normalized(Point[] points) {
        for (int i = 1; i < points.length; i++){
            if (points[0].X >  points[i].X || points[0].Y < points[i].Y){
                return false;
            }
        }
        return true;
    }
}
