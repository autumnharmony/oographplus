/*
 * Copyright (c) 2013. Anton Borisov
 */

package ru.ssau.graphplus;

import com.sun.star.drawing.XShape;

/**
* User: anton
* Date: 9/7/13
* Time: 11:26 PM
*/
public interface PostCreationAction {
    public abstract void postCreate(XShape shape);
}
