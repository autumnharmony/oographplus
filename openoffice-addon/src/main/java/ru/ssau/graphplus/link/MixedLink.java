package ru.ssau.graphplus.link;

import com.sun.star.awt.Point;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.ConnectorType;
import com.sun.star.drawing.LineStyle;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.*;
import com.sun.star.lang.IllegalArgumentException;
import ru.ssau.graphplus.commons.QI;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 Mixed link can be only with two connectors
 */
public class MixedLink extends LinkTwoConnectorsAndTextBase implements Serializable {

    private static final long serialVersionUID = 1L;


    public MixedLink(XMultiServiceFactory xmsf, String c) {
        super(xmsf, c);
    }

    MixedLink() {

    }

    @Override
    protected LinkStyle getStyle() {
        return new LinkStyleBase() {
            @Override
            public void applyStyleForHalf1(XPropertySet xPS1) throws UnknownPropertyException, PropertyVetoException, WrappedTargetException, com.sun.star.lang.IllegalArgumentException {

                xPS1.setPropertyValue("LineColor", new Integer(0x000000));
                xPS1.setPropertyValue("EdgeKind", com.sun.star.drawing.ConnectorType.CURVE);
            }

            @Override
            public void applyStyleForHalf2(XPropertySet xPS2) throws UnknownPropertyException, PropertyVetoException, WrappedTargetException, IllegalArgumentException {
                xPS2.setPropertyValue("LineStyle", LineStyle.DASH);
                xPS2.setPropertyValue("LineDashName", "Fine Dashed");
                xPS2.setPropertyValue("EdgeKind", com.sun.star.drawing.ConnectorType.CURVE);
//                xPS2.setPropertyValue("EdgeKind", ConnectorType.LINE);
//                xPS2.setPropertyValue("LineEndName", "Circle");
                xPS2.setPropertyValue("LineColor", new Integer(0x000000));

            }
        };
    }

    @Override
    public LinkShapes buildShapes(XMultiServiceFactory xMSF) {
        try {
            LinkShapes linkShapes = super.buildShapes(xMSF);

            Object half1 = xMSF.createInstance("com.sun.star.drawing.ConnectorShape");
            Object half2 = xMSF.createInstance("com.sun.star.drawing.ConnectorShape");

            XShape xConnSh1 = QI.XShape(half1);
            XShape xConnSh2 = QI.XShape(half2);
            XShape xTextSh = linkShapes.textShape;

            xPS1 = QI.XPropertySet(xConnSh1);


            xPS2 = QI.XPropertySet(xConnSh2);


            linkShapes.connShape1 = xConnSh1;
            linkShapes.connShape2 = xConnSh2;
            return linkShapes;
        } catch (java.lang.Exception ex) {
            Logger.getLogger(ControlLink.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }





}
