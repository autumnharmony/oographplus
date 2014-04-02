/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 4/2/14
 * Time: 9:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class Settings {


    public static boolean promptForNodeName = false;

    public static LinkingInputMode linkingInputMode;

    public static boolean mouseLinkingMode(){
        return false;
    }

    private enum LinkingInputMode {
        MouseClicking,
        Silent
    }
}
