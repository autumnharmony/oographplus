/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import com.google.inject.Inject;
import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.codegen.LinkCode;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.recognition.LinkTypeRecogniser;


public abstract class LinkCodeBase implements LinkCode {

    protected final LinkTypeRecogniser linkTypeRecogniser;
    protected final ShapeHelperWrapper shapeHelper;
    protected ConnectedShapesComplex connectedShapesComplex;

    protected Node.NodeType fromNodeType(){
        return type(from());
    }

    protected Node.NodeType type(XShape shape){
        return shapeHelper.getNodeType(shape);
    }

    protected Node.NodeType toNodeType(){
        return shapeHelper.getNodeType(connectedShapesComplex.toShape);
    }

    protected XShape from(){
        return connectedShapesComplex.fromShape;
    }

    protected XShape to(){
        return connectedShapesComplex.toShape;
    }

    protected XShape text(){
        return connectedShapesComplex.textShape;
    }

    protected String text(XShape shape){
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
            default: c = ' ';
        }
        return c;
    }

    public void setConnectedShapesComplex(ConnectedShapesComplex connectedShapesComplex) {
        this.connectedShapesComplex = connectedShapesComplex;
    }

    public LinkCodeBase(ConnectedShapesComplex connectedShapesComplex, LinkTypeRecogniser linkTypeRecogniser, ShapeHelperWrapper shapeHelper){
        this.connectedShapesComplex = connectedShapesComplex;
        this.linkTypeRecogniser = linkTypeRecogniser;
        this.shapeHelper = shapeHelper;
    }
}
