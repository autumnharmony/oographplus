/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus.link;

import ru.ssau.graphplus.QI;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.EventObject;
import com.sun.star.view.XSelectionChangeListener;

/**
 *
 * @author anton
 */
public interface Linker {

    void link(XShape sh1, XShape sh2);
    void adjustLink(XShape sh1, XShape sh2);
    XShape getTextShape();
}
