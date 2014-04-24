/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class BetweenNodesLayout implements Layout {

    private final Stage stage;
    private final Logger logger;
    private Obj node1;
    private Obj node2;

    public BetweenNodesLayout(Stage stage, Obj node1, Obj node2) {
        this.node1 = node1;
        this.node2 = node2;
        this.stage = stage;
        logger = Logger.getLogger("BetweenNodesLayout");
    }

    @Override
    public void layout(Stage stage, Obj obj) {
        Point position1 = node1.getPosition();
        Point position2 = node2.getPosition();

        obj.setPosition(new Point((position1.x + position2.x) / 2, (position1.y + position2.y) / 2));

//        if (Math.abs(position2.x - position1.x) < Math.abs(position2.y - position1.y)) {
//
//            boolean placed = false;
//            while (!placed){
//
//                if ()
//
//            }
//
//        } else if (Math.abs(position2.x - position1.x) > Math.abs(position2.y - position1.y)) {
//
//        }
    }

    @Override
    public void layout(Obj obj) {
        layout(stage, obj);
    }


    private Point findPlace(Stage stage, Obj obj, Point nearPoint) {


        int x = nearPoint.x;
        int y = nearPoint.y;


        boolean placed = false;

        while (!placed){
            obj.setPosition(new Point(x,y));
            Rectangle bound = obj.getBound();

            boolean intersects = false;
            java.util.List<Obj> intersectsWith = new ArrayList();
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

    private Dimension getShift(List<Obj> intersectsWith, Obj obj) {
              return new Dimension(0,0);
    }
}
