package ru.ssau.graphplus.commons;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.WrappedTargetException;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Class for storing two connected by ConnectorShape shapes and connector shape itself
 * Main purpose is for storing temp data which we use for generating
 * @see ConnectedShapesComplex
 */
public class ConnectedShapes {

    public static final String START_SHAPE = "StartShape";
    public static final String END_SHAPE = "EndShape";
    public static final String TEXT_SHAPE = "TextShape";

    private XConnectorShape connectorShape;

    private UnoRuntimeWrapper unoRuntimeWrapper;

//    private XPropertySet xPropertySet;

    private XShape xStart;
    private XShape xEnd;

//    @Inject
//    public static UnoRuntimeWrapper unoRuntimeWrapper;

    public ConnectedShapes(XConnectorShape connectorShape, UnoRuntimeWrapper unoRuntimeWrapper, ShapeHelperWrapper shapeHelperWrapper) {
        try {

            this.unoRuntimeWrapper = unoRuntimeWrapper;
            this.connectorShape = connectorShape;

//            xPropertySet = unoRuntimeWrapper.queryInterface(XPropertySet.class, connectorShape);

            xStart = unoRuntimeWrapper.queryInterface(XShape.class, QI.XPropertySet(connectorShape).getPropertyValue(ConnectedShapes.START_SHAPE));
            xEnd = unoRuntimeWrapper.queryInterface(XShape.class, QI.XPropertySet(connectorShape).getPropertyValue(ConnectedShapes.END_SHAPE));

            if (shapeHelperWrapper.isTextShape(xEnd)) {

            }

            if (shapeHelperWrapper.isTextShape(xStart)) {

            }

        } catch (UnknownPropertyException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }


    public XPropertySet getConnectorPropertySet() {
        return QI.XPropertySet(connectorShape);
    }

    public XShape getEnd() {
        return xEnd;
    }

    public XShape getStart() {
        return xStart;
    }

    private void setEnd(XShape xEnd) {
        this.xEnd = xEnd;
    }

    private void setStart(XShape xStart) {
        this.xStart = xStart;
    }

    public boolean hasChanged() {
        try {
            XShape xStart = QI.XShape(QI.XPropertySet(connectorShape).getPropertyValue("StartShape"));
            XShape xEnd = QI.XShape(QI.XPropertySet(connectorShape).getPropertyValue("EndShape"));

            return xStart != this.xStart || xEnd != this.xEnd;
        } catch (UnknownPropertyException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    public boolean update() {
        try {
            xStart = unoRuntimeWrapper.queryInterface(XShape.class, QI.XPropertySet(connectorShape).getPropertyValue("StartShape"));
            xEnd = unoRuntimeWrapper.queryInterface(XShape.class, QI.XPropertySet(connectorShape).getPropertyValue("EndShape"));
            return true;
        } catch (UnknownPropertyException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }
}
