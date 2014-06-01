
package ru.ssau.graphplus.link;

import com.sun.star.awt.Point;
import com.sun.star.awt.Size;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexContainer;
import com.sun.star.drawing.*;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.UnoRuntime;
import ru.ssau.graphplus.DrawHelper;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.commons.GeomUtils;
import ru.ssau.graphplus.commons.PointUtils;
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.commons.ShapeHelper;
import ru.ssau.graphplus.node.NodeBase;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.ssau.graphplus.commons.PointUtils.distance;

/**
 * @author anton
 */
public class LinkAdjuster {

    public static void adjustLink(LinkBase linkBase, NodeBase from, NodeBase to) {
        if (linkBase instanceof LinkOneConnectorBase) {
            return;
        } else if (linkBase instanceof LinkTwoConnectorsAndTextBase) {
            LinkTwoConnectorsAndTextBase link = (LinkTwoConnectorsAndTextBase) linkBase;
            XGluePointsSupplier xGluePointsSupplier = UnoRuntime.queryInterface(XGluePointsSupplier.class, link.getTextShape());
            XIndexContainer gluePoints = xGluePointsSupplier.getGluePoints();
            if (gluePoints == null) {
                return;
            }
            int count = gluePoints.getCount();
            int i = 0;
            int j = 0;
            while (j < count) {
                j++;
                try {
                    gluePoints.removeByIndex(i);
                } catch (com.sun.star.lang.IndexOutOfBoundsException | WrappedTargetException e) {
                    i++;
                }
            }
//            NodeBase from = linkBase.getFrom();
//            NodeBase to = linkBase.getTo();
            Point2D p1 = center(from);
            Point2D p2 = center(to);
            XShape textShape = link.getTextShape();
            Size size = textShape.getSize();
            Point middleFromTo = PointUtils.interpolate(from.getPosition(), to.getPosition(), 0.5);
            Rectangle2D.Double rectangle = new Rectangle2D.Double(middleFromTo.X - size.Width / 2, middleFromTo.Y - size.Height / 2, size.Width, size.Height);
            Line2D line2D = new Line2D.Double(p1, p2);
            Point2D.Double middle;
            double f = 0.1;
            boolean contains;
            do {
                middle = (Point2D.Double) PointUtils.interpolate(p1, p2, f);
                contains = rectangle.contains(middle);
                f += 0.1;
            } while (!contains && f < 1);
            if (!contains) throw new RuntimeException();
            Point2D.Double target1 = new Point2D.Double();
            Point2D.Double target2 = new Point2D.Double();
            boolean rectangleLineIntersection = GeomUtils.findRectangleLineIntersection(rectangle, middle, (Point2D.Double) p1, target1);
            boolean rectangleLineIntersection1 = GeomUtils.findRectangleLineIntersection(rectangle, middle, (Point2D.Double) p2, target2);
            try {
                int x1;
                int y1;
                int x2;
                int y2;

                int x1g;
                int y1g;
                int x2g;
                int y2g;

                x1 = (int) target1.x - middleFromTo.X;
                x1g = (int) target1.x;
                y1 = (int) target1.y - middleFromTo.Y;
                y1g = (int) target1.y;
                int gpi1 = gluePoints.getCount();
                gluePoints.insertByIndex(gpi1, new GluePoint2(new Point(x1, y1), Boolean.FALSE, Alignment.CENTER, EscapeDirection.SMART, Boolean.TRUE));
                x2 = (int) target2.x - middleFromTo.X;
                x2g = (int) target2.x;
                y2 = (int) target2.y - middleFromTo.Y;
                y2g = (int) target2.y;
                int gpi2 = gluePoints.getCount();
                gluePoints.insertByIndex(gpi2, new GluePoint2(new Point(x2, y2), Boolean.FALSE, Alignment.CENTER, EscapeDirection.SMART, Boolean.TRUE));
                try {
                    Point point1 = new Point(x1g, y1g);
                    Point point2 = new Point(x2g, y2g);
                    double fromP1 = distance(from.getPosition(), point1);
                    double toPoint1 = distance(to.getPosition(), point1);
                    double fromP2 = distance(from.getPosition(), point2);
                    double toPoint2 = distance(to.getPosition(), point2);
                    if (fromP1 < toPoint1 && fromP2 > toPoint2) {
                        QI.XPropertySet(((LinkTwoConnectorsAndTextBase) linkBase).connShape1).setPropertyValue("EndGluePointIndex", new Integer(gpi1));
                        QI.XPropertySet(((LinkTwoConnectorsAndTextBase) linkBase).connShape2).setPropertyValue("StartGluePointIndex", new Integer(gpi2));
                    } else {
                        if (fromP2 < toPoint2 && fromP1 > toPoint1){
                            QI.XPropertySet(((LinkTwoConnectorsAndTextBase) linkBase).connShape1).setPropertyValue("EndGluePointIndex", new Integer(gpi1));
                            QI.XPropertySet(((LinkTwoConnectorsAndTextBase) linkBase).connShape2).setPropertyValue("StartGluePointIndex", new Integer(gpi2));
                        }

                       else {
                            System.out.println("OMG");
                        }
                    }
                    XShape textShape1 = ((LinkTwoConnectorsAndTextBase) linkBase).textShape;
                    Point position1 = textShape1.getPosition();
                    textShape1.setPosition(new Point(position1.X - 10, position1.Y - 10));
                    textShape1.setPosition(position1);
//                    QI.XPropertySet(link.connShape1).setPropertyValue("StartShape", link.getFrom().getShape());
//                    QI.XPropertySet(link.connShape1).setPropertyValue("EndShape", link.textShape);
//
//                    QI.XPropertySet(link.connShape2).setPropertyValue("StartShape", link.textShape);
//                    QI.XPropertySet(link.connShape2).setPropertyValue("EndShape", link.getTo().getShape());
                } catch (UnknownPropertyException | PropertyVetoException e) {
                    e.printStackTrace();
                }
            } catch (com.sun.star.lang.IndexOutOfBoundsException | WrappedTargetException | com.sun.star.lang.IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    private static Point2D center(Node node) {
        Point position = node.getPosition();
        return new Point2D.Double(position.X, position.Y);
    }

    public static void placeText(LinkTwoConnectorsAndTextBase link) {
        Point pos1 = link.getConnShape1().getPosition();
        Point pos2 = link.getConnShape2().getPosition();
        Point resultPosition = new Point((pos1.X + pos2.X) / 2, (pos1.Y + pos2.Y) / 2);
        link.getTextShape().setPosition(resultPosition);
    }
}
