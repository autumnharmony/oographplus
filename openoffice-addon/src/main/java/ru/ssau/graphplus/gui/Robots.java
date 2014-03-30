/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 2/22/14
 * Time: 12:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class Robots {
    public static void main(String[] args) {
        GraphicsEnvironment ge = GraphicsEnvironment.
                  getLocalGraphicsEnvironment();
         GraphicsDevice[] gs = ge.getScreenDevices();
           for (int j = 0; j < gs.length; j++) {
               GraphicsDevice gd = gs[j];
                 GraphicsConfiguration[] gc =
                gd.getConfigurations();
               for (int i=0; i < gc.length; i++) {
                       JFrame f = new
                                 JFrame(gs[j].getDefaultConfiguration());
                        Canvas c = new Canvas(gc[i]);
                       Rectangle gcBounds = gc[i].getBounds();
                       int xoffs = gcBounds.x;
                       int yoffs = gcBounds.y;
                       f.getContentPane().add(c);
                       f.setLocation((i*50)+xoffs, (i*60)+yoffs);
                       f.show();
                    }
               }
    }
}
