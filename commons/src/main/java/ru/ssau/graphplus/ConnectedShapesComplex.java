/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import com.google.inject.Inject;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.*;

import java.lang.IllegalArgumentException;

/**
 * Data object for storing two shapes connected by link three shapes (which consists of three shapes  1. ConnectorShape, 2. TextShape, 3. ConnectorShape
 */
public class ConnectedShapesComplex {

    private XShape fromShape;
    private XShape toShape;

    // connector 1 for case of two connectors
    private XConnectorShape connector1;

    // connector 2 for case of two connectors
    private XConnectorShape connector2;
    private XShape textShape;

    // connector for case of one connector
    private XConnectorShape connector;

    @Inject
    private static ShapeHelperWrapper shapeHelperWrapper;

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

    public ConnectedShapesComplex(XShape fromShape, XShape toShape) {
        this.fromShape = fromShape;
        this.toShape = toShape;
    }

    public String getLinkText(){
        try {
            return textShape != null ? shapeHelperWrapper.getText(textShape) : (String) QI.XPropertySet(connector).getPropertyValue("Text");
        } catch (UnknownPropertyException e) {

           throw new IllegalArgumentException(e);
        } catch (WrappedTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public XShape getFromShape() {
        return fromShape;
    }

    public void setFromShape(XShape fromShape) {
        this.fromShape = fromShape;
    }

    public XShape getToShape() {
        return toShape;
    }

    public void setToShape(XShape toShape) {
        this.toShape = toShape;
    }
}
