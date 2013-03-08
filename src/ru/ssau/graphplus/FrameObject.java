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

    private XFrame      m_xFrame        = null;
    private DiagramController m_Diagram_Controller = null;

    FrameObject(XFrame xFrame, DiagramController diagramController){
        m_xFrame = xFrame;
        m_Diagram_Controller = diagramController;
    }

    public XFrame getXFrame(){
        return m_xFrame;
    }

    public void setXFrame(XFrame xFrame){
        m_xFrame = xFrame ;
    }

    public DiagramController getController(){
        return m_Diagram_Controller;
    }

    public void setController(DiagramController diagramController){
        m_Diagram_Controller = diagramController;
    }

}
