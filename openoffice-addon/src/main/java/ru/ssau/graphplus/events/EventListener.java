/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.events;

public interface EventListener<T extends Event> {
    void onEvent(T event);
}
