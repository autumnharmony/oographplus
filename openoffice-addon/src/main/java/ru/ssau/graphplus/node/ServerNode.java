
package ru.ssau.graphplus.node;

import com.sun.star.awt.Point;
import com.sun.star.awt.Size;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexContainer;
import com.sun.star.drawing.*;
import com.sun.star.lang.*;
import com.sun.star.uno.UnoRuntime;
import ru.ssau.graphplus.commons.PostCreationAction;
import ru.ssau.graphplus.commons.QI;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author anton
 */
public class ServerNode extends NodeBase implements Serializable {

    private static final long serialVersionUID = 1L;


    public ServerNode(String id) {
        super(id);
    }

    public ServerNode() {
        super();
        if (postCreationAction == null) {
            postCreationAction = new PostCreationAction() {
                @Override
                public void postCreate(XShape shape) {
                    XGluePointsSupplier xGluePointsSupplier = UnoRuntime.queryInterface(XGluePointsSupplier.class, xShape);
                    XIndexContainer gluePoints = xGluePointsSupplier.getGluePoints();
                    for (int i = 0; i < gluePoints.getCount(); i++) {
                        try {
                            gluePoints.removeByIndex(i);
                        } catch (com.sun.star.lang.IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        } catch (WrappedTargetException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        gluePoints.insertByIndex(gluePoints.getCount(), new GluePoint2(new Point(2500, 0), Boolean.TRUE, Alignment.CENTER, EscapeDirection.RIGHT, Boolean.TRUE));
                    } catch (com.sun.star.lang.IllegalArgumentException ex) {

                    } catch (com.sun.star.lang.IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    } catch (WrappedTargetException e) {
                        e.printStackTrace();
                    }
                }
            };
        }
    }

    public XShape buildShape(XMultiServiceFactory xMSF) {

        try {

            int dx = 100;
            int dy = 200;
            int w = 1200;


            Point[][] points = new Point[][]{
                    {new Point(0 + dx, 1200 + dy),
                            new Point(1200 + dx, 1200 + dy),
                            new Point(900 + dx, 600 + dy),
                            new Point(1200 + dx, 0 + dy),
                            new Point(0 + dx, 0 + dy),
                            new Point(0 + dx, 1200 + dy)}
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
        } catch (com.sun.star.uno.Exception ex) {
            Logger.getLogger(ClientNode.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }


    }
}
