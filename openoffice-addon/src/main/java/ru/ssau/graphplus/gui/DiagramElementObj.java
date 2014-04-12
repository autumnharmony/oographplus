/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui;


import ru.ssau.graphplus.api.DiagramElement;

import java.awt.*;


/**
 * Wrapper of diagram element for layouting purpose
 */
public class DiagramElementObj implements Layout.Obj {

    final DiagramElement diagramElement;

    public DiagramElementObj(DiagramElement diagramElement) {
        this.diagramElement = diagramElement;
    }

    @Override
    public Dimension getDimension() {
        com.sun.star.awt.Rectangle bound = diagramElement.getBound();
        return new Dimension(bound.Width, bound.Height);
    }

    @Override
    public void setPosition(Point position) {
        diagramElement.setPosition(new com.sun.star.awt.Point(position.x, position.y));
    }

    @Override
    public Point getPosition() {
        com.sun.star.awt.Point position = diagramElement.getPosition();
        return new Point(position.X, position.Y);
    }

    @Override
    public Rectangle getBound() {
        com.sun.star.awt.Rectangle bound = diagramElement.getBound();
        return new Rectangle(bound.X, bound.Y, bound.Width, bound.Height);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiagramElementObj)) return false;

        DiagramElementObj that = (DiagramElementObj) o;

        if (!diagramElement.equals(that.diagramElement)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return diagramElement.hashCode();
    }
}
