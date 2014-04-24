package ru.ssau.graphplus.node;

import com.sun.star.awt.Point;
import com.sun.star.awt.Size;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.HomogenMatrix3;
import com.sun.star.drawing.HomogenMatrixLine3;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XMultiServiceFactory;
import ru.ssau.graphplus.OOGraph;
import ru.ssau.graphplus.commons.QI;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientNode extends NodeBase implements Serializable {

    // PolyPolygonDescriptor service included in PolyPolygonShape service
    public static final String POLY_POLYGON = "PolyPolygon";

    public static final String FILL_COLOR = "FillColor";
    public static final String SIZE = "Size";
    public static final String TRANSFORMATION = "Transformation";
    private static final long serialVersionUID = 1L;



    public ClientNode() {
        super();
    }

    public ClientNode(String id) {
        super(id);
    }


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

            Object obj = xMSF.createInstance("com.sun.star.drawing.PolyPolygonShape");
            XPropertySet xPropSet = QI.XPropertySet(obj);
            xPropSet.setPropertyValue(POLY_POLYGON, points);
            xPropSet.setPropertyValue(FILL_COLOR, new Integer(0x99CCFF));
            xPropSet.setPropertyValue(SIZE, new Size(900, 800));
            XShape xShape = QI.XShape(obj);
            HomogenMatrix3 hm = new HomogenMatrix3(new HomogenMatrixLine3(-1, 0, 0), new HomogenMatrixLine3(0, 1, 0), new HomogenMatrixLine3(0, 0, 1));
            xPropSet.setPropertyValue(TRANSFORMATION, hm);
            return xShape;

        } catch (com.sun.star.uno.Exception ex) {
            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
