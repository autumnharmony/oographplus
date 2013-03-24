package ru.ssau.graphplus.link;

import com.sun.star.drawing.XShape;
import com.sun.star.drawing.XShapeAligner;

/**
 * User: anton
 * Date: 3/15/13
 * Time: 4:06 AM
 */
public class LinkShapes {
    private XShape textShape;
    private XShape connShape1;
    private XShape connShape2;
    private LinkShapes.LinkShapePart linkShapePart;

    public LinkShapes(XShape textShape, XShape connShape1, XShape connShape2) {
        this.textShape = textShape;
        this.connShape1 = connShape1;
        this.connShape2 = connShape2;
    }

    public LinkShapes() {
    }

    public LinkShapePart build() {
        if (linkShapePart == null){
        linkShapePart = new LinkShapePart();
        }

        return linkShapePart;
    }

    public LinkShapes complete() throws Exception {
        if (linkShapePart != null){
          if (linkShapePart.linkShapes.connShape1 != null && linkShapePart.linkShapes.connShape2 != null
                  && linkShapePart.linkShapes.connShape2 !=null){
              return linkShapePart.linkShapes;
          }
        }
        throw new Exception("not completed");

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

    class LinkShapePart{

        LinkShapes linkShapes = new LinkShapes();

        public LinkShapePart textShape(XShape xShape){
            linkShapes.textShape = xShape;
            return this;
        }
        public LinkShapePart connShape1(XShape xShape){
            linkShapes.connShape1 = xShape;
            return this;
        }

        public LinkShapePart connShape2(XShape xShape){
             linkShapes.connShape2 = xShape;
            return this;
        }
    }


}
