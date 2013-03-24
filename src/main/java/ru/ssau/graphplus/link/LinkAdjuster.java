/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus.link;

import com.sun.star.awt.Point;
import ru.ssau.graphplus.QI;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author anton
 */
public class LinkAdjuster {

    public static void adjustLink(Link link) {
        XShape start = link.startShape;
        XShape end = link.endShape;
        XShape conn1 = link.getConnShape1();
        XShape conn2 = link.getConnShape2();
        int dx = Math.abs(start.getPosition().X - end.getPosition().X);
        int dy = Math.abs(start.getPosition().Y - end.getPosition().Y);

        // GluePointIndex  
        // Index value of 0 : the shape is connected at the top
        // Index value of 1 : the shape is connected at the left
        // Index value of 2 : the shape is connected at the bottom
        // Index value of 3 : the shape is connected at the right
        if (dx <= dy) {
            try {
                // top or bottom
                XShape top;
                XShape bottom;
                if (conn1.getPosition().Y < conn2.getPosition().Y){
                    top = conn1;
                    bottom = conn2;
                } else {
                    top = conn2;
                    bottom = conn1;
                }
                QI.XPropertySet(bottom).setPropertyValue("EndGluePointIndex", 0);
                QI.XPropertySet(top).setPropertyValue("StartGluePointIndex", 2);
            } catch (UnknownPropertyException ex) {
                Logger.getLogger(LinkAdjuster.class.getName()).log(Level.SEVERE, null, ex);
            } catch (PropertyVetoException ex) {
                Logger.getLogger(LinkAdjuster.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(LinkAdjuster.class.getName()).log(Level.SEVERE, null, ex);
            } catch (WrappedTargetException ex) {
                Logger.getLogger(LinkAdjuster.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                //dx > dy
                XShape left;
                XShape right;
                if (conn1.getPosition().X <= conn1.getPosition().X) {
                    left = conn1;
                    right = conn2;
                } else {
                    left = conn2;
                    right = conn1;
                }
                QI.XPropertySet(right).setPropertyValue("EndGluePointIndex", 3);
                QI.XPropertySet(left).setPropertyValue("StartGluePointIndex", 1);
            } catch (UnknownPropertyException ex) {
                Logger.getLogger(LinkAdjuster.class.getName()).log(Level.SEVERE, null, ex);
            } catch (PropertyVetoException ex) {
                Logger.getLogger(LinkAdjuster.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(LinkAdjuster.class.getName()).log(Level.SEVERE, null, ex);
            } catch (WrappedTargetException ex) {
                Logger.getLogger(LinkAdjuster.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public static void placeText(Link link){

        Point pos1 = link.getConnShape1().getPosition();
        Point pos2 = link.getConnShape2().getPosition();
        Point resultPosition = new Point((pos1.X + pos2.X) / 2 , (pos1.Y + pos2.Y) /2);

        link.getTextShape().setPosition(resultPosition);

    }
}
