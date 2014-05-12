/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl.analizer.matches;


import com.google.inject.Inject;
import ru.ssau.graphplus.codegen.impl.CodeProviderAnnotation;
import ru.ssau.graphplus.codegen.impl.DataAndControlMixedCode;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.codegen.impl.recognition.LinkTypeRecogniser;

@CodeProviderAnnotation(codeProvider = DataAndControlMixedCode.class)
public class DataAndControlMixedMatch extends MatchBase {
    @Inject
    public DataAndControlMixedMatch(ShapeHelperWrapper shapeHelperWrapper, LinkTypeRecogniser linkTypeRecogniser) {
        super(shapeHelperWrapper, linkTypeRecogniser);
    }

    @Override
    public boolean matches(ConnectedShapesComplex connectedShapesComplex) {
        return super.matches(connectedShapesComplex) && getLinkType(connectedShapesComplex).equals(Link.LinkType.MixedFlow);
    }


}
