/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import com.google.inject.AbstractModule;

public class CodeGeneratorModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UnoRuntimeWrapper.class).to(UnoRuntimeWrapperImpl.class);
    }
}
