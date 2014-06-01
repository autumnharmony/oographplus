
package ru.ssau.graphplus;

import com.sun.star.drawing.XShape;
import com.sun.star.lang.XMultiServiceFactory;

/**
 * @author anton
 */
public interface ShapeBuilder {

XShape buildShape(XMultiServiceFactory xMSF);

}
