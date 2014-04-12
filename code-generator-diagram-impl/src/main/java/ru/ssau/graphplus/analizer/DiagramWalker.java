/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.analizer;

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
import ru.ssau.graphplus.gui.Gui;

import java.util.*;


public class DiagramWalker implements Walker<XShape, DiagramModel> {


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

    @Inject
    private MatchFactory matchFactory;

    public DiagramModel walk(Set<XShape> all, XShape start) {

        visited = Sets.newHashSet();

        Iterator<XShape> iterator = all.iterator();
        if (start == null || ShapeHelper.isConnectorShape(start)) {
            start = getStart(all);
        } else {

        }

        Set<XConnectorShape> connectorShapes = Sets.newHashSet();

        // non-text shape -> text shape
        Multimap<XShape, XShape> nonTextToText = ArrayListMultimap.create();

        // text shape -> non-text shape
        Multimap<XShape, XShape> textToNonText = ArrayListMultimap.create();

        // non-text -> non-text
        Multimap<XShape, XShape> nontextToNonText = ArrayListMultimap.create();

        List<ConnectedShapesComplex> connectedShapesComplexes = new ArrayList<>();

        Queue<XShape> shapeQueue = new LinkedList<>();
        shapeQueue.add(start);



        for (XShape shape : all) {
            if (shapeHelperWrapper.isConnectorShape(shape)) {

                XConnectorShape connectorShape = QI.XConnectorShape(shape);

                ConnectedShapes connectedShapes = new ConnectedShapes(connectorShape, unoRuntimeWrapper, shapeHelperWrapper);

                XShape start_ = connectedShapes.getStart();
                XShape end_ = connectedShapes.getEnd();

                if (shapeHelperWrapper.isTextShape(start_) && !shapeHelperWrapper.isTextShape(end_)) {
                    textToNonText.put(start_, end_);
                } else if (!shapeHelperWrapper.isTextShape(start_) && shapeHelperWrapper.isTextShape(end_)) {
                    nonTextToText.put(start_, end_);
                } else {
                    nontextToNonText.put(start_, end_);
                }
            }
        }

        beSureThatOneItem(textToNonText);
        beSureThatOneItem(nonTextToText);


        for (XShape nonText1 : nonTextToText.keySet()) {
            XShape text = nonTextToText.get(nonText1).iterator().next();
            XShape nonText2 = textToNonText.get(text).iterator().next();

            ConnectedShapesComplex connectedShapesComplex = new ConnectedShapesComplex(nonText1,nonText2,text);
            connectedShapesComplexes.add(connectedShapesComplex);
        }

        for (XShape shape : nontextToNonText.keySet()){
            Collection<XShape> shapes = nontextToNonText.get(shape);
            for (XShape shape2 : shapes){
                ConnectedShapesComplex connectedShapesComplex = new ConnectedShapesComplex(shape, shape2, null );
                connectedShapesComplexes.add(connectedShapesComplex);
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
                    return input.getFromShape().equals(finalCurrent);
                }
            });

            for (ConnectedShapesComplex one : filter){

                for (Match match : matchFactory.createAll()){
                    if (match.matches(one)){
                        System.out.println(match.getClass().getSimpleName());
                    }
                }
                shapeQueue.add(one.getToShape());
            }


            visited.add(current);
        }


        return null;
    }

    private void beSureThatOneItem(Multimap<XShape, XShape> textToNonText) {
        for (XShape shape : textToNonText.keySet()) {
            if (textToNonText.get(shape).size() > 1) {
                throw new RuntimeException("", new IllegalArgumentException());
            }
        }
    }

    private XShape getStart(Set<XShape> all) {

        if (diagramType.equals(DiagramType.Channel)){
            for (XShape xShape : all) {
                Node.NodeType nodeType = shapeHelperWrapper.getNodeType(xShape);
                if (nodeType.equals(Node.NodeType.StartMethodOfProcess)) {
                    return xShape;
                }
            }
        }

        if (diagramType.equals(DiagramType.Process)){
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
