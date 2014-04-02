package ru.ssau.graphplus.link;

import com.sun.star.awt.Point;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import ru.ssau.graphplus.QI;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControlLink extends LinkBase implements Linker, Serializable {

    private static final long serialVersionUID = 1L;


    public ControlLink(XMultiServiceFactory xmsf, XComponent xComp, String c) {
        super(xmsf, xComp, c);

    }

    ControlLink() {

    }

    @Override
    protected LinkShapes buildShapes(XMultiServiceFactory xMSF, XComponent xDrawDoc) {

        LinkShapes linkShapes = super.buildShapes(xMSF, xDrawDoc);

        try {


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

    @Override
    protected LinkStyle getStyle() {
        return new LinkStyleBase() {
            @Override
            public void applyStyleForHalf1(XPropertySet xPS1) throws UnknownPropertyException, PropertyVetoException, WrappedTargetException, IllegalArgumentException {
                xPS1.setPropertyValue("LineColor", new Integer(0x000000));
                xPS1.setPropertyValue("EdgeKind", com.sun.star.drawing.ConnectorType.CURVE);

            }

            @Override
            public void applyStyleForHalf2(XPropertySet xPS2) throws UnknownPropertyException, PropertyVetoException, WrappedTargetException, IllegalArgumentException {
                xPS2.setPropertyValue("EdgeKind", com.sun.star.drawing.ConnectorType.CURVE);
                xPS2.setPropertyValue("LineEndName", "Arrow");
                xPS2.setPropertyValue("LineColor", new Integer(0x000000));
            }
        };
    }

    public void link(XShape sh1, XShape sh2) {
        try {
            this.startShape = sh1;
            this.endShape = sh2;
            xPS1.setPropertyValue("StartShape", sh1);
            xPS2.setPropertyValue("EndShape", sh2);
            textShape.setPosition(new Point((sh1.getPosition().X + sh2.getPosition().X) / 2, (sh1.getPosition().Y + sh2.getPosition().Y) / 2));
            super.link(sh1, sh2);
        } catch (UnknownPropertyException ex) {
            Logger.getLogger(ControlLink.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(ControlLink.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ControlLink.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(ControlLink.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


}
