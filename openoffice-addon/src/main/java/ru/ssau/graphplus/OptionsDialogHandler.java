/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.sun.star.awt.XContainerWindowEventHandler;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XControlModel;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.uno.*;
import com.sun.star.uno.Exception;
import com.sun.star.uno.RuntimeException;

import java.util.Arrays;

public class OptionsDialogHandler extends WeakBase implements XServiceInfo, XContainerWindowEventHandler {
    public static final String PROMPT_FOR_NODE_NAME = "promptForNodeName";
    public static String[] SupportedWindowNames = { "FooOptionsPage" };

    static private final String __serviceName = "ru.ssau.graphplus.gui.OptionsDialogHandler";

    private XComponentContext context;

    public OptionsDialogHandler(XComponentContext xCompContext) {
        this.context = xCompContext;
    }

    public String[] getSupportedServiceNames() {
        return getServiceNames();
    }

    public static String[] getServiceNames() {
        String[] sSupportedServiceNames = { __serviceName };
        return sSupportedServiceNames;
    }

    public boolean supportsService( String sServiceName ) {
        return sServiceName.equals( __serviceName );
    }

    public String getImplementationName() {
        return OptionsDialogHandler.class.getName();
    }

    public boolean callHandlerMethod(com.sun.star.awt.XWindow aWindow, Object aEventObject, String sMethod) throws WrappedTargetException {
        if (sMethod.equals("external_event") ) {
            try {
                return handleExternalEvent(aWindow, aEventObject);
            }
            catch (com.sun.star.uno.RuntimeException re) {
                throw re;
            }
            catch (com.sun.star.uno.Exception e) {
                e.printStackTrace();
                throw new WrappedTargetException(sMethod, this, e);
            }
        }

        // return false when event was not handled
        return false;
    }

    public String[] getSupportedMethodNames() {
        return new String[]  { "external_event" };
    }

    private boolean handleExternalEvent(com.sun.star.awt.XWindow aWindow, Object aEventObject) throws com.sun.star.uno.Exception {
        try {
            String sMethod = AnyConverter.toString(aEventObject);
            if (sMethod.equals("ok")) {
                saveData(aWindow);
            }
            else if (sMethod.equals("back") || sMethod.equals("initialize")) {
                loadData(aWindow);
            }
            else {
                System.err.println("handleExternalEvent: " + sMethod);
            }
        }
        catch (com.sun.star.lang.IllegalArgumentException ex) {
            ex.printStackTrace();
            throw new com.sun.star.lang.IllegalArgumentException("Method external_event requires a string in the event object argument.", this, (short) -1);
        }
        return true;
    }

    /* returns the property set (to get/set a string) from a control name */
    private XPropertySet getPropertySet(XControlContainer xContainer, String name) {
        XControl xControl = xContainer.getControl(name);

        if (xControl == null)
            return null;

        XPropertySet xProp = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xControl.getModel());

        return xProp;
    }

    private void setProperty(XControlContainer xContainer, String name, String key, Object value) {
        try {
            XPropertySet propertySet = getPropertySet(xContainer, name);
            if (propertySet == null)
                return;

            propertySet.setPropertyValue(key, value);
        }
        catch (com.sun.star.uno.Exception ex) {
            ex.printStackTrace();
        }
    }

    private Object getProperty(XControlContainer xContainer, String name, String key) {
        try {
            XPropertySet propertySet = getPropertySet(xContainer, name);
            if (propertySet == null)
                return null;

            return propertySet.getPropertyValue(key);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void saveData(com.sun.star.awt.XWindow aWindow) throws com.sun.star.lang.IllegalArgumentException, Exception {
//        String sWindowName = getWindowName(aWindow);
//        if (sWindowName == null)
//            throw new com.sun.star.lang.IllegalArgumentException("This window is not supported by this handler", this, (short) -1);

        XControlContainer xContainer = (XControlContainer) UnoRuntime.queryInterface(XControlContainer.class, aWindow);
        if (xContainer == null)
            throw new Exception("Could not get XControlContainer from window.", this);

        /* handle the AtD categories */

            Object aObj = getProperty(xContainer, PROMPT_FOR_NODE_NAME, "State");

            if (1 == AnyConverter.toShort(aObj)) {
                Settings.getSettings().setPromptForNodeName(true);
            }
            else {
                Settings.getSettings().setPromptForNodeName(false);
            }


        Object property = getProperty(xContainer, "linkingInputMode", "Text");
        String sLinkingMode = (String) property;
        Settings.LinkingInputMode linkingInputMode = Settings.LinkingInputMode.valueOf(sLinkingMode);
        Settings.getSettings().setLinkingInputMode(linkingInputMode);


        try {
            Settings.getSettings().save();
        }
        catch (java.lang.RuntimeException ex) {
            throw new RuntimeException("can't save settings", ex);
        }
    }

    private void loadData(com.sun.star.awt.XWindow aWindow) throws Exception {
        String sWindowName = getWindowName(aWindow);
//        if (sWindowName == null)
//            throw new com.sun.star.lang.IllegalArgumentException("The window is not supported by this handler", this, (short) -1);

        XControlContainer xContainer = (XControlContainer) UnoRuntime.queryInterface(XControlContainer.class, aWindow);
        if (xContainer == null)
            throw new Exception("Could not get XControlContainer from window.", this);

//            for (int x = 0; x < categories.length; x++) {
            Short value = new Short( (short) (Settings.getSettings().promptForNodeName() ? 1 : 0) );
            setProperty(xContainer, PROMPT_FOR_NODE_NAME, "State", value);

        Settings.LinkingInputMode[] values = Settings.LinkingInputMode.values();
        Iterable<String> transform = Iterables.transform(Arrays.asList(values), new Function<Settings.LinkingInputMode, String>() {
            @Override
            public String apply(Settings.LinkingInputMode o) {
                return o.toString();
            }
        });

        setProperty(xContainer, "linkingInputMode", "StringItemList", Iterables.toArray(transform, String.class));
        setProperty(xContainer, "linkingInputMode", "Text", Settings.getSettings().getLinkingInputMode().toString());
//            }

//            setProperty(xContainer, "service", "Text", Configuration.getConfiguration().getServiceHost());
    }

    // Checks if the name property of the window is one of the supported names and returns
    // always a valid string or null
    private String getWindowName(com.sun.star.awt.XWindow aWindow) throws Exception {
        if (aWindow == null)
            new com.sun.star.lang.IllegalArgumentException("Method external_event requires that a window is passed as argument", this, (short) -1);

        // We need to get the control model of the window. Therefore the first step is
        // to query for it.
        XControl xControlDlg = (XControl) UnoRuntime.queryInterface(XControl.class, aWindow);

        if (xControlDlg == null)
            throw new Exception("Cannot obtain XControl from XWindow in method external_event.");

        // Now get model
        XControlModel xModelDlg = xControlDlg.getModel();

        if (xModelDlg == null)
            throw new Exception("Cannot obtain XControlModel from XWindow in method external_event.", this);

        // The model itself does not provide any information except that its
        // implementation supports XPropertySet which is used to access the data.
        XPropertySet xPropDlg = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xModelDlg);
        if (xPropDlg == null)
            throw new Exception("Cannot obtain XPropertySet from window in method external_event.", this);

        // Get the "Name" property of the window
        Object aWindowName = xPropDlg.getPropertyValue("Name");

        // Get the string from the returned com.sun.star.uno.Any
        String sName = null;
        try {
            sName = AnyConverter.toString(aWindowName);
        }
        catch (com.sun.star.lang.IllegalArgumentException ex) {
            ex.printStackTrace();
            throw new Exception("Name - property of window is not a string.", this);
        }

        // Eventually we can check if we this handler can "handle" this options page.
        // The class has a member m_arWindowNames which contains all names of windows
        // for which it is intended
        for (int i = 0; i < SupportedWindowNames.length; i++) {
            if (SupportedWindowNames[i].equals(sName)) {
                return sName;
            }
        }
        return null;
    }
}
