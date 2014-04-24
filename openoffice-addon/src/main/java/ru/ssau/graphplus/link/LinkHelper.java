/*
 * Copyright (c) 2013. Anton Borisov
 */

package ru.ssau.graphplus.link;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.WrappedTargetException;
import ru.ssau.graphplus.commons.ConnectedShapes;
import ru.ssau.graphplus.commons.QI;

/**
 * User: anton
 * Date: 6/13/13
 * Time: 1:05 AM
 */
public class LinkHelper {
    public static XShape getStartShapeStatic(XShape xShape) {
        try {
            return QI.XShape(QI.XPropertySet(xShape).getPropertyValue(ConnectedShapes.START_SHAPE));
        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static XShape getEndShapeStatic(XShape xShape) {
        try {
            return QI.XShape(QI.XPropertySet(xShape).getPropertyValue(ConnectedShapes.END_SHAPE));
        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
