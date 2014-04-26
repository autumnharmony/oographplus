/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.analizer;

import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.uno.RuntimeException;
import ru.ssau.graphplus.*;
import ru.ssau.graphplus.api.DiagramModel;
import ru.ssau.graphplus.api.DiagramType;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.commons.*;

import java.util.*;


public class DiagramWalker implements Walker<XShape, List<ConnectedShapesComplex>> {


    private final UnoRuntimeWrapper unoRuntimeWrapper;
    private /*final*/ DiagramType diagramType;
    Set<XShape> visited;
    ShapeHelperWrapper shapeHelperWrapper;


    @Inject
    public DiagramWalker(ShapeHelperWrapper shapeHelperWrapper, UnoRuntimeWrapper unoRuntimeWrapper) {

        this.shapeHelperWrapper = shapeHelperWrapper;
        this.unoRuntimeWrapper = unoRuntimeWrapper;
    }

    public DiagramType getDiagramType() {
        return diagramType;
    }

    public void setDiagramType(DiagramType diagramType) {
        this.diagramType = diagramType;
    }

    Module getModule() {
        return new CodeGeneratorModule();
    }

    public List<ConnectedShapesComplex> walk(Set<XShape> all, XShape start) {

        visited = Sets.newHashSet();

        Iterator<XShape> iterator = all.iterator();
        if (start == null || ShapeHelper.isConnectorShape(start)) {
            start = getStart(all);
        } else {

        }


        List<ConnectedShapesComplex> connectedShapesComplexes = new ArrayList<>();

        Queue<XShape> shapeQueue = new LinkedList<>();
        shapeQueue.add(start);

        Table<XShape, XShape, ConnectedShapesComplex> fromTo = HashBasedTable.create();

        for (XShape shape : all) {
            if (shapeHelperWrapper.isConnectorShape(shape)) {

                XConnectorShape connectorShape = QI.XConnectorShape(shape);

                ConnectedShapes connectedShapes = new ConnectedShapes(connectorShape, unoRuntimeWrapper, shapeHelperWrapper);

                XShape start_ = connectedShapes.getStart();
                XShape end_ = connectedShapes.getEnd();

//                fromTo.put(start_, end_, new ConnectedShapesComplex(start_, end_));


                if (shapeHelperWrapper.isTextShape(start_) && !shapeHelperWrapper.isTextShape(end_)) {
                    // text -> non text
                    // second part of complex link
                    // start_ is text

                    boolean already = fromTo.column(start_) != null && fromTo.column(start_).size() == 1 && fromTo.column(start_).get(0).toShape.equals(start_);
                    if (already) {
                        ConnectedShapesComplex connectedShapesComplex = fromTo.column(start_).get(0);

//                        assert start_.equals(connectedShapesComplex.toShape);

                        fromTo.put(connectedShapesComplex.fromShape, end_, new ConnectedShapesComplex(connectedShapesComplex.fromShape, end_, connectedShapesComplex.connector, connectorShape, connectedShapesComplex.toShape));
                        fromTo.remove(connectedShapesComplex.fromShape, connectedShapesComplex.toShape);
                    } else {
                        fromTo.put(start_, end_, new ConnectedShapesComplex(start_, end_, connectorShape));
                    }


                } else if (!shapeHelperWrapper.isTextShape(start_) && shapeHelperWrapper.isTextShape(end_)) {

                    // non text -> text
                    // first part of complex link
                    // end_ is text


                    boolean already = fromTo.row(end_) != null && fromTo.row(end_).size() == 1;
                    if (already) {
                        ConnectedShapesComplex connectedShapesComplex = fromTo.row(end_).get(0);

//                        assert connectedShapesComplex.fromShape.equals(end_);

                        fromTo.put(start_, connectedShapesComplex.toShape, new ConnectedShapesComplex(connectedShapesComplex.fromShape, end_, connectedShapesComplex.connector, connectorShape, connectedShapesComplex.toShape));
                        fromTo.remove(connectedShapesComplex.fromShape, connectedShapesComplex.toShape);
                    } else {
                        fromTo.put(start_, end_, new ConnectedShapesComplex(start_, end_, connectorShape));
                    }

                } else {
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

            final XShape finalCurrent = current;

            Iterable<ConnectedShapesComplex> filter = Iterables.filter(connectedShapesComplexes, new Predicate<ConnectedShapesComplex>() {
                @Override
                public boolean apply(ConnectedShapesComplex input) {
                    return input.fromShape.equals(finalCurrent);
                }
            });

            for (ConnectedShapesComplex connectedShapesComplex : filter) {
                shapeQueue.add(connectedShapesComplex.toShape);
            }


            visited.add(current);
        }

        return connectedShapesComplexes;
    }


    private XShape getStart(Set<XShape> all) {
        if (all == null) throw new java.lang.IllegalArgumentException("no nulls please");
        if (diagramType.equals(DiagramType.Channel)) {

            XShape xShape = Iterables.find(all, new Predicate<XShape>() {
                @Override
                public boolean apply(XShape shape) {
                    Node.NodeType nodeType = shapeHelperWrapper.getNodeType(shape);
                    if (Node.NodeType.StartMethodOfProcess.equals(nodeType)) {
                        return true;
                    }
                    return false;
                }
            });
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
