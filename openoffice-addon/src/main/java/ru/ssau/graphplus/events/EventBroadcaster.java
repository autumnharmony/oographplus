/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.events;

public interface EventBroadcaster {
    void addEventListener(EventListener eventListener);
}
