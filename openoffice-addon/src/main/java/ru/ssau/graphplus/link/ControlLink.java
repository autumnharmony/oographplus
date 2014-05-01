package ru.ssau.graphplus.link;

import com.sun.star.awt.Point;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XMultiServiceFactory;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.commons.QI;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface ControlLink extends Link,Serializable,ShapesProvider {

    class ControlLinkTwoConnectorsAndText extends LinkTwoConnectorsAndTextBase implements ControlLink,ShapesProvider {

        private static final long serialVersionUID = 1L;


        public ControlLinkTwoConnectorsAndText(XMultiServiceFactory xmsf, String c) {
            super(xmsf, c);

        }

        ControlLinkTwoConnectorsAndText() {

        }

        @Override
        protected LinkShapes buildShapes(XMultiServiceFactory xMSF) {

            LinkShapes linkShapes = super.buildShapes(xMSF);

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




    }
    class ControlLinkOneConnector extends LinkOneConnectorBase implements ControlLink, ShapesProvider {

        public ControlLinkOneConnector(XMultiServiceFactory xmsf, String c) {
            super(xmsf, c);

        }


        @Override
        protected LinkShapes buildShapes(XMultiServiceFactory xMSF) {

            LinkShapes linkShapes = super.buildShapes(xMSF);

            try {

                Object half1 = xMSF.createInstance("com.sun.star.drawing.ConnectorShape");
                XShape xConnSh1 = QI.XShape(half1);


                xPS = QI.XPropertySet(xConnSh1);




                linkShapes.connShape1 = xConnSh1;
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
                    xPS1.setPropertyValue("LineEndName", "Arrow");
                }

                @Override
                public void applyStyleForHalf2(XPropertySet xPS2) throws UnknownPropertyException, PropertyVetoException, WrappedTargetException, IllegalArgumentException {
//                    xPS2.setPropertyValue("EdgeKind", com.sun.star.drawing.ConnectorType.CURVE);
//                    xPS2.setPropertyValue("LineEndName", "Arrow");
//                    xPS2.setPropertyValue("LineColor", new Integer(0x000000));
                }
            };
        }
    }


}