/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.analizer.matches;

import com.google.inject.Inject;
import ru.ssau.graphplus.CodeProviderAnnotation;
import ru.ssau.graphplus.MethodToMethodCode;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.recognition.LinkTypeRecogniser;

@CodeProviderAnnotation(codeProvider = MethodToMethodCode.class)
public class MethodToMethodOnSuccess extends AbstractMethodToMethodMatch {

    @Inject
    public MethodToMethodOnSuccess(ShapeHelperWrapper shapeHelperWrapper, LinkTypeRecogniser linkTypeRecogniser) {
        super(shapeHelperWrapper, linkTypeRecogniser);
        text = "+";
    }


}
