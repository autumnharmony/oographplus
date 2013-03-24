/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus.link;

import com.sun.star.awt.Point;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.drawing.XShapeGrouper;
import com.sun.star.drawing.XShapes;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;
import ru.ssau.graphplus.QI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author anton
 */
public class ControlLink extends Link implements Linker {


    public ControlLink(XMultiServiceFactory xmsf, XDrawPage xDP, XComponent xComp) {
        super(xmsf, xDP, xComp);

    }

    public ControlLink() {

    }

    @Override
    protected LinkShapes buildShapes(XMultiServiceFactory xMSF, XDrawPage xDP, XComponent xDrawDoc) {
        super.buildShapes(xMSF, xDP, xDrawDoc);
        try {


            Object half1 = xMSF.createInstance("com.sun.star.drawing.ConnectorShape");
            Object half2 = xMSF.createInstance("com.sun.star.drawing.ConnectorShape");
            Object text = xMSF.createInstance("com.sun.star.drawing.TextShape");
            XShape xConnSh1 = QI.XShape(half1);
            XShape xConnSh2 = QI.XShape(half2);
            XShape xTextSh = QI.XShape(text);
            textShape = xTextSh;
            XShapeGrouper xShapeGrouper = (XShapeGrouper) UnoRuntime.queryInterface(XShapeGrouper.class, xDP);
            XComponent xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, xDP);

            XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, xDP);
            xShapes.add(xConnSh1);
            xShapes.add(xConnSh2);
            xShapes.add(xTextSh);
            this.connShape1 = xConnSh1;
            this.connShape2 = xConnSh2;
            this.textShape = xTextSh;


            xPS1 = QI.XPropertySet(xConnSh1);
            xPS1.setPropertyValue("EndShape", xTextSh);
            xPS1.setPropertyValue("LineColor", new Integer(0x000000));

            xPS1.setPropertyValue("EdgeKind", com.sun.star.drawing.ConnectorType.CURVE);

            xPS2 = QI.XPropertySet(xConnSh2);
            xPS2.setPropertyValue("StartShape", xTextSh);
            xPS2.setPropertyValue("EdgeKind", com.sun.star.drawing.ConnectorType.CURVE);
            xPS2.setPropertyValue("LineEndName", "Arrow");
            xPS2.setPropertyValue("LineColor", new Integer(0x000000));



            //                     Object obj2 = xMSF.createInstance("com.sun.star.drawing.PointSequence");


            List<XShape> list = new ArrayList<XShape>();
            list.add(xConnSh1);
            list.add(xConnSh2);
            list.add(xTextSh);

            return new LinkShapes(xTextSh, xConnSh1, xConnSh2);
        } catch (java.lang.Exception ex) {
            Logger.getLogger(ControlLink.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (Exception ex) {
//            Logger.getLogger(ControlLink.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Collection<XShape> buildShapes(XMultiServiceFactory xMSF, XDrawPage xDP, XComponent xDrawDoc, Boolean textShapeFlag) {
        //Collection<XShape> xShapes1 = super.buildShapes(xMSF, xDP, xDrawDoc, textShapeFlag);
        try {


            Object half1 = xMSF.createInstance("com.sun.star.drawing.ConnectorShape");
            Object half2 = xMSF.createInstance("com.sun.star.drawing.ConnectorShape");
            Object text = xMSF.createInstance("com.sun.star.drawing.TextShape");
            XShape xConnSh1 = QI.XShape(half1);
            XShape xConnSh2 = QI.XShape(half2);
            XShape xTextSh = QI.XShape(text);
            textShape = xTextSh;
            XShapeGrouper xShapeGrouper = (XShapeGrouper) UnoRuntime.queryInterface(XShapeGrouper.class, xDP);
            XComponent xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, xDP);

            XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, xDP);
            xShapes.add(xConnSh1);
            xShapes.add(xConnSh2);
            xShapes.add(xTextSh);
            this.connShape1 = xConnSh1;
            this.connShape2 = xConnSh2;
            this.textShape = xTextSh;


            xPS1 = QI.XPropertySet(xConnSh1);
            xPS1.setPropertyValue("EndShape", xTextSh);
            xPS1.setPropertyValue("LineColor", new Integer(0x000000));

            xPS1.setPropertyValue("EdgeKind", com.sun.star.drawing.ConnectorType.CURVE);

            xPS2 = QI.XPropertySet(xConnSh2);
            xPS2.setPropertyValue("StartShape", xTextSh);
            xPS2.setPropertyValue("EdgeKind", com.sun.star.drawing.ConnectorType.CURVE);
            xPS2.setPropertyValue("LineEndName", "Arrow");
            xPS2.setPropertyValue("LineColor", new Integer(0x000000));



            List<XShape> list = new ArrayList<XShape>();
            list.add(xConnSh1);
            list.add(xConnSh2);
            list.add(xTextSh);

            return list;
        } catch (java.lang.Exception ex) {
            Logger.getLogger(ControlLink.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
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
