/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.inject.Inject;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import ru.ssau.graphplus.DiagramModel;
import ru.ssau.graphplus.DrawHelper;
import ru.ssau.graphplus.commons.OOoUtils;
import ru.ssau.graphplus.api.DiagramElement;

import java.awt.*;
import java.util.Collection;

public class StageSheetImpl implements Layout.Stage {

    private DiagramModel diagramModel;
    private XComponent drawDocument;

    @Inject
    public StageSheetImpl(DiagramModel diagramModel, XComponent drawDocument) {
        this.diagramModel = diagramModel;
        this.drawDocument = drawDocument;
    }

    @Override
    public Dimension getDimenstion() {

        try {
            XDrawPage currentDrawPage = DrawHelper.getCurrentDrawPage(drawDocument);

            int height = OOoUtils.getIntProperty(currentDrawPage, "Height");
            int width = OOoUtils.getIntProperty(currentDrawPage, "Width");
            return new Dimension(width, height);
        } catch (UnknownPropertyException | WrappedTargetException e) {
            throw new RuntimeException("can't get dimension", e);
        }

    }

    private Collection<Layout.Obj> objects;

    @Override
    public Collection<Layout.Obj> getObjects() {
        if (objects == null){
            objects = Collections2.transform(diagramModel.getDiagramElements(), new Function<DiagramElement, Layout.Obj>() {
                @Override
                public Layout.Obj apply(ru.ssau.graphplus.api.DiagramElement diagramElement) {
                    return new DiagramElementObj(diagramElement);
                }
            });
        }

        return objects;
    }

    @Override
    public Point getPosition(Layout.Obj obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Rectangle getBound(Layout.Obj obj) {
        throw new UnsupportedOperationException();
    }
}
