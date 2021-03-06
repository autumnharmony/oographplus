/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.WrappedTargetException;
import ru.ssau.graphplus.api.DiagramElement;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.link.LinkBase;
import ru.ssau.graphplus.link.LinkOneConnectorBase;
import ru.ssau.graphplus.link.LinkTwoConnectorsAndTextBase;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class BetweenNodesLayout implements Layout {

    private final Stage stage;
    private final Logger logger;
    private Obj node1;
    private Obj node2;

    public BetweenNodesLayout(Stage stage, Obj node1, Obj node2) {
        this.node1 = node1;
        this.node2 = node2;
        this.stage = stage;
        logger = Logger.getLogger("BetweenNodesLayout");
    }

    @Override
    public void layout(Stage stage, Obj obj) {
        Point position1 = node1.getPosition();
        Point position2 = node2.getPosition();
        if (obj instanceof DiagramElementObj) {
            DiagramElementObj diagramElementObj = (DiagramElementObj) obj;
            DiagramElement diagramElement = diagramElementObj.getDiagramElement();
            if (diagramElement instanceof LinkTwoConnectorsAndTextBase) {
                LinkTwoConnectorsAndTextBase linkBase = (LinkTwoConnectorsAndTextBase) diagramElement;
                XShape textShape = linkBase.getTextShape();
                try {
                    com.sun.star.awt.Point point1 = (com.sun.star.awt.Point) QI.XPropertySet(linkBase.getConnShape1()).getPropertyValue("StartPosition");
                    com.sun.star.awt.Point point2 = (com.sun.star.awt.Point) QI.XPropertySet(linkBase.getConnShape2()).getPropertyValue("EndPosition");
                    position1 = new Point(point1.X, point1.Y);
                    position2 = new Point(point2.X, point2.Y);
                    Point position = new Point((position1.x + position2.x) / 2 - textShape.getSize().Width / 2, (position1.y + position2.y) / 2 - textShape.getSize().Height / 2);
                    findPlace(stage, obj, position);
                    obj.setPosition(position);
                    return;
                } catch (ClassCastException cce) {
                    return;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (diagramElement instanceof LinkOneConnectorBase) {
//                LinkOneConnectorBase linkOneConnectorBase = (LinkOneConnectorBase) diagramElement;
//                linkOneConnectorBase.getConnShape();
            }
        }
        Point position = new Point((position1.x + position2.x) / 2, (position1.y + position2.y) / 2);
        findPlace(stage, obj, position);
        obj.setPosition(position);
    }

    @Override
    public void layout(Obj obj) {
        layout(stage, obj);
    }

    private Point findPlace(Stage stage, Obj obj, Point nearPoint) {
        int x = nearPoint.x;
        int y = nearPoint.y;
        boolean placed = false;
        while (!placed) {
            obj.setPosition(new Point(x, y));
            Rectangle bound = obj.getBound();
            boolean intersects = false;
            java.util.List<Obj> intersectsWith = new ArrayList();
            for (Obj staged : stage.getObjects()) {
                if (obj.equals(staged)) continue;
                if (staged.getBound().intersects(bound)) {
                    intersects = true;
                    intersectsWith.add(staged);
                    break;
                }
            }
            if (intersects) {
                logger.info("intersects");
                if (x + obj.getDimension().width >= stage.getDimenstion().width) {
                    if (y + obj.getDimension().height >= stage.getDimenstion().height) {
                        throw new IllegalStateException("can't place object on stage");
                    }
                    x = 0;
                    y += getShift(intersectsWith, obj).height;
                    logger.info("y = " + y);
                } else {
                    x += getShift(intersectsWith, obj).width;
                    logger.info("x = " + x);
                }
            } else {
                obj.setPosition(new Point(x, y));
                placed = true;
            }
        }
        return new Point(x, y);
    }

    private Dimension getShift(List<Obj> intersectsWith, Obj o) {
        int maxX = 0;
        int maxY = 0;
        for (Obj obj : intersectsWith) {
            Rectangle bound = obj.getBound();
            int right = bound.x + bound.width;
            int bottom = bound.y + bound.height;
            if (right > maxX) {
                maxX = right;
            }
            if (bottom > maxY) {
                maxY = bottom;
            }
        }
        return new Dimension(maxX - o.getPosition().x, maxY - o.getPosition().y);
    }
}
