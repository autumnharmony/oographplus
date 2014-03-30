/*
 * Copyright (c) 2013. Anton Borisov
 */

package ru.ssau.graphplus;

public interface DeferredInitializable<T> {

    void setProps();

    void setProps(T ... params);
}
