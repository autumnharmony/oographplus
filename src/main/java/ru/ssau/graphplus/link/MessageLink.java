/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus.link;

import com.sun.star.awt.Point;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.drawing.ConnectorType;
import com.sun.star.drawing.LineStyle;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.*;
import ru.ssau.graphplus.QI;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author anton
 */
public class MessageLink extends Link implements Linker, Serializable {

    private static final long serialVersionUID = 1L;


    public MessageLink(XMultiServiceFactory xmsf, XDrawPage xDP, XComponent xComp, String c) {
        super(xmsf, xDP, xComp, c);
    }

    public MessageLink() {

    }

    public LinkShapes buildShapes(XMultiServiceFactory xMSF, XDrawPage xDP, XComponent xDrawDoc) {
        try {
            LinkShapes linkShapes = super.buildShapes(xMSF, xDP, xDrawDoc);

            Object half1 = xMSF.createInstance("com.sun.star.drawing.ConnectorShape");
            Object half2 = xMSF.createInstance("com.sun.star.drawing.ConnectorShape");
            XShape xConnSh1 = QI.XShape(half1);
            XShape xConnSh2 = QI.XShape(half2);
            linkShapes.setConnShape1(xConnSh1);
            linkShapes.setConnShape2(xConnSh2);


            XShape xTextSh = linkShapes.getTextShape();

            xPS1 = QI.XPropertySet(xConnSh1);


            xPS2 = QI.XPropertySet(xConnSh2);


            return linkShapes;
        } catch (java.lang.Exception ex) {
            Logger.getLogger(ControlLink.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (Exception ex) {
//            Logger.getLogger(ControlLink.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void setProps() {
        super.setProps();

        try {
            xPS1.setPropertyValue("EndShape", getTextShape());
            xPS1.setPropertyValue("LineStyle", LineStyle.DASH);
            xPS1.setPropertyValue("LineDashName", "Fine Dashed");

            xPS1.setPropertyValue("EdgeKind", ConnectorType.LINE);
            xPS1.setPropertyValue("LineColor", new Integer(0x000000));

            xPS2.setPropertyValue("StartShape", getTextShape());
            xPS2.setPropertyValue("EdgeKind", ConnectorType.LINE);
            xPS2.setPropertyValue("LineEndName", "Arrow");
            xPS2.setPropertyValue("LineStyle", LineStyle.DASH);
            xPS2.setPropertyValue("LineDashName", "Fine Dashed");
            xPS2.setPropertyValue("LineColor", new Integer(0x000000));

        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        } catch (com.sun.star.lang.IllegalArgumentException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
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
        } catch (com.sun.star.lang.IllegalArgumentException ex) {
            Logger.getLogger(ControlLink.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(ControlLink.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
