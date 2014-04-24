/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.events;

import ru.ssau.graphplus.api.Node;

public class NodeAddedEvent extends NodeEvent {


    public NodeAddedEvent(Node node) {
        super(node);
    }
}
