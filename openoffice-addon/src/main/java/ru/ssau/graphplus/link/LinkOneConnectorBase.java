package ru.ssau.graphplus.link;

import com.sun.star.awt.Point;
import com.sun.star.awt.Rectangle;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.PolyPolygonBezierCoords;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.*;
import com.sun.star.lang.IllegalArgumentException;
import ru.ssau.graphplus.DeferredInitializable;
import ru.ssau.graphplus.DiagramModel;
import ru.ssau.graphplus.Refreshable;
import ru.ssau.graphplus.StringSerializable;
import ru.ssau.graphplus.api.DiagramElement;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.commons.ConnectedShapes;
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.node.NodeBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 5/1/14
 * Time: 7:14 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class LinkOneConnectorBase extends LinkBase implements Link,
//        Linker,
        Validatable,
        DiagramElement,
        Refreshable<ru.ssau.graphplus.api.DiagramModel>,
        DeferredInitializable<XShape>,
        StringSerializable,
        ShapesProvider {

    @Override
    public boolean isConnected() {
        return node1 != null && node2 != null;
    }

    @Override
    public void link(Node node1, Node node2) {
        System.out.println(getClass().getSimpleName() + "link");
        System.out.println(node1.getId());
        System.out.println(node2.getId());
        setStartNode(node1);
        setEndNode(node2);

        NodeBase nodeBase1 = (NodeBase) node1;
        NodeBase nodeBase2 = (NodeBase) node2;

        try {
            System.out.println("StartShape assign to"+nodeBase1.getId());
            xPS.setPropertyValue("StartShape", nodeBase1.getShape());

            System.out.println("EndShape assign to"+nodeBase2.getId());
            xPS.setPropertyValue("EndShape", nodeBase2.getShape());
        } catch (UnknownPropertyException | WrappedTargetException | IllegalArgumentException | PropertyVetoException e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public void setPosition(Point newPosition) {

        Point position1 = connShape1.getPosition();

        int dX = newPosition.X - position1.X;
        int dY = newPosition.Y - position1.Y;

        connShape1.setPosition(newPosition);

//        Point position2 = connShape2.getPosition();
//        connShape2.setPosition(new Point(position2.X + dX, position2.Y + dY));
//
//        Point positionT = textShape.getPosition();
//        textShape.setPosition(new Point(positionT.X + dX, positionT.Y + dY));
    }

    @Override
    public Point getPosition() {
        Point position1 = connShape1.getPosition();
//        Point position2 = connShape2.getPosition();
//        Point position3 = textShape.getPosition();

//        int[] xx = {position1.X, position2.X, position3.X};
//        int[] yy = {position1.Y, position2.Y, position3.Y};

//        Arrays.sort(xx);
//        Arrays.sort(yy);

        // TODO remove redundant
        return new Point(position1.X, position1.Y);

    }


    public void refresh(ru.ssau.graphplus.api.DiagramModel diagramModel) {
        // TODO
    }

    private static final long serialVersionUID = 1L;

    public static final String LINK_STRING = "";
    protected transient XPropertySet xPS;


    protected transient XShape connShape1;
    protected transient XShape startShape;
    protected transient XShape endShape;

    protected transient XShape previousStartShape;
    protected transient XShape previousEndShape;

    private Link.LinkType linkType;

    @Override
    public Link.LinkType getType() {
//        if (linkType == null) {
//            if (this instanceof MixedLink) {
//                linkType = Link.LinkType.MixedFlow;
//            } else if (this instanceof ControlLink) {
//                linkType = Link.LinkType.ControlFlow;
//            } else if (this instanceof DataLink) {
//                linkType = Link.LinkType.DataFlow;
//            }
//        }
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

    public LinkOneConnectorBase(XMultiServiceFactory xMSF, String id) {

//        withTextShape = Settings.getSettings().isAddTextToShapeToLink();


        LinkShapes linkShapes = buildShapes(xMSF);

        shapes = new ArrayList<XShape>();
        shapes.add(linkShapes.connShape1);
//        shapes.add(linkShapes.connShape2);


//        shapes.add(linkShapes.textShape);

//        this.textShape = linkShapes.textShape;


        this.connShape1 = linkShapes.connShape1;
//        this.connShape2 = linkShapes.connShape2;


        setId(id);

    }

    @Override
    public String getName() {
        return QI.XText(connShape1).getString();
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

        return convertPoints(getPoints(xPS));

    }

    public java.awt.Point[] convertPoints(Point[] points) {
        java.awt.Point[] result = new java.awt.Point[points.length];
        for (int i = 0; i < points.length; i++) {
            result[i] = new java.awt.Point(points[i].X, points[i].Y);
        }
        return result;
    }


    @Override
    public void setProps(XShape... params) {
        if (params.length != 3) {

            throw new IllegalStateException();
        }
        connShape1 = params[0];
//        connShape2 = params[1];
//        textShape = params[2];

        xPS = QI.XPropertySet(connShape1);
//        xPS2 = QI.XPropertySet(connShape2);
//        xPStext = QI.XPropertySet(textShape);
    }

    protected LinkShapes buildShapes(XMultiServiceFactory xMSF) {

        LinkShapes linkShapes = new LinkShapes();

        return linkShapes;
    }


    class LinkApplyerImpl implements LinkApplyer {
        @Override
        public void apply(LinkStyle linkStyle, LinkBase link) {
            try {
                LinkOneConnectorBase linkBase = (LinkOneConnectorBase) link;
                linkStyle.applyStyleForHalf1(QI.XPropertySet(linkBase.getConnShape()));
//                linkStyle.applyStyleForText(QI.XPropertySet(linkBase.getTextShape()));
//                linkStyle.applyStyleForHalf2(QI.XPropertySet(linkBase.getConnShape2()));
            } catch (UnknownPropertyException | PropertyVetoException | com.sun.star.lang.IllegalArgumentException | WrappedTargetException e) {
                throw new WrappedTargetRuntimeException("Error"); // TODO wrap exception
            }
        }
    }


    private void applyStyle(LinkStyle style) {
        new LinkApplyerImpl().apply(style, this);
    }

    protected abstract LinkStyle getStyle();

    @Override
    public Rectangle getBound() {

        // TODO redundant
        Point position = connShape1.getPosition();
//        Point position1 = connShape2.getPosition();
//        Point position2 = textShape.getPosition();

        int[] xx = {position.X};//, position1.X, position2.X};
        Arrays.sort(xx);
        int[] yy = {position.Y};//, position1.Y, position2.Y};
        Arrays.sort(yy);

        int x = xx[0];
        int y = yy[0];

        XShape[] xShapes = {connShape1}; //, connShape2, textShape};

        int maxx = 0;
        int maxy = 0;

        for (XShape xShape : xShapes) {
            int xw = xShape.getPosition().X + xShape.getSize().Width;
            if (xw > maxx) {
                maxx = xw;
            }

            int yh = xShape.getPosition().Y + xShape.getSize().Height;
            if (yh > maxy) {
                maxy = yh;
            }
        }

        return new Rectangle(x, y, maxx - x, maxy - y);
    }

    public void setProps() {

        try {


            QI.XText(xPS).setString(getClass().getSimpleName());

            xPS.setPropertyValue("StartPosition", new Point(0, 200));
            xPS.setPropertyValue("EndPosition", new Point(1400, 500));
        } catch (UnknownPropertyException | PropertyVetoException | IllegalArgumentException | WrappedTargetException e) {
            throw new RuntimeException(e);
        }


        applyStyle(getStyle());


//        MiscHelper.setLinkType(connShape1, getType());

//        MiscHelper.tagShapeAsLink(connShape1);

        shapes = Arrays.asList(connShape1);


    }


    public void adjustLink(XShape sh1, XShape sh2) {
    }

    public LinkOneConnectorBase() {
    }


    //    @Override
    public XShape getConnShape() {
        return connShape1;
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
            return QI.XShape(QI.XPropertySet(connShape1).getPropertyValue(ConnectedShapes.END_SHAPE));
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
        return getStartShape(getConnShape());
    }

    private XShape connShape1EndShape() {
        return getEndShape(getConnShape());

    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id='" + id + '\'' +
                ", linkType=" + getType().toString() +
                ", node1=" + node1 != null ? node1.getName() : "null" +
                ", node2=" + node2 != null ? node2.getName() : "null" +
                '}';
    }
}
