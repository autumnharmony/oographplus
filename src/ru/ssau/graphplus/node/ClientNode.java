/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus.node;

import ru.ssau.graphplus.QI;
import ru.ssau.graphplus.ShapeHelper;
import com.sun.star.awt.Point;
import com.sun.star.awt.Size;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.*;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.Exception;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author anton
 */
public class ClientNode extends Node {


    public XShape buildShape(XMultiServiceFactory xMSF) {
        try {
            
            int dx = 100;
            int dy = 200;
            int w = 1200;
            
            
            Point[][] points = new Point[][]{
                {new Point(0+dx, 1200+dy),
                    new Point(1200+dx, 1200+dy),
                    new Point(900+dx, 600+dy),
                    new Point(1200+dx, 0+dy),
                    new Point(0+dx, 0+dy),
                    new Point(0+dx, 1200+dy)}
            };
            Size size = new Size(1000, 800);
            //                    WritingMode2;

            PolygonFlags[][] flags = new PolygonFlags[][]{
                {PolygonFlags.NORMAL,
                    PolygonFlags.NORMAL,
                    PolygonFlags.NORMAL,
                    PolygonFlags.NORMAL,
                    PolygonFlags.NORMAL}
            };
            com.sun.star.drawing.PolyPolygonBezierCoords coodrs = new PolyPolygonBezierCoords(points, flags);
            Object obj = xMSF.createInstance("com.sun.star.drawing.PolyPolygonShape");
            //                     Object obj2 = xMSF.createInstance("com.sun.star.drawing.PointSequence");
            XPropertySet xPropSet = QI.XPropertySet(obj);
            xPropSet.setPropertyValue("PolyPolygon", points);
            xPropSet.setPropertyValue("FillColor", new Integer(0x99CCFF));
//            xPropSet.setPropertyValue("Size", size);
            XShape xShape = (XShape) QI.XShape(obj);
            
//            ShapeHelper.addPortion(xShape, "client", false);

            
            int scale = 3000;
            // ???????????????????????????????????? ?????? 2pi ?????????????????? OX ??????????????? ??????????????? ??????????????? ??????????????????
            
            HomogenMatrix3 hm = new HomogenMatrix3(new HomogenMatrixLine3(-1, 0, 0), new HomogenMatrixLine3(0, 1, 0), new HomogenMatrixLine3(0, 0, 1));
            
            xPropSet.setPropertyValue("Transformation", hm);
                
//            hm = new HomogenMatrix3(new HomogenMatrixLine3(scale, 0, 0), new HomogenMatrixLine3(0, scale, 0), new HomogenMatrixLine3(0, 0, scale));
//            xPropSet.setPropertyValue("Transformation", hm);
            
            
            return xShape;
        } catch (Exception ex) {
            Logger.getLogger(ClientNode.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
