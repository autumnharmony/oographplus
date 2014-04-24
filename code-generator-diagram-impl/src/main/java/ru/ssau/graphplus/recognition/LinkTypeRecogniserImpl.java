/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.recognition;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.LineStyle;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.WrappedTargetException;
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.api.Link;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 4/24/14
 * Time: 2:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class LinkTypeRecogniserImpl implements LinkTypeRecogniser {

    public static final String LINE_STYLE = "LineStyle";

    @Override
    public Link.LinkType getType(XConnectorShape connectorShape1, XShape text, XConnectorShape connectorShape2) {
        if (text != null) {
            XPropertySet conn1PS = QI.XPropertySet(connectorShape1);
            XPropertySet conn2PS = QI.XPropertySet(connectorShape2);

            Object lineStyleObj = null;
            try {
                lineStyleObj = conn1PS.getPropertyValue(LINE_STYLE);
                LineStyle lineStyle1 = (LineStyle) lineStyleObj;

                lineStyleObj = conn2PS.getPropertyValue(LINE_STYLE);
                LineStyle lineStyle2 = (LineStyle) lineStyleObj;

                if (lineStyle1.equals(LineStyle.DASH) && lineStyle2.equals(LineStyle.DASH))
                    return Link.LinkType.DataFlow;
                if (lineStyle1.equals(LineStyle.SOLID) && lineStyle2.equals(LineStyle.SOLID_value))
                    return Link.LinkType.ControlFlow;

                if (lineStyle1.equals(LineStyle.DASH) && lineStyle2.equals(LineStyle.SOLID_value))
                    return Link.LinkType.MixedFlow;
                if (lineStyle2.equals(LineStyle.DASH) && lineStyle1.equals(LineStyle.SOLID_value))
                    return Link.LinkType.MixedFlow;


            } catch (UnknownPropertyException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (WrappedTargetException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        } else {
            XConnectorShape xConnectorShape = connectorShape1 != null ? connectorShape1 : connectorShape2;
            if (xConnectorShape != null) {
                XPropertySet xPropertySet = QI.XPropertySet(xConnectorShape);
                try {
                    LineStyle lineStyle = (LineStyle) xPropertySet.getPropertyValue(LINE_STYLE);
                    return lineStyle.equals(LineStyle.SOLID)? Link.LinkType.ControlFlow : Link.LinkType.DataFlow;
                } catch (UnknownPropertyException | WrappedTargetException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

            }
        }

        throw new IllegalArgumentException("Strange link, can't determine it's type");
    }
}
