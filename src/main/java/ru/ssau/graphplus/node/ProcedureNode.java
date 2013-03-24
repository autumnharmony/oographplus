/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus.node;

import ru.ssau.graphplus.DrawHelper;
import ru.ssau.graphplus.QI;
import ru.ssau.graphplus.ShapeHelper;
import com.sun.star.awt.Point;
import com.sun.star.awt.Size;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XMultiServiceFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author anton
 */
public class ProcedureNode extends Node {
    public static final String CORNER_RADIUS = "CornerRadius";
//    public static final long RADIUS_VALUE = new Long(50).longValue();

    
    
    public XShape buildShape(XMultiServiceFactory xMSF) {
        try {
            xShape = ShapeHelper.createShape(xMSF, new Point(100, 100), new Size(800, 800), DrawHelper.SHAPE_KIND_RECTANGLE);
//            ShapeHelper.addPortion(xShape, "procedure", false);
            XPropertySet xPS = QI.XPropertySet(xShape);
            xPS.setPropertyValue(CORNER_RADIUS, new Integer(500));
            xPS.setPropertyValue("FillColor", new Integer(0x99CCFF));
            
            return xShape;
        } catch (Exception ex) {
            Logger.getLogger(ProcedureNode.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
    }
    
}