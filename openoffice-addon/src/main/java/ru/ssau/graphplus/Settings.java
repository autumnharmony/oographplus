/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import com.google.common.base.Strings;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Settings {

    public static final String ADD_TEXT_SHAPE_TO_LINK = "addTextShapeToLink";
    public static final String AUTOLAYOUT_COMPLEX_LINKS = "autolayoutComplexLinks";
    public static final String PROMPT_FOR_NODE_NAME = "promptForNodeName";
    public static final String LINKING_INPUT_MODE = "linkingInputMode";
    public static final String VALIDATION = "validation";
    private static final String GRAPHPLUS_PROPERTIES = "Graphplus Properties";
    protected static Settings singleton;
    private File file;
    private Properties config;
    private boolean validationRequired;
    private boolean autolayoutComplexLinks;
    private boolean promptForNodeName;
    private LinkingInputMode linkingInputMode;
    private boolean addTextToShapeToLink;
    public Settings(File _file) {
        config = new Properties();
        file = _file;
    }
    private Settings() {
    }
    public synchronized static Settings getSettings() {
        if (singleton == null) {
            String home = System.getProperty("user.home");
            Path settings = Paths.get(home + File.separator + ".graphplus" + File.separator + "settings.properties");
            try {
                Files.createDirectories(settings.getParent());
                File file1 = Files.createFile(settings).toFile();
                singleton = new Settings(file1);
                singleton.load();
            } catch (FileAlreadyExistsException e) {
                singleton = new Settings(settings.toFile());
                singleton.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return singleton;
    }
    public boolean isAutolayoutComplexLinks() {
        return autolayoutComplexLinks;
    }
    public void setAutolayoutComplexLinks(boolean autolayoutComplexLinks) {
        this.autolayoutComplexLinks = autolayoutComplexLinks;
    }
    public boolean isValidationRequired() {
        return validationRequired;
    }

    public void setValidationRequired(boolean validationRequired) {
        this.validationRequired = validationRequired;
    }

    public void load() {
        try {
            config.load(new FileInputStream(file));
            setPromptForNodeName(Boolean.parseBoolean(config.getProperty(PROMPT_FOR_NODE_NAME)));
            setAddTextToShapeToLink(Boolean.parseBoolean(config.getProperty(ADD_TEXT_SHAPE_TO_LINK)));
            setAutolayoutComplexLinks(Boolean.parseBoolean(config.getProperty(AUTOLAYOUT_COMPLEX_LINKS)));
            String linkingInputModeString = config.getProperty(LINKING_INPUT_MODE);
            linkingInputMode = Strings.isNullOrEmpty(linkingInputModeString) || linkingInputModeString.equals("null") ? LinkingInputMode.Silent : LinkingInputMode.valueOf(linkingInputModeString);
            validationRequired = "on".equals(config.getProperty(VALIDATION)) ? true : false;
        } catch (FileNotFoundException e) {
            // first time run
            // ignore
            try {
                FileOutputStream out = null;
                out = new FileOutputStream(file);
                config.store(out, GRAPHPLUS_PROPERTIES);
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Could not load properties", ex);
        }
    }

    public void save() {
        try {
            config.setProperty(PROMPT_FOR_NODE_NAME, String.valueOf(promptForNodeName));
            config.setProperty(LINKING_INPUT_MODE, String.valueOf(linkingInputMode));
            config.setProperty(ADD_TEXT_SHAPE_TO_LINK, String.valueOf(addTextToShapeToLink));
            config.setProperty(VALIDATION, validationRequired ? "on" : "off");
            config.setProperty(AUTOLAYOUT_COMPLEX_LINKS, String.valueOf(autolayoutComplexLinks));
            FileOutputStream out = new FileOutputStream(file);
            config.store(out, GRAPHPLUS_PROPERTIES);
            out.flush();
        } catch (Exception ex) {
            throw new RuntimeException("Could not save properties\nLocation:" + file + "\n" + ex.getMessage());
        }
    }

    public void save(String name, String value) {
        try {
            config.setProperty(name, value);
            FileOutputStream out = new FileOutputStream(file);
            config.save(out, "Graphplus Properties");
            out.flush();
        } catch (Exception ex) {
            throw new RuntimeException("Could not save properties\nLocation:" + file + "\n" + ex.getMessage());
        }
    }

    public boolean promptForNodeName() {
        return promptForNodeName;
    }

    public void setPromptForNodeName(boolean b) {
        promptForNodeName = b;
        save(PROMPT_FOR_NODE_NAME, String.valueOf(b));
    }

    public LinkingInputMode getLinkingInputMode() {
        return linkingInputMode;
    }

    public void setLinkingInputMode(LinkingInputMode linkingInputMode_) {
        linkingInputMode = linkingInputMode_;
        save(LINKING_INPUT_MODE, linkingInputMode_.toString());
    }

    public boolean mouseLinkingMode() {
        return linkingInputMode != null && linkingInputMode.equals(LinkingInputMode.MouseClicking);
    }

    public boolean isAddTextToShapeToLink() {
        return addTextToShapeToLink;
    }

    public void setAddTextToShapeToLink(boolean addTextToShapeToLink) {
        this.addTextToShapeToLink = addTextToShapeToLink;
        save(ADD_TEXT_SHAPE_TO_LINK, String.valueOf(addTextToShapeToLink));
    }

    private void fireChangeEvent(String name, String value) {
    }

    public enum LinkingInputMode {
        MouseClicking,
        Silent
    }
}
