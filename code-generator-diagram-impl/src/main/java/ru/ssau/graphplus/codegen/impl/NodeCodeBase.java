/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl;

import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.codegen.NodeCode;

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
