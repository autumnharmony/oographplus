/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.commons;

import com.sun.star.drawing.XShape;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 4/23/14
 * Time: 12:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class MiscHelperWrapperImpl implements MiscHelperWrapper {
    @Override
    public String getNodeType(XShape xShape) {
        return MiscHelper.getNodeType(xShape);
    }
}
