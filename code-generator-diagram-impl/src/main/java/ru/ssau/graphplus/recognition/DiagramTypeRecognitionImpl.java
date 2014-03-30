/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.recognition;

import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.api.DiagramType;

import java.util.Set;

public class DiagramTypeRecognitionImpl implements DiagramTypeRecognition {
    @Override
    public DiagramType recognise(Set<XShape> shapes) {
//        Collection<Node> nodes = shapes.getNodes();
//        for (Node node : nodes){
//            if (node.getType().equals(Node.NodeType.MethodOfProcess)){
//                return DiagramType.StartMethodOfProcess;
//            }
//        }
        return DiagramType.Channel;
    }
}
