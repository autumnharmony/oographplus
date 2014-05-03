/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.analizer;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
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
import ru.ssau.graphplus.recognition.DiagramTypeRecognitionImpl;

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

        Injector injector = Guice.createInjector(new CodeGeneratorModule());
    }

    public DiagramType getDiagramType() {
        return diagramType;
    }

    public void setDiagramType(DiagramType diagramType) {
        this.diagramType = diagramType;
    }

    public List<ConnectedShapesComplex> walk(Set<XShape> all, XShape start) {

        visited = Sets.newHashSet();

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

        Table<XShape, XShape, ConnectedShapesComplex> fromTo = HashBasedTable.create();


        for (XShape shape : all) {
            if (shapeHelperWrapper.isConnectorShape(shape)) {

                XConnectorShape connectorShape = QI.XConnectorShape(shape);
                if (!shapeHelperWrapper.isConnected(connectorShape)) continue;
                ConnectedShapes connectedShapes = new ConnectedShapes(connectorShape, unoRuntimeWrapper, shapeHelperWrapper);

                XShape start_ = connectedShapes.getStart();
                XShape end_ = connectedShapes.getEnd();

                if (start_==null || end_ == null){
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

        return Lists.newArrayList(fromTo.values());
    }


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
