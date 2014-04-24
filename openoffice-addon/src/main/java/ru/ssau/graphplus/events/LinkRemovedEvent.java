/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.events;


import ru.ssau.graphplus.api.Link;

public class LinkRemovedEvent extends LinkEvent {

    private final Link link;

    public LinkRemovedEvent(Link link) {
        this.link = link;
    }
}
