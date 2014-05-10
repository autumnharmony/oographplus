/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl;

import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.codegen.impl.recognition.LinkTypeRecogniser;


public class DataAndControlMixedCode extends LinkCodeBase {
    public DataAndControlMixedCode(ConnectedShapesComplex connectedShapesComplex, LinkTypeRecogniser linkTypeRecogniser, ShapeHelperWrapper shapeHelper) {
        super(connectedShapesComplex, linkTypeRecogniser, shapeHelper);
    }

    @Override
    public String getCode() {

//        1->(3|4): port: Type ! Message -> method; method(port?Message);
//        2->(3|4): port: Type ? Message -> method; method(port?Message);

        StringBuffer buffer = new StringBuffer();
        buffer
                .append(text(from()))
                .append(getPortChar(from()))
                .append(text(textShape()))
                .append(" -> ")
                .append(text(to()))
                .append("; ")
                .append(text(to())).append("(").append(text(from())).append("?").append(text(textShape())).append(");");
        return buffer.toString();
    }
}
