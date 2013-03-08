/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus.link;

import ru.ssau.graphplus.DiagramElement;
import ru.ssau.graphplus.ShapeBuilder;
import ru.ssau.graphplus.node.Node;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.TextHorizontalAdjust;
import com.sun.star.drawing.TextVerticalAdjust;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.ssau.graphplus.QI;

/**
 *
 * @author 1
 */
public abstract class Link implements Linker, DiagramElement {

    public static final String LINK_STRING = "   ";
    protected XPropertySet xPS1;
    protected XPropertySet xPS2;
    protected XPropertySet xPStext;

    private LinkController linkController;

    public Link(XMultiServiceFactory xMSF, XDrawPage xDP, XComponent xDrawDoc) {
        shapes = buildShapes(xMSF, xDP, xDrawDoc);

    }




    protected Collection<XShape> buildShapes(XMultiServiceFactory xMSF, XDrawPage xDP, XComponent xDrawDoc) {
        try {
            Object text = xMSF.createInstance("com.sun.star.drawing.TextShape");

            XShape xTextSh = QI.XShape(text);
            textShape = xTextSh;
            xPStext = QI.XPropertySet(text);
            xPStext.setPropertyValue("TextVerticalAdjust", TextVerticalAdjust.CENTER);
            xPStext.setPropertyValue("TextHorizontalAdjust", TextHorizontalAdjust.CENTER);
            xPStext.setPropertyValue("TextAutoGrowWidth", new Boolean(true));

            XText xText = (XText) UnoRuntime.queryInterface(XText.class, xTextSh);
        } catch (Exception ex) {
            Logger.getLogger(Link.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }






    public void adjustLink(XShape sh1, XShape sh2) {
    }

    public Link() {
    }
    Collection<XShape> shapes;




    protected XShape textShape;
    protected XShape connShape1;
    protected XShape connShape2;
    protected XShape startShape;
    protected XShape endShape;

    public Link(XShape textShape, XShape connShape1, XShape connShape2, XShape start, XShape end) {
        this.textShape = textShape;
        this.connShape1 = connShape1;
        this.connShape2 = connShape2;
//        this.node1 = node1;
//        this.node2 = node2;
        startShape = start;
        endShape = end;
        shapes = new ArrayList<XShape>();
        shapes.add(textShape);
        shapes.add(connShape1);
        shapes.add(connShape2);
//        shapes.add(textShape);

    }

    public XShape getConnShape1() {
        return connShape1;
    }

    public XShape getConnShape2() {
        return connShape2;
    }

    public XShape getTextShape() {
        return textShape;
    }

    public Iterable<XShape> getShapes() {
        return shapes;
    }

    public enum LinkType {

        Link,
        Message,
        Control
    }
    Node node1, node2;

    public Node getNode1() {
        return node1;
    }

    public Node getNode2() {
        return node2;
    }

    public void link(XShape sh1, XShape sh2) {
        XText xText = QI.XText(textShape);
        xText.setString(LINK_STRING);


        // select all text and make active
    }
}
