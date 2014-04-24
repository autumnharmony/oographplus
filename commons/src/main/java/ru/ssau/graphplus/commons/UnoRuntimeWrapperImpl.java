/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.commons;

import com.sun.star.uno.UnoRuntime;


public class UnoRuntimeWrapperImpl implements UnoRuntimeWrapper {
    @Override
    public <T> T queryInterface(Class<T> tClass, Object o) {
        return UnoRuntime.queryInterface(tClass, o);
    }
}
