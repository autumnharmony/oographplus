/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import ru.ssau.graphplus.DiagramModel;
import ru.ssau.graphplus.OOoUtils;
import ru.ssau.graphplus.QI;

import java.awt.*;
import java.util.Collection;

public class StageSheetImpl implements Layout.Stage {

    private DiagramModel diagramModel;
    private XComponent drawDocument;

    public StageSheetImpl(DiagramModel diagramModel, XComponent drawDocument) {
        this.diagramModel = diagramModel;
        this.drawDocument = drawDocument;
    }

    @Override
    public Dimension getDimenstion() {

        try {
            int height = OOoUtils.getIntProperty(drawDocument, "Height");
            int width = OOoUtils.getIntProperty(drawDocument, "Width");
            return new Dimension(width, height);
        } catch (UnknownPropertyException | WrappedTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private Collection<Layout.Obj> objects;

    @Override
    public Collection<Layout.Obj> getObjects() {
        if (objects == null){

        }
    }

    @Override
    public Point getPosition(Layout.Obj obj) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Rectangle getBound(Layout.Obj obj) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
