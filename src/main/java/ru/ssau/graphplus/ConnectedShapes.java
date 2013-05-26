package ru.ssau.graphplus;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.UnoRuntime;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectedShapes {

    public static final String START_SHAPE = "StartShape";
    public static final String END_SHAPE = "EndShape";
    private XPropertySet mxConnector;
    private XShape mxStart;
    private XShape mxEnd;

    public ConnectedShapes(XConnectorShape xShape) {
        System.out.println("ConnectedShapes");
        try {
            mxConnector = UnoRuntime.queryInterface(XPropertySet.class, xShape);
            mxStart = UnoRuntime.queryInterface(XShape.class, mxConnector.getPropertyValue(START_SHAPE));
            mxEnd = UnoRuntime.queryInterface(XShape.class, mxConnector.getPropertyValue(END_SHAPE));
        } catch (UnknownPropertyException ex) {
            Logger.getLogger(ConnectorShapeListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(ConnectorShapeListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    public ConnectedShapes(XPropertySet mxConnector, XShape mxStart, XShape mxEnd) {
//        this.mxConnector = mxConnector;
//        this.mxStart = mxStart;
//        this.mxEnd = mxEnd;
//    }

    public XPropertySet getConnector() {
        return mxConnector;
    }

    public XShape getEnd() {
        return mxEnd;
    }

    public XShape getStart() {
        return mxStart;
    }

    private void setEnd(XShape xEnd) {
        mxEnd = xEnd;
    }

    private void setStart(XShape xStart) {
        mxStart = xStart;
    }

    public boolean hasChanged() {
        try {
            XShape xStart = UnoRuntime.queryInterface(XShape.class, mxConnector.getPropertyValue("StartShape"));
            XShape xEnd = UnoRuntime.queryInterface(XShape.class, mxConnector.getPropertyValue("EndShape"));

            return xStart != mxStart || xEnd != mxEnd;
        } catch (UnknownPropertyException ex) {
            Logger.getLogger(ConnectorShapeListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(ConnectorShapeListener.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    public boolean update() {
        try {
            mxStart = UnoRuntime.queryInterface(XShape.class, mxConnector.getPropertyValue("StartShape"));
            mxEnd = UnoRuntime.queryInterface(XShape.class, mxConnector.getPropertyValue("EndShape"));
            return true;
        } catch (UnknownPropertyException ex) {
            Logger.getLogger(ConnectorShapeListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(ConnectorShapeListener.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }
}
