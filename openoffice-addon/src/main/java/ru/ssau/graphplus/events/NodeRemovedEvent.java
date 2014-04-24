/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.events;

import ru.ssau.graphplus.api.Node;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 4/14/14
 * Time: 12:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class NodeRemovedEvent extends NodeEvent {
    public NodeRemovedEvent(Node node) {
        super(node);
    }
}
