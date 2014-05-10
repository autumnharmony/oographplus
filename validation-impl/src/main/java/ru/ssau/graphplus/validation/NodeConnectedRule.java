/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.validation;

import ru.ssau.graphplus.api.DiagramModel;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.api.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 5/2/14
 * Time: 6:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class NodeConnectedRule implements NodeRule {

    private DiagramModel diagramModel;

    private Set<Node> connectedNodes;

    public NodeConnectedRule(DiagramModel diagramModel) {
        this.diagramModel = diagramModel;
        connectedNodes = new HashSet<>();
        for (Link link : diagramModel.getLinks()) {
            Node startNode = link.getStartNode();
            Node endNode = link.getEndNode();
            connectedNodes.add(startNode);
            connectedNodes.add(endNode);
        }
    }

    @Override
    public RuleResult<Node> check(Node node) {
        if (connectedNodes.contains(node)) {
            return new ResultOk();
        } else {
            return new RuleError("Not connected node", node);
        }
    }
}