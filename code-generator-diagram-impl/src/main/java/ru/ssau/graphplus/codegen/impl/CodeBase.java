/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl;

import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.commons.QI;

/**
 */
public class CodeBase {

    protected String portName(XShape shape) {
        return portName(QI.XText(shape).getString());
    }

    protected String portName(String name){
        String[] split = name.split(":");
        if (split.length == 0) {
            throw new IllegalArgumentException();
        }
        return split[0];
    }


}
