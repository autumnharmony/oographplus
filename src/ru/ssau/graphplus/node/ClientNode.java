/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus.node;

import com.sun.star.container.XIndexContainer;
import com.sun.star.lang.*;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.uno.UnoRuntime;
import ru.ssau.graphplus.QI;
import ru.ssau.graphplus.ShapeHelper;
import com.sun.star.awt.Point;
import com.sun.star.awt.Size;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.*;
import com.sun.star.uno.Exception;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author anton
 */
public class ClientNode extends Node {


    public ClientNode() {
        super();
        if (postCreationAction == null) {
            postCreationAction = new PostCreationAction() {
                @Override
                public void postCreate(XShape shape) {
                    XGluePointsSupplier xGluePointsSupplier = UnoRuntime.queryInterface(XGluePointsSupplier.class, xShape);
                    XIndexContainer gluePoints = xGluePointsSupplier.getGluePoints();
                    for (int i = 0; i < gluePoints.getCount(); i++){
                        try {
                            gluePoints.removeByIndex(i);
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        } catch (WrappedTargetException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }
                    try {
                            gluePoints.insertByIndex(gluePoints.getCount(), new GluePoint2(new Point(2500,0), Boolean.TRUE, Alignment.CENTER, EscapeDirection.RIGHT , Boolean.TRUE));
                    } catch (com.sun.star.lang.IllegalArgumentException ex){
                        
                    } catch (com.sun.star.lang.IndexOutOfBoundsException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (WrappedTargetException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            };
        }
    }

    public ClientNode(PostCreationAction postCreationAction) {
       super(postCreationAction);
    }

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
            Object shape = xMSF.createInstance("com.sun.star.drawing.PolyPolygonShape");


            XPropertySet xPropSet = QI.XPropertySet(shape);
            xPropSet.setPropertyValue("PolyPolygon", points);
            xPropSet.setPropertyValue("FillColor", new Integer(0x99CCFF));
            XShape xShape = QI.XShape(shape);
            

            HomogenMatrix3 hm = new HomogenMatrix3(new HomogenMatrixLine3(-1, 0, 0), new HomogenMatrixLine3(0, 1, 0), new HomogenMatrixLine3(0, 0, 1));
            
            xPropSet.setPropertyValue("Transformation", hm);
                

            
            
            return xShape;
        } catch (Exception ex) {
            Logger.getLogger(ClientNode.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
