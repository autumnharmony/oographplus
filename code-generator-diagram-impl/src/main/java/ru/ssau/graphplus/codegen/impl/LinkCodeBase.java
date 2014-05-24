/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl;

import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.codegen.LinkCode;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.codegen.impl.recognition.LinkTypeRecogniser;


public abstract class LinkCodeBase extends CodeBase implements LinkCode {

    protected final LinkTypeRecogniser linkTypeRecogniser;
    protected final ShapeHelperWrapper shapeHelper;
    protected ConnectedShapesComplex connectedShapesComplex;

    protected Node.NodeType fromNodeType() {
        return type(from());
    }

    protected Node.NodeType type(XShape shape) {
        return shapeHelper.getNodeType(shape);
    }

    protected Node.NodeType toNodeType() {
        return shapeHelper.getNodeType(connectedShapesComplex.toShape);
    }

    protected XShape from() {
        return connectedShapesComplex.fromShape;
    }

    protected XShape to() {
        return connectedShapesComplex.toShape;
    }

    protected XShape textShape() {
        return connectedShapesComplex.textShape;
    }

    protected String linkText() {
        if (textShape() == null) {
            return text(connectedShapesComplex.connector);
        } else {
            return text(textShape());
        }
    }


    protected String text(XShape shape) {
        return shapeHelper.getText(shape);
    }


    protected char getPortChar(XShape shape) {

        Node.NodeType fromType = shapeHelper.getNodeType(shape);
        char c;
        switch (fromType) {
            case ClientPort:
                c = '!';
                break;
            case ServerPort:
                c = '?';
                break;
            default:
                c = ' ';
        }
        return c;
    }



    protected String getPortType(XShape shape) {
        String string = QI.XText(shape).getString();
        String[] split = string.split(":");
        if (split.length < 2) {
            throw new IllegalArgumentException();
        }
        return split[1];
    }

    public void setConnectedShapesComplex(ConnectedShapesComplex connectedShapesComplex) {
        this.connectedShapesComplex = connectedShapesComplex;
    }

    public LinkCodeBase(ConnectedShapesComplex connectedShapesComplex, LinkTypeRecogniser linkTypeRecogniser, ShapeHelperWrapper shapeHelper) {
        this.connectedShapesComplex = connectedShapesComplex;
        this.linkTypeRecogniser = linkTypeRecogniser;
        this.shapeHelper = shapeHelper;
    }

    @Override
    public String toString() {
        return "LinkCodeBase{" +
                "connectedShapesComplex=" + connectedShapesComplex +
                '}';
    }
}
