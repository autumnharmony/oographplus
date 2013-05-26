/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus.link;

import com.sun.star.awt.Point;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.*;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.text.XText;
import com.sun.star.uno.Exception;
import ru.ssau.graphplus.*;
import ru.ssau.graphplus.node.Node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Link implements Linker, DiagramElement, Serializable, Validatable {

    private static final long serialVersionUID = 1L;

    public static final String LINK_STRING = "   ";
    protected transient XPropertySet xPS1;
    protected transient XPropertySet xPS2;
    protected transient XPropertySet xPStext;


    protected volatile transient XShape textShape;
    protected transient XShape connShape1;
    protected transient XShape connShape2;
    protected transient XShape startShape;
    protected transient XShape endShape;

    protected transient XShape previousStartShape;
    protected transient XShape previousEndShape;

    private LinkType linkType;


    public LinkType getType() {
        if (linkType == null) {
            if (this instanceof LinkLink) {
                linkType = LinkType.Link;
            } else if (this instanceof ControlLink) {
                linkType = LinkType.Control;
            } else if (this instanceof MessageLink) {
                linkType = LinkType.Message;
            }
        }
        return linkType;
    }

    protected transient Collection<XShape> shapes;
    protected String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Link(XMultiServiceFactory xMSF, XDrawPage xDP, XComponent xDrawDoc, String c) {

        LinkShapes linkShapes = buildShapes(xMSF, xDP, xDrawDoc);

        shapes = new ArrayList<XShape>();
        shapes.add(linkShapes.getConnShape1());
        shapes.add(linkShapes.getConnShape2());
        shapes.add(linkShapes.getTextShape());

        this.textShape = linkShapes.getTextShape();

        this.connShape1 = linkShapes.getConnShape1();
        this.connShape2 = linkShapes.getConnShape2();

//        Misc.setId(this.connShape1, c+"_connShape1");
//        Misc.setId(this.connShape2, c+"_connShape2");
//        Misc.setId(this.textShape, c+"_textShape");
        setId(c);

    }


    public String getName() {
        return QI.XText(textShape).getString();
    }

    private Point[] getPoints(XPropertySet xPS1) {
        Object polyPolygonBezier = null;
        try {
            polyPolygonBezier = xPS1.getPropertyValue("PolyPolygonBezier");
            PolyPolygonBezierCoords polyPolygonBezierCoords = (PolyPolygonBezierCoords) polyPolygonBezier;
            return ((PolyPolygonBezierCoords) polyPolygonBezier).Coordinates[0];
        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public java.awt.Point[] getFirstPartPoints() {

        return convertPoints(getPoints(xPS1));

    }

    public java.awt.Point[] convertPoints(Point[] points) {
        java.awt.Point[] result = new java.awt.Point[points.length];
        for (int i = 0; i < points.length; i++) {
            result[i] = new java.awt.Point(points[i].X, points[i].Y);
        }
        return result;
    }

    public java.awt.Point[] getSecondPartPoints() {
        return convertPoints(getPoints(xPS2));
    }


    protected LinkShapes buildShapes(XMultiServiceFactory xMSF, XDrawPage xDP, XComponent xDrawDoc) {

        LinkShapes linkShapes = new LinkShapes();
        try {

            Object text = xMSF.createInstance("com.sun.star.drawing.TextShape");

            XShape xTextSh = QI.XShape(text);

            linkShapes.setTextShape(xTextSh);
            textShape = xTextSh;
            xPStext = QI.XPropertySet(text);


        } catch (Exception ex) {
            Logger.getLogger(Link.class.getName()).log(Level.SEVERE, null, ex);
        }
        return linkShapes;
    }

    public void setProps() {
        try {
            xPStext.setPropertyValue("TextVerticalAdjust", TextVerticalAdjust.CENTER);
            xPStext.setPropertyValue("TextHorizontalAdjust", TextHorizontalAdjust.CENTER);
            xPStext.setPropertyValue("TextAutoGrowWidth", new Boolean(true));


            Misc.setId(connShape1, getName() + "/conn1");
            Misc.setId(connShape2, getName() + "/conn2");
            Misc.setId(textShape, getName() + "/text");

            Misc.setLinkType(connShape1, getType());
            Misc.setLinkType(connShape2, getType());
            Misc.setLinkType(textShape, getType());

            Misc.tagShapeAsLink(connShape1);
            Misc.tagShapeAsLink(connShape2);
            Misc.tagShapeAsLink(textShape);

            shapes = Arrays.asList(connShape1, connShape2, textShape);

        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        } catch (com.sun.star.lang.IllegalArgumentException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public void setProps(XShape connShape1, XShape connShape2, XShape textShape) {
        this.connShape1 = connShape1;
        this.connShape2 = connShape2;
        this.textShape = textShape;

        xPS1 = QI.XPropertySet(connShape1);
        xPS2 = QI.XPropertySet(connShape2);
        xPStext = QI.XPropertySet(textShape);

    }


    public void adjustLink(XShape sh1, XShape sh2) {
    }

    public Link() {
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

    public void setNode1(Node node1) {
        this.node1 = node1;
        startShape = node1.getShape();
    }

    public void setNode2(Node node2) {
        this.node2 = node2;
        endShape = node2.getShape();
    }

    public void link(XShape sh1, XShape sh2) {
        XText xText = QI.XText(textShape);

        // select all text and make active
    }

    public XShape getStartShape() {
        try {
            return QI.XShape(QI.XPropertySet(connShape1).getPropertyValue(ConnectedShapes.START_SHAPE));
        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static XShape getStartShapeStatic(XShape xShape) {
        try {
            return QI.XShape(QI.XPropertySet(xShape).getPropertyValue(ConnectedShapes.START_SHAPE));
        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public XShape getEndShape() {
        try {
            return QI.XShape(QI.XPropertySet(connShape2).getPropertyValue(ConnectedShapes.END_SHAPE));
        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static XShape getEndShapeStatic(XShape xShape) {
        try {
            return QI.XShape(QI.XPropertySet(xShape).getPropertyValue(ConnectedShapes.END_SHAPE));
        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isValid() {
        Node node1 = getNode1();
        XShape node1Shape = node1.getShape();


        Node node2 = getNode2();
        XShape node2Shape = node2.getShape();

        XShape startShape = getStartShape();
        XShape endShape = getEndShape();

        if (node1Shape.equals(startShape) && node2Shape.equals(endShape)) {

        } else {
            if (endShape != null) {

            }
            if (startShape != null) {

            }
            return false;
        }


        return true;
    }

    public void refresh(DiagramModel diagramModel) {
        if (!isValid()) {
            if (!getNode1().getShape().equals(getStartShape())) {
                DiagramElement diagramElement = diagramModel.getShapeToDiagramElementMap().get(getStartShape());
                if (diagramElement instanceof Node) {
                    Node node = (Node) diagramElement;
                    setNode1(node);
                } else {
                    // something wrong in model


                    DiagramElement diagramELementByShape = diagramModel.getDiagramELementByShape(connShape1StartShape());
                    DiagramElement diagramELementByShape1 = diagramModel.getDiagramELementByShape(connShape1EndShape());
                    Node node = null;
                    if (diagramELementByShape instanceof Node) {
                        node = (Node) diagramELementByShape;
                    } else if (diagramELementByShape1 instanceof Node) {
                        node = (Node) diagramELementByShape1;
                    }
                    if (node != null) {
                        setNode1(node);
                    } else {
                        Logger.getAnonymousLogger().warning("node1 of link [+" + getName() + "] is null");
                    }
                }
            }

            if (!getNode2().getShape().equals(getEndShape())) {
                DiagramElement diagramElement = diagramModel.getShapeToDiagramElementMap().get(getEndShape());
                if (diagramElement instanceof Node) {
                    Node node = (Node) diagramElement;
                    setNode2(node);
                } else {
                    // something wrong in model


                    DiagramElement diagramELementByShape = diagramModel.getDiagramELementByShape(connShape1StartShape());
                    DiagramElement diagramELementByShape1 = diagramModel.getDiagramELementByShape(connShape1EndShape());
                    Node node = null;
                    if (diagramELementByShape instanceof Node) {
                        node = (Node) diagramELementByShape;
                    } else if (diagramELementByShape1 instanceof Node) {
                        node = (Node) diagramELementByShape1;
                    }
                    if (node != null) {
                        setNode2(node);
                    } else {
                        Logger.getAnonymousLogger().warning("node2 of link [+" + getName() + "] is null");
                    }
                }
            }
        }
    }

    private Collection<XShape> allShapes() {
        return Arrays.asList(connShape1StartShape(), connShape1EndShape(), connShape2StartShape(), connShape2EndShape());
    }


    private XShape getStartEndShape(XShape xShape, String startEnd) {
        try {
            return QI.XShape(QI.XPropertySet(xShape).getPropertyValue(startEnd));
        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private XShape getStartShape(XShape xShape) {
        return getStartEndShape(xShape, "StartShape");
    }

    private XShape getEndShape(XShape xShape) {
        return getStartEndShape(xShape, "EndShape");
    }

    private XShape connShape1StartShape() {
        return getStartShape(getConnShape1());
    }

    private XShape connShape2StartShape() {

        return getStartShape(getConnShape2());

    }

    private XShape connShape1EndShape() {
        return getEndShape(getConnShape1());

    }

    private XShape connShape2EndShape() {
        return getEndShape(getConnShape2());
    }


}
