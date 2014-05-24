/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.api.DiagramType;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.api.Graph;
import ru.ssau.graphplus.codegen.impl.analizer.Walker;
import ru.ssau.graphplus.codegen.impl.recognition.DiagramTypeRecognitionImpl;
import ru.ssau.graphplus.commons.*;
import ru.ssau.graphplus.link.LinkFactory;
import ru.ssau.graphplus.node.NodeFactory;

import java.util.*;


public class DiagramWalker implements Walker<Set<XShape>, Graph> {


    private final UnoRuntimeWrapper unoRuntimeWrapper;
    private /*final*/ DiagramType diagramType;
    Set<XShape> visited;
    ShapeHelperWrapper shapeHelperWrapper;
    private final LinkFactory linkFactory;
    private final NodeFactory nodeFactory;
    private Table<Node,Node,List<Link>> graph;
    private Table<XShape,XShape,ConnectedShapesComplex> fromTo;
    private Collection<ConnectedShapesComplex> connectedShapesComplexes;


    @Inject
    public DiagramWalker(ShapeHelperWrapper shapeHelperWrapper, UnoRuntimeWrapper unoRuntimeWrapper, LinkFactory linkFactory, NodeFactory nodeFactory) {

        this.shapeHelperWrapper = shapeHelperWrapper;
        this.unoRuntimeWrapper = unoRuntimeWrapper;
        this.linkFactory = linkFactory;
        this.nodeFactory = nodeFactory;
//        Injector injector = Guice.createInjector(Modules.combine(new CommonsModule(),new CodeGeneratorModule()));
    }

    public DiagramType getDiagramType() {
        return diagramType;
    }

    public void setDiagramType(DiagramType diagramType) {
        this.diagramType = diagramType;
    }

    public Graph walk(Set<XShape> all) {

        visited = Sets.newHashSet();
        XShape start = null;
        Iterator<XShape> iterator = all.iterator();
        if (start == null || ShapeHelper.isConnectorShape(start)) {
            start = getStart(all);
        } else {

        }


        Queue<XShape> shapeQueue = new LinkedList<>();
        shapeQueue.add(start);


        /*
             | from1 | from 2 |
         ----+-------+--------+
         to1 |       |        |
         ----+-------+--------+
         to2 |       |        |
         ----+----------------+

         */

        fromTo = HashBasedTable.create();


        for (XShape shape : all) {
            if (shapeHelperWrapper.isConnectorShape(shape)) {

                XConnectorShape connectorShape = QI.XConnectorShape(shape);
                if (!shapeHelperWrapper.isConnected(connectorShape)) continue;
                ConnectedShapes connectedShapes = new ConnectedShapes(connectorShape, unoRuntimeWrapper, shapeHelperWrapper);

                XShape start_ = connectedShapes.getStart();
                XShape end_ = connectedShapes.getEnd();

                if (start_ == null || end_ == null) {
                    continue;
                }
                if (!shapeHelperWrapper.isTextShape(start_) && shapeHelperWrapper.isTextShape(end_)) {

                    // shape -> text
                    // first part of complex link

                    // start_   is  shape
                    // end_     is  text


                    boolean already = fromTo.row(end_) != null && fromTo.row(end_).size() == 1;
                    if (already) {

                        ConnectedShapesComplex secondPart = fromTo.row(end_).get(fromTo.row(end_).keySet().iterator().next());

                        // second part

                        // secondPart.fromShape is  text
                        // secondPart.toShape   is  shape

                        fromTo.put(start_, secondPart.toShape, new ConnectedShapesComplex(start_, secondPart.toShape, connectorShape, secondPart.connector, end_));
                        fromTo.remove(secondPart.fromShape, secondPart.toShape);
                        visited.add(secondPart.fromShape);
                    } else {
                        fromTo.put(start_, end_, new ConnectedShapesComplex(start_, end_, QI.XConnectorShape(connectorShape)));
                    }
                }


                if (shapeHelperWrapper.isTextShape(start_) && !shapeHelperWrapper.isTextShape(end_)) {
                    // text -> non text
                    // second part of complex link
                    // start_ is text
                    // end_ is shape

                    boolean already = fromTo.column(start_) != null && fromTo.column(start_).size() == 1;

                    if (already) {
                        ConnectedShapesComplex firstPart = fromTo.column(start_).get(fromTo.column(start_).keySet().iterator().next());

                        fromTo.put(firstPart.fromShape, end_, new ConnectedShapesComplex(firstPart.fromShape, end_, firstPart.connector, connectorShape, start_));
                        fromTo.remove(firstPart.fromShape, firstPart.toShape);
                        visited.add(start_);
                    } else {
                        fromTo.put(start_, end_, new ConnectedShapesComplex(start_, end_, QI.XConnectorShape(connectorShape)));
                    }
                }

                if (!shapeHelperWrapper.isTextShape(start_) && !shapeHelperWrapper.isTextShape(end_)) {
                    fromTo.put(start_, end_, new ConnectedShapesComplex(start_, end_, connectorShape));
                }
            }
        }


        XShape current;

        while (true && visited.size() != all.size()) {

            if (shapeQueue.isEmpty()) {
                shapeQueue.add(iterator.next());
            }

            current = shapeQueue.poll();

            if (visited.contains(current)) continue;

            final XShape finalCurrent = current;


            Map<XShape, ConnectedShapesComplex> column = fromTo.column(finalCurrent);
            Collection<ConnectedShapesComplex> filter = column.values();

            for (ConnectedShapesComplex connectedShapesComplex : filter) {
                shapeQueue.add(connectedShapesComplex.toShape);
            }

            visited.add(current);
        }

        System.out.println("Found " + fromTo.values().size() + " link which connects " + fromTo.values().size() * 2 + " nodes");
        System.out.println("Overall count of shapes processed: " + all.size());


        Set<Node> nodes = new HashSet<>();
        Set<Link> links = new HashSet<>();


        graph = HashBasedTable.create();

        normalize(fromTo.values());

        for (ConnectedShapesComplex input : fromTo.values()) {
            Collection<Node> c = nodeFactory.create(input);
            nodes.addAll(c);
            Link link = linkFactory.create(input);
            links.add(link);
            Iterator<Node> iterator1 = c.iterator();
            Node from = iterator1.next();
            Node to = iterator1.next();

            link.setStartNode(from);
            link.setEndNode(to);

            if (!graph.contains(from, to)) {
                graph.put(from, to, new ArrayList<Link>());
            }

            graph.get(from, to).add(link);
        }
        Graph nodeLinkGraph = new Graph(graph, nodes, links);
        connectedShapesComplexes = fromTo.values();
        return nodeLinkGraph;
    }

    private void normalize(Collection<ConnectedShapesComplex> values) {
       for (ConnectedShapesComplex connectedShapesComplex : values){
           connectedShapesComplex.normalize();
       }
    }

    public Collection<ConnectedShapesComplex> getConnectedShapesComplexes() {
        return connectedShapesComplexes;
    }

    //    private Node createNode(Set<XShape> uniqueNodes, XShape shape, Map<XShape, Node> shapeNodeMap) {
//        Node node = null;
//        if (!uniqueNodes.contains(shape)) {
//
//            node = nodeFactory.create();
//        } else {
//            return
//        }
//
//        uniqueNodes.add(shape);
//        return node;
//    }


    private XShape getStart(Set<XShape> all) {

        if (all == null) throw new java.lang.IllegalArgumentException("no nulls, please");

        if (diagramType == null) {
            DiagramType recognise = new DiagramTypeRecognitionImpl().recognise(all);
            diagramType = recognise;
        }
        if (diagramType.equals(DiagramType.Channel)) {
            XShape xShape;
            Optional<XShape> xShapeOptional = Iterables.tryFind(all, new Predicate<XShape>() {
                @Override
                public boolean apply(XShape shape) {
                    if (shapeHelperWrapper.isConnectorShape(shape)) return false;
                    Node.NodeType nodeType = shapeHelperWrapper.getNodeType(shape);
                    if (Node.NodeType.StartMethodOfProcess.equals(nodeType)) {
                        return true;
                    }
                    return false;
                }
            });

            if (xShapeOptional.isPresent()) {
                xShape = xShapeOptional.get();
            } else {
                xShape = all.iterator().next();
            }
            return xShape;

        }

        if (diagramType.equals(DiagramType.Process)) {
            return all.iterator().next();
        }

        throw new IllegalStateException();
    }

    private Node.NodeType getType(XShape shape) {
        String shapeType = shape.getShapeType();
        return Node.NodeType.ClientPort;
    }

    static class Connections {
        enum ConnectType {
            NotConnected,
            DirectConnection,
            RevertedConnection
        }

//        public ConnectType isConnected(XShape shape, XShape shape2) {
//
//        }
    }
}
