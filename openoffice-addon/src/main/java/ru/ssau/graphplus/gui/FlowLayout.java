/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui;


import com.google.inject.Inject;
import ru.ssau.graphplus.api.DiagramElement;
import ru.ssau.graphplus.api.Link;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * Flow layout. Object's positioning from left to right, from top to bottom
 */
public class FlowLayout implements Layout {

    private Stage stage;
    private Logger logger;

    @Inject
    public FlowLayout(Stage stage) {
        this();
        this.stage = stage;
    }

    public FlowLayout() {
        logger = Logger.getLogger(getClass().getSimpleName());
    }

    @Override
    public void layout(Stage stage, Obj obj) {

        if (obj instanceof DiagramElementObj){
            DiagramElementObj diagramElementObj = (DiagramElementObj) obj;
            DiagramElement diagramElement = diagramElementObj.getDiagramElement();
            if (diagramElement instanceof Link){
                Link link = (Link) diagramElement;
                if (link.isConnected()){

//                    while ()

                    return;
                }
            }
        }

        findPlace(stage, obj, new Point(0,0));

    }

    private Point findPlace(Stage stage, Obj obj, Point startPoint) {


        int x = startPoint.x;
        int y = startPoint.y;


        boolean placed = false;

        while (!placed){
            obj.setPosition(new Point(x,y));
            Rectangle bound = obj.getBound();

            boolean intersects = false;
            List<Obj> intersectsWith = new ArrayList();
            for (Obj staged : stage.getObjects()){
                if (obj.equals(staged)) continue;
                if (staged.getBound().intersects(bound)){
                    intersects = true;
                    intersectsWith.add(staged);
                    break;
                }
            }

            if (intersects)
            {

                logger.info("intersects");

                if (x + obj.getDimension().width >= stage.getDimenstion().width)
                {
                    if (y + obj.getDimension().height >= stage.getDimenstion().height)
                    {
                        throw new IllegalStateException("can't place object on stage");
                    }

                    x = 0;
                    y += getShift(intersectsWith, obj).height;
                    logger.info("y = "+ y);
                }
                else
                {
                    x += getShift(intersectsWith, obj).width;
                    logger.info("x = "+ x);
                }
            }
            else {
                obj.setPosition(new Point(x,y));
                placed = true;
            }
        }
        return new Point(x,y);
    }

    Dimension getShift(List<Obj> interesectsWith, Obj o) {

        int maxX = 0;
        int maxY = 0;
        for (Obj obj : interesectsWith){
            Rectangle bound = obj.getBound();
            int right = bound.x + bound.width;
            int bottom = bound.y + bound.height;


            if (right > maxX){
                maxX = right;
            }

            if (bottom > maxY) {
                maxY = bottom;
            }


        }

        return new Dimension(maxX - o.getPosition().x, maxY - o.getPosition().y);
    }



    public void layout(Obj obj){
        layout(stage, obj);
    }
}
