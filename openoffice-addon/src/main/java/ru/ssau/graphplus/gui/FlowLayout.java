/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui;

import java.awt.*;


public class FlowLayout implements Layout {
    @Override
    public void layout(Stage stage, Obj obj) {


        int x = 0;
        int y = 0;

        int dx = 10;
        int dy = 10;

        boolean placed = false;

        while (!placed){
            obj.setPosition(new Point(x,y));
            Rectangle bound = obj.getBound();

            boolean intersects = false;
            for (Obj staged : stage.getObjects()){
                if (staged.getBound().intersects(bound)){
                    intersects = true;
                    break;
                }
            }

            if (intersects)
            {
                if (x + obj.getDimension().width > stage.getDimenstion().width)
                {
                    if (y + obj.getDimension().height > stage.getDimenstion().height)
                    {
                        throw new IllegalStateException("can't place object on stage");
                    }

                    x = 0;
                    y += dy;

                }
                else
                {
                    x += dx;
                }
            }
            else {
                obj.setPosition(new Point(x,y));
                placed = true;
            }
        }

    }
}
