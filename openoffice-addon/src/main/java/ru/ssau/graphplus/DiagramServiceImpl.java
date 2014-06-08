/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.sun.star.beans.PropertyVetoException;
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
import ru.ssau.graphplus.link.LinkFactory;
import ru.ssau.graphplus.node.NodeBase;
import ru.ssau.graphplus.node.NodeFactory;

import java.util.Map;

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
    public DiagramServiceImpl(NodeFactory nodeFactory, LinkFactory linkFactory, DiagramModel diagramModel, DiagramController diagramController, XComponent xDrawDoc, Layout.Stage stage, Layout layout) {
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
        return nodeFactory.create(nodeType);
    }

    @Override
    public Node createNode(Node.NodeType nodeType){
        return nodeFactory.create(nodeType);
    }

    @Override
    public Link createLink(String name, Link.LinkType linkType) {
        return linkFactory.create(linkType, Settings.getSettings().isAddTextToShapeToLink() ? LinkFactory.LinkConnectors.TwoConnectorsShape : LinkFactory.LinkConnectors.OneConnectorShape);
    }

    @Override
    public void insertNode(Node node) {
        NodeBase nodeBase = (NodeBase) node;
        ShapeHelper.insertShape(nodeBase.getShape(), DrawHelper.getCurrentDrawPage(xDrawDoc));
        try {
            DrawHelper.setShapeSize(((NodeBase)node).getShape(), 1800, 1500);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }

        diagramController.insertNode(nodeBase);
        QI.XText(nodeBase.getShape()).setString(node.getId());
        QI.XNamed(nodeBase.getShape()).setName(node.getId());
        layout.layout(new DiagramElementObj(node));
    }

    @Override
    public void insertLink(Link link) {
        System.out.println("insertLink");
        ShapesProvider shapesProvider = (ShapesProvider) link;
        XDrawPage currentDrawPage = DrawHelper.getCurrentDrawPage(xDrawDoc);

        for (XShape xShape : shapesProvider.getShapes()) {
            System.out.println("insertShape");
            ShapeHelper.insertShape(xShape, currentDrawPage);
        }

        link.setProps();
        diagramController.insertLink(link);
        layout.layout(new DiagramElementObj(link));
        System.out.println("insertLink end");

    }

    @Override
    public void linkNodes(Node node1, Node node2, Link link) {


        System.out.println("linkNodes");
        System.out.println(node1.getId());
        System.out.println(node2.getId());
        link.link(node1, node2);

        diagramModel.getGraph().link(node1, node2, link);
        layoutLink(node1, node2, link);
    }
    public void layoutLink(Node node1, Node node2, Link link) {

        BetweenNodesLayout betweenNodesLayout = new BetweenNodesLayout(stage, wrapForLayout(node1), wrapForLayout(node2));
        betweenNodesLayout.layout(wrapForLayout(link));


    }

    private Layout.Obj wrapForLayout(DiagramElement diagramElement){
        return new DiagramElementObj(diagramElement);
    }

    @Override
    public void select(DiagramElement diagramElement) {
        diagramController.select(diagramElement);
    }
}
