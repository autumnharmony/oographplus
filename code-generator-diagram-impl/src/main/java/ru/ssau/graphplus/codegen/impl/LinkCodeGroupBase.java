/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl;

import com.google.common.base.Joiner;
import ru.ssau.graphplus.codegen.impl.recognition.LinkTypeRecogniser;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;

import java.util.List;

/**
 */
public abstract class LinkCodeGroupBase<T> extends LinkCodeBase implements GroupingCode<T>{


    protected List<T> group;

    public LinkCodeGroupBase(ConnectedShapesComplex connectedShapesComplex, LinkTypeRecogniser linkTypeRecogniser, ShapeHelperWrapper shapeHelper) {
        super(connectedShapesComplex, linkTypeRecogniser, shapeHelper);
    }


    public void setGroup(List<T> group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return "LinkCodeGroupBase{" +
                "group=" + Joiner.on(',').join(group) +
                '}';
    }
}
