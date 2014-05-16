/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl.analizer;

import com.google.common.collect.Table;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.api.Node;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 */
public class Graph extends AbstractGraph<Node,Link> {


    public Graph(Table<Node, Node, List<Link>> graph, Set<Node> nodeSet, Set<Link> linkSet) {
        this.graph = graph;

        nodes.addAll(nodeSet);
        links.addAll(linkSet);
    }


}
