/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.validation.impl;

import com.sun.star.awt.Point;
import com.sun.star.awt.Rectangle;
import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.ShapesProvider;
import ru.ssau.graphplus.api.DiagramElement;

import java.util.Collections;
import java.util.UUID;

/**
 */
public class UnknownSingleShapedDiagramElement implements DiagramElement, ShapesProvider {

    private final XShape unusedShape;

    public UnknownSingleShapedDiagramElement(XShape unusedShape) {
        this.unusedShape = unusedShape;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setId(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Rectangle getBound() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPosition(Point position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Point getPosition() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<XShape> getShapes() {
        return Collections.singleton(unusedShape);
    }
}
