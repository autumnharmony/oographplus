/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl.analizer;

import com.google.common.collect.Table;

import java.util.List;
import java.util.Set;

/**
 */
public abstract class AbstractGraph<N,L> {

    Table<N,N,List<L>> graph;

    protected Set<N> nodes;
    protected Set<L> links;

    public Set<N> getNodes() {
        return nodes;
    }

    public Set<L> getLinks() {
        return links;
    }
}
