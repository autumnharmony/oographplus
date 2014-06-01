
package ru.ssau.graphplus.node;

import com.sun.star.awt.Point;
import com.sun.star.awt.Size;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XMultiServiceFactory;
import ru.ssau.graphplus.DrawHelper;
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.commons.ShapeHelper;

import java.awt.*;
import java.io.Serializable;

public class ProcessNode extends NodeBase implements Serializable {

    private static final long serialVersionUID = 1L;

    public ProcessNode(String id) {
        super(id);
    }

    public XShape buildShape(XMultiServiceFactory xMSF) {
        try {
            XShape xShape = ShapeHelper.createShape(xMSF, new Point(100, 100), new Size(800, 800), DrawHelper.SHAPE_KIND_RECTANGLE);
            XPropertySet xPS = QI.XPropertySet(xShape);
            xPS.setPropertyValue("FillColor", new Integer(0x99CCFF));
            return xShape;
        } catch (Exception ex) {
            return null;
        }
    }
}
