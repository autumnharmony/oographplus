/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui;

import com.google.inject.Inject;
import com.sun.star.accessibility.XAccessible;
import com.sun.star.awt.*;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.frame.XController;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.lang.DisposedException;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.ui.LayoutSize;
import com.sun.star.uno.*;
import ru.ssau.graphplus.DrawHelper;
import ru.ssau.graphplus.api.DiagramService;
import ru.ssau.graphplus.commons.MiscHelper;
import ru.ssau.graphplus.MyDispatch;
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.gui.sidebar.PanelBase;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.node.NodeBase;
import ru.ssau.graphplus.node.NodeFactory;

import java.lang.Exception;

public class InsertNodePanel extends PanelBase {

    private static final String DIALOG_PATH = "vnd.sun.star.extension://ru.ssau.graphplus.oograph/dialogs/InsertNodeDialog.xdl";
    private final XController mxController;
    private final XModel mxModel;
    private final MyDispatch myDispatch;
    private final XComponent m_xComponent;
    private final XComponentContext xContext;
    private final DiagramService diagramService;
    private InsertNodeDialog dialog;


    public InsertNodePanel(XFrame xFrame, XWindow xParentWindow, XComponentContext xContext, MyDispatch myDispatch1, XComponent m_xComponent, DiagramService diagramService) {
        mxController = xFrame.getController();
        mxModel = mxController.getModel();
        myDispatch = myDispatch1;
        this.diagramService = diagramService;
        this.m_xComponent = m_xComponent;
        this.xContext = xContext;
        XWindowPeer xParentPeer = UnoRuntime.queryInterface(XWindowPeer.class, xParentWindow);
        if (xParentPeer == null) {

            return;
        }


        // Create the dialog window.
        XContainerWindowProvider xProvider = ContainerWindowProvider.create(xContext);
        if (xProvider == null) {

            return;
        }

        try {
            mxWindow = xProvider.createContainerWindow(DIALOG_PATH, "", xParentPeer, this);
        } catch (Exception aException) {
            mxWindow = null;
        }
        if (mxWindow == null) {
            return;
        }

        // Add a window listener to get informed about disposing and size changes.
        mxWindow.addWindowListener(
                new XWindowListener2() {
                    @Override
                    public void disposing(EventObject arg0) {
                        mxWindow = null;
                    }

                    @Override
                    public void windowShown(EventObject arg0) {
                    }

                    @Override
                    public void windowResized(WindowEvent arg0) {

                    }

                    @Override
                    public void windowMoved(WindowEvent arg0) {
                    }

                    @Override
                    public void windowHidden(EventObject arg0) {
                    }

                    @Override
                    public void windowEnabled(EventObject arg0) {
                    }

                    @Override
                    public void windowDisabled(EventObject arg0) {
                    }
                }
        );
        mxWindow.setVisible(true);
        setupButtons();

    }

    @Override
    public boolean callHandlerMethod(XDialog xDialog, Object o, String s) throws WrappedTargetException {
        return false;  // empty body TODO
    }

    @Override
    public boolean callHandlerMethod(XWindow xWindow, Object o, String s) throws WrappedTargetException {
        return false;  // empty body TODO
    }

    @Override
    public String[] getSupportedMethodNames() {
        return new String[0];  // empty body TODO
    }

    class NodeButtonActionListener implements XActionListener {

        private final NodeFactory nodeFactory;
        private final Node.NodeType type;

        NodeButtonActionListener(NodeFactory nodeFactory, Node.NodeType type) {
            this.nodeFactory = nodeFactory;
            this.type = type;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            diagramService.insertNode(diagramService.createNode("", type));
        }

        @Override
        public void disposing(EventObject eventObject) {

        }
    }

    private void setupButtons() {
        NodeFactory nodeFactory = myDispatch.getNodeFactory();
        setupButton(new NodeButtonActionListener(nodeFactory, Node.NodeType.ClientPort), "clientNodeButton");
        setupButton(new NodeButtonActionListener(nodeFactory, Node.NodeType.ServerPort), "serverNodeButton");
        setupButton(new NodeButtonActionListener(nodeFactory, Node.NodeType.StartMethodOfProcess), "processNodeButton");
        setupButton(new NodeButtonActionListener(nodeFactory, Node.NodeType.MethodOfProcess), "procedureNodeButton");

    }

    private void setupButton(XActionListener xActionListener, String controlId) {
        final XControlContainer xControlContainer = (XControlContainer) UnoRuntime.queryInterface(XControlContainer.class, mxWindow);
        XControl clientNodeButtonControl = xControlContainer.getControl(controlId);
        XButton clientNodeButton = QI.XButton(clientNodeButtonControl);
        XControlModel model = clientNodeButtonControl.getModel();
        clientNodeButton.addActionListener(xActionListener);
    }

    public void setDialog(InsertNodeDialog dialog) {
        this.dialog = dialog;
    }

    @Override
    protected void Layout(Size aWindowSize) {
        // empty body TODO
    }

    @Override
    public XAccessible createAccessible(XAccessible arg0) {
        return (XAccessible) UnoRuntime.queryInterface(XAccessible.class, getWindow());


    }

    @Override
    public XWindow getWindow() {
        if (mxWindow == null) {
            throw new DisposedException("DemoPanel disposed", this);
        }

        return mxWindow;
    }

    @Override
    public void windowHidden(EventObject aEvent) {
        super.windowHidden(aEvent);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void windowMoved(WindowEvent aEvent) {
        super.windowMoved(aEvent);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void windowResized(WindowEvent aEvent) {
        super.windowResized(aEvent);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void windowShown(EventObject aEvent) {
        super.windowShown(aEvent);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public LayoutSize getHeightForWidth(int nWidth) {
        return new LayoutSize(
                // The height of this dialog is fixed.  Just return the value that was set during its construction.
                mxWindow.getPosSize().Height,
                // No upper bound.  The dialog can be enlarged until the bottom of the sidebar.
                -1,
                // No preferred size.
                0);
    }

    @Override
    public void disposing(EventObject aEvent) {
        super.disposing(aEvent);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void dispose() {
        super.dispose();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
