/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus.node;

import ru.ssau.graphplus.OOGraph;
import ru.ssau.graphplus.PageHelper;
import ru.ssau.graphplus.QI;
import ru.ssau.graphplus.ShapeHelper;
import com.sun.star.awt.Point;
import com.sun.star.awt.Size;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.*;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.rendering.XPolyPolygon2D;
import com.sun.star.uno.UnoRuntime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author anton
 */
public class ServerNode extends Node {

    public XShape buildShape(XMultiServiceFactory xMSF) {
        try {
            Point[][] points = new Point[][]{
                {new Point(0, 0),
                    new Point(300, 0),
                    new Point(400, 200),
                    new Point(300, 400),
                    new Point(0, 400),
                    new Point(0, 0)}
            };

//            com.sun.star.drawing.PolyPolygonBezierCoords coodrs = new PolyPolygonBezierCoords(points, flags);
            Object obj = xMSF.createInstance("com.sun.star.drawing.PolyPolygonShape");
            //                     Object obj2 = xMSF.createInstance("com.sun.star.drawing.PointSequence");
            XPropertySet xPropSet = QI.XPropertySet(obj);
            xPropSet.setPropertyValue("PolyPolygon", points);
            xPropSet.setPropertyValue("FillColor", new Integer(0x99CCFF));
            xPropSet.setPropertyValue("Size", new Size(900, 800));
//            xPropSet.setPropertyValue("Text", new String("server"));
            
            XShape xShape = (XShape) QI.XShape(obj);
//            ShapeHelper.addPortion(xShape, "server", true);

            HomogenMatrix3 hm = new HomogenMatrix3(new HomogenMatrixLine3(-1, 0, 0), new HomogenMatrixLine3(0, 1, 0), new HomogenMatrixLine3(0, 0, 1));
//
            xPropSet.setPropertyValue("Transformation", hm);

            return xShape;
        } catch (com.sun.star.uno.Exception ex) {
            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
