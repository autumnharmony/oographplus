/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import com.google.inject.AbstractModule;
import ru.ssau.graphplus.commons.*;

import static org.mockito.Mockito.mock;

public class TestModule extends AbstractModule {

    private final ShapeHelperWrapper shapeHelperWrapper;
    private final UnoRuntimeWrapper unoRuntimeWrapper;

    public TestModule(ShapeHelperWrapper shapeHelperWrapper, UnoRuntimeWrapper unoRuntimeWrapper) {
        this.shapeHelperWrapper = shapeHelperWrapper;
        this.unoRuntimeWrapper = unoRuntimeWrapper;

    }

    @Override
    protected void configure() {
        requestStaticInjection(ConnectedShapes.class);
        bind(UnoRuntimeWrapper.class).toInstance(unoRuntimeWrapper);
        bind(MiscHelperWrapper.class).to(MiscHelperWrapperImpl.class);
        bind(ShapeHelperWrapper.class).toInstance(shapeHelperWrapper);
    }
}
