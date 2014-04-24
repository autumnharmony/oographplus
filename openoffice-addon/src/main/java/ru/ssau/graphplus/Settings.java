/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 4/2/14
 * Time: 9:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class Settings {

    protected static Settings singleton;
    private File file;
    private Properties config;

    public Settings(File _file) {
        config = new Properties();
        file   = _file;
    }

    public void load() {
        try {
            config.load(new FileInputStream(file));
            setPromptForNodeName(Boolean.parseBoolean(config.getProperty("promptForNodeName")));
            linkingInputMode = LinkingInputMode.valueOf(config.getProperty("linkingInputMode"));
        }
        catch (Exception ex) {
        }
    }

    public void save() {
        try {
            config.setProperty("promptForNodeName", String.valueOf(promptForNodeName));
            config.setProperty("linkingInputMode", String.valueOf(linkingInputMode));
            FileOutputStream out = new FileOutputStream(file);
            config.save(out, "Graphplus Properties");
            out.flush();
        }
        catch (Exception ex) {
            throw new RuntimeException("Could not save properties\nLocation:" + file + "\n" + ex.getMessage());
        }
    }





    public synchronized static Settings getSettings() {
            if (singleton == null) {
                singleton = new Settings(new File(System.getProperty("user.home"), ".graphplus"));
                singleton.load();
            }
            return singleton;

    }

    private Settings() {
    }

    private boolean promptForNodeName;

    public boolean promptForNodeName(){
        return promptForNodeName;
    }

    public void setPromptForNodeName(boolean b){
        promptForNodeName = b;
        save();
    }

    public void setLinkingInputMode(LinkingInputMode linkingInputMode_){
        linkingInputMode = linkingInputMode_;
        save();
    }

    public LinkingInputMode getLinkingInputMode() {
        return linkingInputMode;
    }

    private LinkingInputMode linkingInputMode;


    public boolean mouseLinkingMode(){
        return linkingInputMode != null && linkingInputMode.equals(LinkingInputMode.MouseClicking);
    }

    public enum LinkingInputMode {
        MouseClicking,
        Silent
    }
}
