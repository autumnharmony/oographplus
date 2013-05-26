/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus;

/**
 *
 * @author 1
 */

import com.sun.star.frame.XFrame;

// store pairs of XFrame and DiagramController
public class FrameObject {


    private XFrame xFrame = null;
    private DiagramController diagramController = null;
    private DiagramModel diagramModel = null;

    public FrameObject(XFrame xFrame, DiagramController diagramController, DiagramModel diagramModel) {
        this.xFrame = xFrame;
        this.diagramController = diagramController;
        this.diagramModel = diagramModel;

    }

    public XFrame getXFrame() {
        return xFrame;
    }

    public void setXFrame(XFrame xFrame) {
        this.xFrame = xFrame;
    }

    public DiagramController getController() {
        return diagramController;
    }

    public void setController(DiagramController diagramController) {
        this.diagramController = diagramController;
    }

}
