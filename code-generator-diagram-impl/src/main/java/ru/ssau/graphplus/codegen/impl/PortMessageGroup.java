/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl;

import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.codegen.impl.recognition.LinkTypeRecogniser;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;

import java.util.Iterator;
import java.util.List;

/**
 */
public class PortMessageGroup extends LinkCodeGroupBase<PortMessageCode> implements GroupingCode<PortMessageCode> {


    public PortMessageGroup(ConnectedShapesComplex connectedShapesComplex, LinkTypeRecogniser linkTypeRecogniser, ShapeHelperWrapper shapeHelper) {
        super(connectedShapesComplex, linkTypeRecogniser, shapeHelper);
    }

    @Override
    public String getCode() {
        StringBuffer buffer = new StringBuffer();

        Iterator<PortMessageCode> iterator = group.iterator();


        PortMessageCode first = group.iterator().next();
        XShape to = first.to();

        XShape from = first.from();
        boolean methodIsFrom = type(from).equals(Node.NodeType.StartMethodOfProcess) || type(from).equals(Node.NodeType.MethodOfProcess);

        boolean methodIsTo = false;
        if (methodIsFrom) {
            String text = text(from);
            buffer.append(text);
        } else {
            methodIsTo = type(from).equals(Node.NodeType.ServerPort) || type(from).equals(Node.NodeType.ClientPort);
            if (methodIsTo) {
                buffer.append(text(to));
            }
        }

        buffer.append("(");


        boolean isFirst = true;

        for (PortMessageCode pmc : group) {

            if (isFirst) {
                isFirst = false;
            } else {
                buffer.append(',');
            }

            buffer.append(methodIsFrom ? pmc.portName(pmc.to()) : text(pmc.from()));
            buffer.append(methodIsFrom ? "!" : "");
            buffer.append(methodIsTo ? "?" : "");
            buffer.append(pmc.linkText());
        }
        buffer.append(");");

        return buffer.toString();
    }


    public String toString(){
        return super.toString() + " code:" + getCode();
    }

}