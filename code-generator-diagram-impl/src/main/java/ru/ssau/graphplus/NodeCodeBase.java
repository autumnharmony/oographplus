/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import ru.ssau.graphplus.api.Node;

public abstract class NodeCodeBase implements NodeCode {

    protected Node node;

    public NodeCodeBase(Node node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return getCode();
    }
}
