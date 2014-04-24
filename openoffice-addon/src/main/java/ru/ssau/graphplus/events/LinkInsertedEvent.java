/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.events;

import ru.ssau.graphplus.api.Link;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 4/20/14
 * Time: 2:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class LinkInsertedEvent extends LinkEvent {

    private final Link link;

    public LinkInsertedEvent(Link link) {
        this.link = link;
    }

    public Link getLink() {
        return link;
    }
}
