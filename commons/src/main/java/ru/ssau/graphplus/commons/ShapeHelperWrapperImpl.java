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
import org.apache.commons.lang.ArrayUtils;
import ru.ssau.graphplus.api.Node;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    void shift(Point[] p) {
        Point t2 = p[p.length - 1];
        for (int i = 0; i < p.length; i++) {
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
        // by property on shape
        try {
            nodeType = miscHelper.getNodeType(shape);
            if (nodeType != null) {
                return Node.NodeType.valueOf(nodeType);
            }
        } catch (Exception ex) {
            // so sad
        }
        // manually
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
                p = removeExtra(p);
                int shiftCount = 0;
                boolean normalized = false;
                do {
                    normalized = normalized(p);
                    if (!normalized) {
                        shift(points[0]);
                        shiftCount++;
                    }
                    else {
                        break;
                    }
                }
                while (!normalized && shiftCount < p.length * 2);

                if (!normalized){
                    ArrayUtils.reverse(p);
                    shiftCount = 0;
                    do {
                        normalized = normalized(p);
                        if (!normalized) {
                            shift(points[0]);
                            shiftCount++;
                        }
                        else {
                            break;
                        }
                    }
                    while (!normalized && shiftCount < p.length * 2);
                    if (!normalized){
                        return null;
                    }
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
    public Point[] removeExtra(Point[] p) {
        if (p.length < 3) return p;
        List<Integer> toRemove = new ArrayList<>();
        boolean notAll = true;
        int i = 0;
        int ii = 0;
        int iii = 0;
        for (i = 0; i <= p.length - 3; i++) {
//            if (i == p.length - 1) {
//                if (p[i].equals(p[0])) {
//                    ii = 1;
//                    iii = 2;
//                }
//                else {
//                    break;
//                }
//            } else {
            ii = i + 1;
            iii = ii + 1;
//            }
            java.awt.Point p1 = PointUtils.convert(p[i]);
            java.awt.Point p2 = PointUtils.convert(p[ii]);
            java.awt.Point p3 = PointUtils.convert(p[iii]);
            boolean pointLiesOnLine = GeomUtils.pointLiesOnLine(p2, new Line2D.Double(p1, p3));
            if (pointLiesOnLine) {
                toRemove.add(ii);
            }
        }
        Point[] out = new Point[p.length - toRemove.size()];
        int j = 0;
        for (i = 0; i < p.length; i++) {
            if (!toRemove.contains(i))
                out[j++] = new Point(p[i].X, p[i].Y);
        }
        return out;
    }

    private boolean normalized(Point[] points) {
        for (int i = 1; i < points.length; i++) {
            if (points[0].X > points[i].X || points[0].Y > points[i].Y) {
                return false;
            }
        }
        return true;
    }
}
