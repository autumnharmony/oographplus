/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import com.google.inject.AbstractModule;
import ru.ssau.graphplus.commons.UnoRuntimeWrapper;
import ru.ssau.graphplus.commons.UnoRuntimeWrapperImpl;

public class CodeGeneratorModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UnoRuntimeWrapper.class).to(UnoRuntimeWrapperImpl.class);
    }
}
