/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.commons;

import com.google.inject.AbstractModule;

public class CommonsModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(UnoRuntimeWrapper.class).to(UnoRuntimeWrapperImpl.class);
        bind(MiscHelperWrapper.class).to(MiscHelperWrapperImpl.class);
        bind(ShapeHelperWrapper.class).to(ShapeHelperWrapperImpl.class);
    }
}
