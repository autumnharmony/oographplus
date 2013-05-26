package ru.ssau.graphplus.link;

import com.sun.star.drawing.XShape;

/**
 * User: anton
 * Date: 3/15/13
 * Time: 4:06 AM
 */
public class LinkShapes {
    private XShape textShape;
    private XShape connShape1;
    private XShape connShape2;

    public LinkShapes(XShape textShape, XShape connShape1, XShape connShape2) {
        this.textShape = textShape;
        this.connShape1 = connShape1;
        this.connShape2 = connShape2;
    }

    public LinkShapes() {
    }







    public XShape getTextShape() {
        return textShape;
    }

    public XShape getConnShape1() {
        return connShape1;
    }

    public XShape getConnShape2() {
        return connShape2;
    }

    public void setConnShape1(XShape connShape1) {
        this.connShape1 = connShape1;
    }

    public void setConnShape2(XShape connShape2) {
        this.connShape2 = connShape2;
    }

    public void setTextShape(XShape textShape) {
        this.textShape = textShape;
    }


}
