/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl;

import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.codegen.NodeCode;

import java.util.List;
import java.util.Map;

public abstract class NodeCodeBase extends CodeBase implements NodeCode {

    protected final Node node;

    public NodeCodeBase(Node node, Map<Node, List<Link>> outgoingNodes, Map<Node, List<Link>> incomingNodes) {
        this.node = node;
        this.outgoingNodes = outgoingNodes;
        this.incomingNodes = incomingNodes;
    }

    protected final Map<Node,List<Link>> outgoingNodes;
    protected final Map<Node,List<Link>> incomingNodes;

    @Override
    public String toString() {
        return getCode();
    }
}
