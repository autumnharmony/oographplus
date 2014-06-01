/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.api;

import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public abstract class AbstractGraph<N, L> {

    Table<N, N, List<L>> graph;

    protected Set<N> nodes;
    protected Set<L> links;

    public Set<N> getNodes() {
        return nodes;
    }

    public Set<L> getLinks() {
        return links;
    }

    protected AbstractGraph() {
        nodes = Sets.newHashSet();
        links = Sets.newHashSet();
    }

    public Table<N, N, List<L>> getTable() {
        return graph;
    }

    public void link(Node from, Node to, Link link){
        if (!nodes.contains(from)){
            nodes.add((N) from);
        }

        if (!nodes.contains(to)){
            nodes.add((N) to);
        }

        if (!getTable().contains(from,to)){
            getTable().put((N)from, (N)to, new ArrayList<L>());
        }
        getTable().get(from, to).add((L) link);

    }
}
