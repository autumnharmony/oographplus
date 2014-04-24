/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.commons;

import com.google.inject.AbstractModule;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 4/23/14
 * Time: 1:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class CommonsModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(UnoRuntimeWrapper.class).to(UnoRuntimeWrapperImpl.class);
        bind(MiscHelperWrapper.class).to(MiscHelperWrapperImpl.class);
        bind(ShapeHelperWrapper.class).to(ShapeHelperWrapperImpl.class);
    }
}
