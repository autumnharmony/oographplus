/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.commons;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.ShapesProvider;
import ru.ssau.graphplus.api.Link;

import java.util.HashSet;

/**
 * Data object for storing two shapes connected by link three shapes (which consists of three shapes  1. ConnectorShape, 2. TextShape, 3. ConnectorShape
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

    public ConnectedShapesComplex(XShape fromShape, XShape toShape, XShape textShape) {
        this.fromShape = fromShape;
        this.toShape = toShape;
        this.textShape = textShape;
    }


    public ConnectedShapesComplex(XShape fromShape, XShape toShape, XConnectorShape connector) {
        this.fromShape = fromShape;
        this.toShape = toShape;
        this.connector = connector;
    }

    public ConnectedShapesComplex(XShape fromShape, XShape toShape) {
        this.fromShape = fromShape;
        this.toShape = toShape;
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
}
