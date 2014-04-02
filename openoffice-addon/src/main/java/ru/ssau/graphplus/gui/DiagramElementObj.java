/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui;

import com.sun.star.awt.*;
import ru.ssau.graphplus.api.DiagramElement;
import ru.ssau.graphplus.api.Node;

import java.awt.*;
import java.awt.Point;
import java.awt.Rectangle;

public class DiagramElementObj implements Layout.Obj {

    DiagramElement diagramElement;

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
        diagramElement.setPosition(position);
    }

    @Override
    public Point getPosition() {
        return diagramElement.getPosition();
    }

    @Override
    public Rectangle getBound() {
        com.sun.star.awt.Rectangle bound = diagramElement.getBound();
        return new Rectangle(bound.X, bound.Y, bound.Width, bound.Height);
    }
}
