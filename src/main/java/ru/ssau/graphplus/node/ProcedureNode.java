package ru.ssau.graphplus.node;

import com.sun.star.awt.Point;
import com.sun.star.awt.Size;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XMultiServiceFactory;
import ru.ssau.graphplus.DrawHelper;
import ru.ssau.graphplus.QI;
import ru.ssau.graphplus.ShapeHelper;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProcedureNode extends Node implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String CORNER_RADIUS = "CornerRadius";
    public static final String FILL_COLOR = "FillColor";

    public XShape buildShape(XMultiServiceFactory xMSF) {
        try {
            xShape = ShapeHelper.createShape(xMSF, new Point(100, 100), new Size(800, 800), DrawHelper.SHAPE_KIND_RECTANGLE);

            XPropertySet xPS = QI.XPropertySet(xShape);
            xPS.setPropertyValue(CORNER_RADIUS, new Integer(500));
            xPS.setPropertyValue(FILL_COLOR, new Integer(0x99CCFF));

            return xShape;
        } catch (Exception ex) {
            Logger.getLogger(ProcedureNode.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

}


