
package ru.ssau.graphplus.link;

import com.sun.star.awt.Point;
import com.sun.star.awt.Rectangle;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.PolyPolygonBezierCoords;
import com.sun.star.drawing.TextHorizontalAdjust;
import com.sun.star.drawing.TextVerticalAdjust;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.*;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.text.XText;
import com.sun.star.uno.Exception;
import ru.ssau.graphplus.*;
import ru.ssau.graphplus.DiagramModel;
import ru.ssau.graphplus.api.*;
import ru.ssau.graphplus.api.DiagramElement;
import ru.ssau.graphplus.node.NodeBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class LinkBase implements Link,
        Linker,
        Validatable,
        DiagramElement,
        Refreshable<ru.ssau.graphplus.api.DiagramModel>,
        DeferredInitializable<XShape>,
        StringSerializable,
        ShapesProvider
{

    @Override
    public void setPosition(Point position) {
        // TODO implement

    }

    @Override
    public Point getPosition() {
        Point position1 = connShape1.getPosition();
        Point position2 = connShape2.getPosition();
        Point position3 = textShape.getPosition();

        int[] xx = {position1.X, position2.X, position3.X};
        int[] yy = {position1.Y, position2.Y, position3.Y};

        Arrays.sort(xx);
        Arrays.sort(yy);
        return new Point(xx[0], yy[0]);

    }

    //    @Override
    public void setStartNode(Node node1) {
        if (node1 instanceof NodeBase){
            setStartNode(node1);
        }
    }

//    @Override
    public void setEndNode(Node node2) {
        if (node2 instanceof NodeBase){
            setEndNode(node2);
        }
    }

//    @Override
    public void refresh(ru.ssau.graphplus.api.DiagramModel diagramModel) {
        // TODO
    }

    private static final long serialVersionUID = 1L;

    public static final String LINK_STRING = "";
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

    @Override
    public LinkType getType() {
        if (linkType == null) {
            if (this instanceof MixedLink) {
                linkType = LinkType.MixedFlow;
            } else if (this instanceof ControlLink) {
                linkType = LinkType.ControlFlow;
            } else if (this instanceof DataLink) {
                linkType = LinkType.DataFlow;
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

    public LinkBase(XMultiServiceFactory xMSF, XComponent xDrawDoc, String c) {

        LinkShapes linkShapes = buildShapes(xMSF, xDrawDoc);

        shapes = new ArrayList<XShape>();
        shapes.add(linkShapes.connShape1);
        shapes.add(linkShapes.connShape2);
        shapes.add(linkShapes.textShape);

        this.textShape = linkShapes.textShape;


        this.connShape1 = linkShapes.connShape1;
        this.connShape2 = linkShapes.connShape2;


        setId(c);

    }

    @Override
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

    @Override
    public void setProps(XShape... params) {
        if (params.length != 3) {

            throw new IllegalStateException();
        }
        connShape1 = params[0];
        connShape2 = params[1];
        textShape = params[2];

        xPS1 = QI.XPropertySet(connShape1);
        xPS2 = QI.XPropertySet(connShape2);
        xPStext = QI.XPropertySet(textShape);
    }

    protected LinkShapes buildShapes(XMultiServiceFactory xMSF, XComponent xDrawDoc) {

        LinkShapes linkShapes = new LinkShapes();
        try {

            Object text = xMSF.createInstance("com.sun.star.drawing.TextShape");
            XShape xTextSh = QI.XShape(text);

            linkShapes.textShape = xTextSh;
            textShape = xTextSh;
            xPStext = QI.XPropertySet(text);

        } catch (Exception ex) {
            Logger.getLogger(LinkBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return linkShapes;
    }


    interface LinkStyle {
        void applyStyleForHalf1(XPropertySet xPropertySet) throws UnknownPropertyException, PropertyVetoException, WrappedTargetException, IllegalArgumentException;
        void applyStyleForHalf2(XPropertySet xPropertySet) throws UnknownPropertyException, PropertyVetoException, WrappedTargetException, IllegalArgumentException;
        void applyStyleForText(XPropertySet xPropertySet) throws UnknownPropertyException, PropertyVetoException, WrappedTargetException, IllegalArgumentException;
    }

    abstract class LinkStyleBase implements LinkStyle {
        @Override
        public void applyStyleForText(XPropertySet xPStext) throws UnknownPropertyException, PropertyVetoException, WrappedTargetException, IllegalArgumentException {

                xPStext.setPropertyValue("TextVerticalAdjust", TextVerticalAdjust.CENTER);
                xPStext.setPropertyValue("TextHorizontalAdjust", TextHorizontalAdjust.CENTER);
                xPStext.setPropertyValue("TextAutoGrowWidth", new Boolean(true));

        }
    }

    interface LinkApplyer {
        void apply(LinkStyle linkStyle, LinkBase linkBase);
    }

    class LinkApplyerImpl implements LinkApplyer {
        @Override
        public void apply(LinkStyle linkStyle, LinkBase linkBase) {
         try {
                    linkStyle.applyStyleForHalf1( QI.XPropertySet(linkBase.getConnShape1()));
                    linkStyle.applyStyleForText(QI.XPropertySet(linkBase.getTextShape()));
                    linkStyle.applyStyleForHalf2(QI.XPropertySet(linkBase.getConnShape2()));
            } catch (UnknownPropertyException | PropertyVetoException | com.sun.star.lang.IllegalArgumentException|WrappedTargetException e) {
                throw new WrappedTargetRuntimeException("Error"); // TODO wrap exception
            }
        }
    }

    private void applyStyle(LinkStyle style){
        new LinkApplyerImpl().apply(style, this);
    }

    protected abstract LinkStyle getStyle();

    @Override
    public Rectangle getBound() {
        Point position = connShape1.getPosition();
        Point position1 = connShape2.getPosition();
        Point position2 = textShape.getPosition();

        int[] xx = {position.X, position1.X, position2.X};
        Arrays.sort(xx);
        int[] yy = {position.Y, position1.Y, position2.Y};
        Arrays.sort(yy);

        int x = xx[0];
        int y = yy[0];

        XShape[] xShapes = {connShape1, connShape2, textShape};

        int maxx = 0;
        int maxy = 0;

        for (XShape xShape : xShapes){
            int xw = xShape.getPosition().X + xShape.getSize().Width;
            if (xw > maxx) {
                maxx = xw;
            }

            int yh = xShape.getPosition().Y + xShape.getSize().Height;
            if (yh > maxy) {
                maxy = yh;
            }
        }

        return new Rectangle(x,y, maxx - x, maxy - y);
    }

    public void setProps() {

        try {
            xPS1.setPropertyValue("EndShape", getTextShape());
            xPS2.setPropertyValue("StartShape", getTextShape());
        }
        catch (UnknownPropertyException | PropertyVetoException | IllegalArgumentException |WrappedTargetException e)
        {
            throw new RuntimeException(e);
        }


        applyStyle(getStyle());

//            MiscHelper.setId(connShape1, getName() + "/conn1");
//            MiscHelper.setId(connShape2, getName() + "/conn2");
//            MiscHelper.setId(textShape, getName() + "/text");

            MiscHelper.setLinkType(connShape1, getType());
            MiscHelper.setLinkType(connShape2, getType());
            MiscHelper.setLinkType(textShape, getType());

            MiscHelper.tagShapeAsLink(connShape1);
            MiscHelper.tagShapeAsLink(connShape2);
            MiscHelper.tagShapeAsLink(textShape);

            shapes = Arrays.asList(connShape1, connShape2, textShape);



    }


    public void adjustLink(XShape sh1, XShape sh2) {
    }

    public LinkBase() {
    }


//    @Override
    public XShape getConnShape1() {
        return connShape1;
    }

//    @Override
    public XShape getConnShape2() {
        return connShape2;
    }

//    @Override
    public XShape getTextShape() {
        return textShape;
    }

//    @Override
    public Iterable<XShape> getShapes() {
        return shapes;
    }


    NodeBase node1, node2;

    public Node getStartNode() {
        return node1;
    }

    public Node getEndNode() {
        return node2;
    }

    public void setStartNode(NodeBase node1) {
        this.node1 = node1;
        startShape = node1.getShape();
    }

    public void setEndNode(NodeBase node2) {
        this.node2 = node2;
        endShape = node2.getShape();
    }

    public void link(XShape sh1, XShape sh2) {
        XText xText = QI.XText(textShape);

        // select all text and make active
    }

//    @Override
    public XShape getStartNodeShape() {
        try {
            return QI.XShape(QI.XPropertySet(connShape1).getPropertyValue(ConnectedShapes.START_SHAPE));
        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

//    @Override
    public XShape getEndNodeShape() {
        try {
            return QI.XShape(QI.XPropertySet(connShape2).getPropertyValue(ConnectedShapes.END_SHAPE));
        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isValid() {
        NodeBase node1 = (NodeBase) getStartNode();
        XShape node1Shape = node1.getShape();


        NodeBase node2 = (NodeBase) getEndNode();
        XShape node2Shape = node2.getShape();

        XShape startShape = getStartNodeShape();
        XShape endShape = getEndNodeShape();

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
            Node startNode_ = getStartNode();
            NodeBase startNode = (NodeBase) startNode_;
            if (!startNode.getShape().equals(getStartNodeShape())) {
                DiagramElement diagramElement = diagramModel.getShapeToDiagramElementMap().get(getStartNodeShape());
                if (diagramElement instanceof NodeBase) {
                    NodeBase node = (NodeBase) diagramElement;
                    setStartNode(node);
                } else {
                    // something wrong in model


                    DiagramElement diagramELementByShape = diagramModel.getDiagramElementByShape(connShape1StartShape());
                    DiagramElement diagramELementByShape1 = diagramModel.getDiagramElementByShape(connShape1EndShape());
                    NodeBase node = null;
                    if (diagramELementByShape instanceof NodeBase) {
                        node = (NodeBase) diagramELementByShape;
                    } else if (diagramELementByShape1 instanceof NodeBase) {
                        node = (NodeBase) diagramELementByShape1;
                    }
                    if (node != null) {
                        setStartNode(node);
                    } else {
                        Logger.getAnonymousLogger().warning("node1 of link [+" + getName() + "] is null");
                    }
                }
            }

            Node endNode_ = getEndNode();

            NodeBase endNode = (NodeBase) endNode_;
            if (!endNode.getShape().equals(getEndNodeShape())) {
                DiagramElement diagramElement = diagramModel.getShapeToDiagramElementMap().get(getEndNodeShape());
                if (diagramElement instanceof NodeBase) {
                    NodeBase node = (NodeBase) diagramElement;
                    setEndNode(node);
                } else {
                    // something wrong in model


                    DiagramElement diagramELementByShape = diagramModel.getDiagramElementByShape(connShape1StartShape());
                    DiagramElement diagramELementByShape1 = diagramModel.getDiagramElementByShape(connShape1EndShape());
                    NodeBase node = null;
                    if (diagramELementByShape instanceof NodeBase) {
                        node = (NodeBase) diagramELementByShape;
                    } else if (diagramELementByShape1 instanceof NodeBase) {
                        node = (NodeBase) diagramELementByShape1;
                    }
                    if (node != null) {
                        setEndNode(node);
                    } else {
                        Logger.getAnonymousLogger().warning("node2 of link [+" + getName() + "] is null");
                    }
                }
            }
        }
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

    @Override
    public String toString() {
        return getClass().getSimpleName() +"{" +
                "id='" + id + '\'' +
                ", linkType=" + getType().toString() +
                ", node1=" + node1 != null ? node1.getName() : "null" +
                ", node2=" + node2 != null ? node2.getName() : "null" +
                '}';
    }
}
