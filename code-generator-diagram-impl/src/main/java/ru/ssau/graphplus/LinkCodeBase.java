/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import ru.ssau.graphplus.api.Link;


public class LinkCodeBase implements LinkCode {
    @Override
    public String getCode() {
        return "";
    }

    private final Link link;

    public LinkCodeBase(Link link) {
        this.link = link;
    }
}
