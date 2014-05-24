/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.commons;

import com.google.common.collect.Sets;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.WrappedTargetException;
import ru.ssau.graphplus.ShapesProvider;

import java.util.HashSet;

/**
 * Object for storing two shapes connected by link three shapes (which consists of three shapes  1. ConnectorShape, 2. TextShape, 3. ConnectorShape
 */
public class ConnectedShapesComplex implements ShapesProvider {

    public XShape fromShape;
    public XShape toShape;

    // connector 1 for case of two connectors
    public XConnectorShape connector1;

    // connector 2 for case of two connectors
    public XConnectorShape connector2;
    public XShape textShape;

    // connector for case of one connector
    public XConnectorShape connector;

    public ConnectedShapesComplex(XShape fromShape, XShape toShape, XConnectorShape connector1, XConnectorShape connector2, XShape textShape) {
        this.fromShape = fromShape;
        this.toShape = toShape;
        this.connector1 = connector1;
        this.connector2 = connector2;
        this.textShape = textShape;
    }


    public ConnectedShapesComplex(XShape fromShape, XShape toShape, XConnectorShape connector) {
        this.fromShape = fromShape;
        this.toShape = toShape;
        this.connector = connector;
    }

    public String getConnectorText() {
        try {
            return textShape != null ? QI.XText(textShape).getString() : QI.XText(connector).getString();
        } catch (Exception e) {
            return null;
        }
    }


    @Override
    public Iterable<XShape> getShapes() {
        HashSet<XShape> xShapes = Sets.newHashSet(fromShape, toShape);
        if (connector != null) {
            xShapes.add(connector);
        }

        if (connector1 != null) {
            xShapes.add(connector1);
        }

        if (connector2 != null) {
            xShapes.add(connector2);
        }

        if (textShape != null) {
            xShapes.add(textShape);
        }

        xShapes.add(fromShape);
        xShapes.add(toShape);


        return xShapes;
    }

    @Override
    public String toString() {
        return "ConnectedShapesComplex{" +
                "\nfromShape=" + QI.XText(fromShape).getString() +
                "\ntoShape=" + QI.XText(toShape).getString() +
                "\nconnector=" + getConnectorText() +
                '}';
    }

    public void normalize() {
        try {
            if (connector == null) {
                if (QI.XPropertySet(connector1).getPropertyValue("LineEndName").equals("Arrow")) {
                    invert1();
                }
            } else {

            }
        } catch (UnknownPropertyException | WrappedTargetException e) {

        }
    }

    private void invert1() {
        XShape tShape = fromShape;
        fromShape = toShape;
        toShape = tShape;

        XConnectorShape tConnector = connector1;
        connector1 = connector2;
        connector2 = tConnector;
    }

//
//    private void invert2() {
//        XShape tShape = fromShape;
//        fromShape = toShape;
//        toShape = tShape;
//
//        XConnectorShape tConnector = connector1;
//        connector1 = connector2;
//        connector2 = tConnector;
//    }
}
