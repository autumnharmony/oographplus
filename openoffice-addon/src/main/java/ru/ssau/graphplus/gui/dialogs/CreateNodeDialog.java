/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui.dialogs;

import com.google.common.collect.ImmutableMap;
import com.sun.star.awt.*;
import com.sun.star.drawing.XShape;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import ru.ssau.graphplus.*;
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.gui.MyDialog;

import java.util.Map;

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
                            QI.XText(shape).setString(QI.XTextComponent(xControlContainer.getControl("nameTextField")).getText());
                        } finally {
                            xDialog.endExecute();
                            return true;
                        }

                    }
                })
                .put(MyDialogHandler.Event.event("cancelExecute"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        xDialog.endExecute();
                        return true;
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
