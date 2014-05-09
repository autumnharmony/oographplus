/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.recognition;

import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.api.Link;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 4/24/14
 * Time: 2:02 AM
 * To change this template use File | Settings | File Templates.
 */
public interface LinkTypeRecogniser {

    Link.LinkType getType(XConnectorShape connectorShape1, XShape text, XConnectorShape connectorShape2);
//    Link.LinkType getType(XConnectorShape connectorShape1);

}
