/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import com.google.inject.Inject;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.*;
import ru.ssau.graphplus.api.DiagramElement;
import ru.ssau.graphplus.api.DiagramService;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.commons.ShapeHelper;
import ru.ssau.graphplus.gui.BetweenNodesLayout;
import ru.ssau.graphplus.gui.DiagramElementObj;
import ru.ssau.graphplus.gui.Layout;
import ru.ssau.graphplus.link.LinkBase;
import ru.ssau.graphplus.link.LinkFactory;
import ru.ssau.graphplus.node.NodeBase;
import ru.ssau.graphplus.node.NodeFactory;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 4/14/14
 * Time: 4:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class DiagramServiceImpl implements DiagramService {


    private final Layout.Stage stage;

    @Inject
    public DiagramServiceImpl(NodeFactory nodeFactory, LinkFactory linkFactory, DiagramModel diagramModel, DiagramController diagramController, XComponent xDrawDoc,Layout.Stage stage, Layout layout) {
        this.nodeFactory = nodeFactory;
        this.linkFactory = linkFactory;
        this.diagramModel = diagramModel;
        this.xDrawDoc = xDrawDoc;
        this.diagramController = diagramController;
        this.layout = layout;
        this.stage = stage;

    }

    private DiagramModel diagramModel;
    private DiagramController diagramController;
    private XComponent xDrawDoc;

    private final Layout layout;
    private NodeFactory nodeFactory;
    private LinkFactory linkFactory;

    @Override
    public void addDiagramElement(DiagramElement diagramElement) {

    }

    @Override
    public void removeDiagramElement(DiagramElement diagramElement) {

    }

    @Override
    public Node createNode(String name, Node.NodeType nodeType) {
        return nodeFactory.create(nodeType, xDrawDoc);
    }

    @Override
    public Link createLink(String name, Link.LinkType linkType) {
        return linkFactory.create(linkType, xDrawDoc);
    }

    @Override
    public void insertNode(Node node) {
        NodeBase nodeBase = (NodeBase) node;
        ShapeHelper.insertShape(nodeBase.getShape(), DrawHelper.getCurrentDrawPage(xDrawDoc));


        layout.layout(new DiagramElementObj(node));
    }

    @Override
    public void insertLink(Link link) {
        LinkBase linkBase = (LinkBase) link;
        XDrawPage currentDrawPage = DrawHelper.getCurrentDrawPage(xDrawDoc);

        for (XShape xShape : linkBase.getShapes()) {

            ShapeHelper.insertShape(xShape, currentDrawPage);
        }

        ((LinkBase) link).setProps();
        diagramController.insertLink(link);
        layout.layout(new DiagramElementObj(link));

    }

    @Override
    public void linkNodes(Node node1, Node node2, Link link) {

        LinkBase linkBase = (LinkBase) link;
        new BetweenNodesLayout(stage, new DiagramElementObj(node1), new DiagramElementObj(node2)).layout(new DiagramElementObj(link));
        XPropertySet connector1 = QI.XPropertySet(linkBase.getConnShape1());
        XPropertySet connector2 = QI.XPropertySet(linkBase.getConnShape2());
        try {
            NodeBase node11 = (NodeBase) node1;
            XShape shape = node11.getShape();
            connector1.setPropertyValue("StartShape", shape);
            NodeBase node21 = (NodeBase) node2;
            XShape shape1 = node21.getShape();
            connector2.setPropertyValue("EndShape", shape1);
        } catch (UnknownPropertyException | PropertyVetoException | com.sun.star.lang.IllegalArgumentException | WrappedTargetException | ClassCastException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }






    }
}
