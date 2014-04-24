/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.events;

import ru.ssau.graphplus.api.Node;


public class NodeEvent implements Event {
    protected Node node;

    public Node getNode() {
        return node;
    }

    public NodeEvent(Node node) {
        this.node = node;
    }
}
