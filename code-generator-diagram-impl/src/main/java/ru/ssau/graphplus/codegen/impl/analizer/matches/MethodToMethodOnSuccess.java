/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl.analizer.matches;

import com.google.inject.Inject;
import ru.ssau.graphplus.codegen.impl.CodeProviderAnnotation;
import ru.ssau.graphplus.codegen.impl.MethodToMethodCode;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.codegen.impl.recognition.LinkTypeRecogniser;

@CodeProviderAnnotation(codeProvider = MethodToMethodCode.class)
public class MethodToMethodOnSuccess extends AbstractMethodToMethodMatch {

    @Inject
    public MethodToMethodOnSuccess(ShapeHelperWrapper shapeHelperWrapper, LinkTypeRecogniser linkTypeRecogniser) {
        super(shapeHelperWrapper, linkTypeRecogniser);
        text = "+";
    }


}
