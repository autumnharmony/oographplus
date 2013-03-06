/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus;

import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import java.util.Collection;

/**
 *
 * @author anton
 */
public interface ShapeBuilder {
    
    XShape buildShape(XMultiServiceFactory xMSF);
    
    XShape buildShape(XMultiServiceFactory xMSF, XDrawPage xDP, XComponent xDrawDoc);
    
    
    // for complex links
    Collection<XShape> buildShapes(XMultiServiceFactory xMSF, XDrawPage xDP, XComponent xDrawDoc);
    
}
