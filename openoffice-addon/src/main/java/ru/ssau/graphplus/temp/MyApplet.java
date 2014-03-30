/*
 * Copyright (c) 2013. Anton Borisov
 */

package ru.ssau.graphplus.temp;

import javax.swing.*;
import java.applet.Applet;


/**
 * User: anton
 * Date: 7/21/13
 * Time: 2:39 PM
 */
public class MyApplet extends Applet {

    public void init() {

        //Execute a job on the event-dispatching thread; creating this applet's GUI.
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    JLabel lbl = new JLabel("Hello World");
                    add(lbl);
                    setBounds(0,0,100,100);
                    setSize(100,100);
                }
            });
        } catch (Exception e) {
            System.err.println("createGUI didn't complete successfully");
        }
    }
}
