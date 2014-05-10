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

    private Point[] sort(Point[][] points) {
        Point[] point = points[0];
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        Point leftDown;

        Point[] newPoints = new Point[point.length];

        int leftDownIndex = -1;

        for (int i = 0; i < point.length; i++) {
            Point _p = point[i];
            if (_p.X < minX && _p.Y < minY) {
                minX = _p.X;
                minY = _p.Y;
                leftDown = _p;
                leftDownIndex = i;
            }
        }

        assert leftDownIndex != -1;

        int k = 0;
        for (int j = leftDownIndex; j < point.length; j++) {
            newPoints[k++] = point[j];
        }

        for (int j = 0; j < leftDownIndex; j++) {
            newPoints[k++] = point[j];
        }

        return newPoints;


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

//        try {
//            nodeType = miscHelper.getNodeType(shape);
//            return Node.NodeType.valueOf(nodeType);
//        } catch (Exception ex) {
//            // so sad
//        }

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
                Point[] sort = sort(points);

                if (sort[3].X > sort[2].X && sort[3].X > sort[4].X) {
                    // >
                    // client
                    return Node.NodeType.ClientPort;
                }

                if ((sort[3].X < sort[2].X && sort[3].X < sort[4].X) || (sort[3].X < sort[2].X && sort[3].X < sort[1].X)) {
                    // <
                    // server
                    return Node.NodeType.ServerPort;
                }


            } catch (UnknownPropertyException e) {
                throw new com.sun.star.uno.RuntimeException(e.getMessage(), e);
            } catch (WrappedTargetException e) {
                throw new com.sun.star.uno.RuntimeException(e.getMessage(), e);
            }

        }
        return null;
    }
}
