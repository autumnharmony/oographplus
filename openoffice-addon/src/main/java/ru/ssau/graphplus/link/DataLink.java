
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
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.commons.QI;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author anton
 */
public interface DataLink extends Link,Serializable,ShapesProvider  {


    class DataLinkTwoConnectors extends LinkTwoConnectorsAndTextBase implements DataLink{

        private static final long serialVersionUID = 1L;


        public DataLinkTwoConnectors(XMultiServiceFactory xmsf, String c) {
            super(xmsf, c);
        }

        DataLinkTwoConnectors() {

        }

        public LinkShapes buildShapes(XMultiServiceFactory xMSF) {
            try {
                LinkShapes linkShapes = super.buildShapes(xMSF);

                Object half1 = xMSF.createInstance("com.sun.star.drawing.ConnectorShape");
                Object half2 = xMSF.createInstance("com.sun.star.drawing.ConnectorShape");
                XShape xConnSh1 = QI.XShape(half1);
                XShape xConnSh2 = QI.XShape(half2);
                linkShapes.connShape1 = xConnSh1;
                linkShapes.connShape2 = xConnSh2;


                XShape xTextSh = linkShapes.textShape;

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

        @Override
        protected LinkStyle getStyle() {
            return new LinkStyleBase() {
                @Override
                public void applyStyleForHalf1(XPropertySet xPS1) throws UnknownPropertyException, PropertyVetoException, WrappedTargetException, com.sun.star.lang.IllegalArgumentException {

                    xPS1.setPropertyValue("LineStyle", LineStyle.DASH);
                    xPS1.setPropertyValue("LineDashName", "Fine Dashed");

                    xPS1.setPropertyValue("EdgeKind", ConnectorType.LINE);
                    xPS1.setPropertyValue("LineColor", new Integer(0x000000));
                }

                @Override
                public void applyStyleForHalf2(XPropertySet xPS2) throws UnknownPropertyException, PropertyVetoException, WrappedTargetException, IllegalArgumentException {

                    xPS2.setPropertyValue("EdgeKind", ConnectorType.LINE);
                    xPS2.setPropertyValue("LineEndName", "Arrow");
                    xPS2.setPropertyValue("LineStyle", LineStyle.DASH);
                    xPS2.setPropertyValue("LineDashName", "Fine Dashed");
                    xPS2.setPropertyValue("LineColor", new Integer(0x000000));
                }
            };
        }



    }
    class DataLinkOneConnector extends LinkOneConnectorBase implements DataLink{
        private static final long serialVersionUID = 1L;


        public DataLinkOneConnector (XMultiServiceFactory xmsf, String c) {
            super(xmsf, c);
        }

        DataLinkOneConnector() {

        }

        public LinkShapes buildShapes(XMultiServiceFactory xMSF) {
            try {
                LinkShapes linkShapes = super.buildShapes(xMSF);
                Object half1 = xMSF.createInstance("com.sun.star.drawing.ConnectorShape");
                XShape xConnSh1 = QI.XShape(half1);
                linkShapes.connShape1 = xConnSh1;
                xPS = QI.XPropertySet(xConnSh1);
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
                public void applyStyleForHalf1(XPropertySet xPS1) throws UnknownPropertyException, PropertyVetoException, WrappedTargetException, com.sun.star.lang.IllegalArgumentException {

                    xPS1.setPropertyValue("LineStyle", LineStyle.DASH);
                    xPS1.setPropertyValue("LineDashName", "Fine Dashed");

                    xPS1.setPropertyValue("EdgeKind", ConnectorType.LINE);
                    xPS1.setPropertyValue("LineColor", new Integer(0x000000));
                    xPS1.setPropertyValue("LineEndName", "Arrow");

                }

                @Override
                public void applyStyleForHalf2(XPropertySet xPS2) throws UnknownPropertyException, PropertyVetoException, WrappedTargetException, IllegalArgumentException {
                }
            };
        }



    }
}
