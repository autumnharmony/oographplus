package ru.ssau.graphplus.codegen.impl;

import com.google.inject.Inject;
import ru.ssau.graphplus.codegen.CodeProvider;
import ru.ssau.graphplus.codegen.impl.recognition.LinkTypeRecogniser;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


public class CodeProviderFactoryImpl implements CodeProviderFactory {


    private final LinkTypeRecogniser linkTypeRecogniser;
    private final ShapeHelperWrapper shapeHelper;

    @Inject
    public CodeProviderFactoryImpl(LinkTypeRecogniser linkTypeRecogniser, ShapeHelperWrapper shapeHelper) {
        this.linkTypeRecogniser = linkTypeRecogniser;
        this.shapeHelper = shapeHelper;
    }

    @Override
    public CodeProvider create(Class<? extends CodeProvider> codeProviderClass){

        try {
            Constructor<? extends CodeProvider> constructor = null;
            constructor = codeProviderClass.getConstructor(ConnectedShapesComplex.class, LinkTypeRecogniser.class, ShapeHelperWrapper.class);
            CodeProvider codeProvider1 = null;
            codeProvider1 = constructor.newInstance(null, linkTypeRecogniser, shapeHelper);
            return codeProvider1;
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }


    }
}
