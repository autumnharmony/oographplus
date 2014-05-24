/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl;

import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.api.Node;

import java.util.List;
import java.util.Map;

/**
 */
public class ServerNodeCode extends NodeCodeBase {


    public ServerNodeCode(Node node, Map<Node, List<Link>> outgoingNodes, Map<Node, List<Link>> incomingNodes) {
        super(node, outgoingNodes, incomingNodes);
    }

    @Override
    public String getCode() {
        return "SERVER NODE CODE MUST BE HERE";
    }
}
