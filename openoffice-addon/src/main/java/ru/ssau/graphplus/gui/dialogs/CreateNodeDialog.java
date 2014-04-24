/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui.dialogs;

import com.google.common.collect.ImmutableMap;
import com.sun.star.awt.*;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.*;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import ru.ssau.graphplus.*;
import ru.ssau.graphplus.commons.QI;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 4/12/14
 * Time: 9:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateNodeDialog implements MyDialog<CreateNodeDialog> {
    public static final String CREATE_NODE_DIALOG_XDL = "vnd.sun.star.extension://ru.ssau.graphplus.oograph/dialogs/CreateNodeDialog.xdl";
    private XDialog xDialog;
    private XControlContainer xControlContainer;
    private XShape shape;
    private XComponentContext m_xContext;


    public CreateNodeDialog() {
        createNodeDialogHandler = new CreateNodeDialogHandler(ImmutableMap.<MyDialogHandler.Event, MyDialogHandler.EventHandler>builder()
                .put(MyDialogHandler.Event. event("notToPromptExecute"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        System.out.println("omg");
                        return true;
                    }
                })

                .put(MyDialogHandler.Event.event("notToPromptStatusChanged"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        short state = UnoRuntime.queryInterface(XCheckBox.class, ((ItemEvent) o).Source).getState();
                        Settings.getSettings().setPromptForNodeName(state == 1);
                        return true;
                    }
                })

                .put(MyDialogHandler.Event.event("okExecute"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        try {
                            QI.XPropertySet(shape).setPropertyValue("Text", QI.XText(xControlContainer.getControl("nodeNameTextField")).getString());
                        } catch (UnknownPropertyException | PropertyVetoException | com.sun.star.lang.IllegalArgumentException | WrappedTargetException e) {

                        } finally {
                            xDialog.endExecute();
                            return true;
                        }

                    }
                })
                .build()

        );
    }

    public CreateNodeDialog(XShape shape, XComponentContext m_xContext) {
        this();
        this.shape = shape;
        this.m_xContext = m_xContext;

    }

    class CreateNodeDialogHandler extends MyDialogHandler {

        public CreateNodeDialogHandler(Map<Event, EventHandler> eventHandlerMap) {
            super(eventHandlerMap);
        }
    }

    private CreateNodeDialogHandler createNodeDialogHandler;

    @Override
    public MyDialogHandler getDialogHandler() {
        return createNodeDialogHandler;
    }


    public void init(XDialog xDialog) {
        this.xDialog = xDialog;
        xControlContainer = UnoRuntime.queryInterface(XControlContainer.class, xDialog);
        XControl notToPromptNextTime = xControlContainer.getControl("notToPromptNextTime");
        XCheckBox xCheckBox = UnoRuntime.queryInterface(XCheckBox.class, notToPromptNextTime);
        xCheckBox.setState((short) (Settings.getSettings().promptForNodeName() ? 1 : 0));
    }
}
