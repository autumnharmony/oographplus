/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.validation.impl;

import ru.ssau.graphplus.api.DiagramModel;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.validation.NodeRule;
import ru.ssau.graphplus.validation.RuleResult;

import java.util.HashSet;
import java.util.Set;


public class NodeConnectedRule extends RuleBase<Node> implements NodeRule {

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
        } else return warning(node);
    }
}
