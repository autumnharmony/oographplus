/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.analizer.matches;

import com.google.inject.Inject;
import ru.ssau.graphplus.CodeProviderAnnotation;
import ru.ssau.graphplus.LinkCodeBaseImpl;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.recognition.LinkTypeRecogniser;

@CodeProviderAnnotation(codeProvider = LinkCodeBaseImpl.class)
public class MethodToMethodMatch extends MatchBase {

    @Inject
    public MethodToMethodMatch(ShapeHelperWrapper shapeHelperWrapper, LinkTypeRecogniser linkTypeRecogniser) {
        super(shapeHelperWrapper, linkTypeRecogniser);
    }

    @Override
    public boolean matches(ConnectedShapesComplex connectedShapesComplex) {
        return shapeHelperWrapper.getNodeType(connectedShapesComplex.fromShape).equals(Node.NodeType.MethodOfProcess) && shapeHelperWrapper.getNodeType(connectedShapesComplex.toShape).equals(Node.NodeType.MethodOfProcess);
    }
}
