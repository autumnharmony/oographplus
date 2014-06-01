
package ru.ssau.graphplus.node;

import com.sun.star.awt.Point;
import com.sun.star.awt.Size;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.*;
import com.sun.star.lang.*;
import ru.ssau.graphplus.codegen.impl.CodeProviderAnnotation;
import ru.ssau.graphplus.codegen.impl.ServerNodeCode;
import ru.ssau.graphplus.commons.QI;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

@CodeProviderAnnotation(codeProvider = ServerNodeCode.class)
public class ServerNode extends NodeBase implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String POLY_POLYGON = "PolyPolygon";

    public ServerNode(String id) {
        super(id);
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
            xPropSet.setPropertyValue(POLY_POLYGON, points);
            xPropSet.setPropertyValue(FILL_COLOR, new Integer(0x99CCFF));
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
