/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.analizer;

import com.sun.star.drawing.XShape;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 3/17/14
 * Time: 12:07 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Match {
    boolean matches(XShape xShape1, XShape xShape2);
}