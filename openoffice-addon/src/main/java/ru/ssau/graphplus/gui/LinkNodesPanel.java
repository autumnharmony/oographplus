/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.sun.star.accessibility.XAccessible;
import com.sun.star.awt.*;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.frame.XController;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.*;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.ui.LayoutSize;
import com.sun.star.uno.*;
import com.sun.star.uno.RuntimeException;
import ru.ssau.graphplus.*;
import ru.ssau.graphplus.api.DiagramService;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.gui.sidebar.PanelBase;

import java.lang.Exception;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static ru.ssau.graphplus.MyDialogHandler.Event.event;


public class LinkNodesPanel extends PanelBase {

    public static final String A_NODE_COMBO_BOX = "aNodeComboBox";
    public static final String Z_NODE_COMBO_BOX = "zNodeComboBox";
    public static final String A_NODE_RESET_BUTTON = "aNodeResetButton";
    public static final String Z_NODE_RESET_BUTTON = "zNodeResetButton";
    public static final String LINK_BUTTON = "linkButton";
    private final static String DIALOG_PATH = "vnd.sun.star.extension://ru.ssau.graphplus.oograph/dialogs/LinkNodesDialog.xdl";
    private final MyDispatch dispatch;
    private final DiagramController diagramController;
    DiagramController.NodeSelectionListener nodeSelectionListener = new DiagramController.NodeSelectionListener() {
        @Override
        public void nodeSelected(Node node) {

            if (state.equals(State.NothingEntered)) {
                setComboText(aModel, node);
                setState(State.ANodeEntered);
                return;

            }
            if (state.equals(State.ANodeEntered)) {
                setComboText(zModel, node);
                setState(State.AZEntered);
                return;
            }

        }
    };
    private DiagramModel diagramModel;
    private Node aNode;
    private Node zNode;
    private XComboBox aNodeComboBox;
    private XComboBox zNodeComboBox;
    private XControlModel aModel;
    private XControlModel zModel;
    private PanelBase linkNodesPanel;
    private XControl xControl;
    private State state = State.NothingEntered;
    private XButton linkButton;
    private XControlModel linkButtonModel;
    private XControlModel aNodeResetButtonModel;
    private XControlModel zNodeResetButtonModel;
    private XControlModel linkTypeListBoxControlModel;
    private XListBox linkListBox;
    private XWindow mxWindow;
    private XController mxController;


    public LinkNodesPanel(XFrame xFrame, XWindow xParentWindow, XComponentContext xContext, MyDispatch myDispatch) {

        this.diagramController = myDispatch.getDiagramController();
        mxController = xFrame.getController();
        diagramModel = diagramController.getDiagramModel();

        dispatch = myDispatch;
        XWindowPeer xParentPeer = (XWindowPeer) UnoRuntime.queryInterface(XWindowPeer.class, xParentWindow);
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
            OOGraph.LOGGER.warning(aException.getLocalizedMessage());
            mxWindow = null;
        }
        if (mxWindow == null) {
            OOGraph.LOGGER.warning("LinkNodesPanel: could not create container window");
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
                        ProcessResize();
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

        // Setup callbacks for the search and the replace buttons.
        ConnectToButtons();

        init(this);

    }

    /**
     * Add an XActionListener each to the search and the replace button.
     */
    private void ConnectToButtons() {

    }

    private Map<String, Node> getNodeMap() {
        Map<String, Node> nodeMap = new HashMap<>();
        for (Node node : diagramModel.getNodes()) {
            nodeMap.put(Strings.isNullOrEmpty(node.getName()) ? node.getId() : node.getName(), node);
        }
        return nodeMap;
    }

    public Node getaNode() {
        return aNode;
    }

    public Node getzNode() {
        return zNode;
    }

    public void setNodeA(Node node) {
        aNode = node;
        QI.XTextComponent(aNodeComboBox).setText(node.getName());
    }

    public void setNodeZ(Node node) {
        zNode = node;
        QI.XTextComponent(zNodeComboBox).setText(node.getName());
    }

    public void resetNodeA() {
        QI.XTextComponent(aNodeComboBox).setText("");
        if (zNode == null) {
            setState(State.NothingEntered);
        } else {
            setState(State.ZNodeEntered);
        }

    }

    public void resetNodeZ() {

        QI.XTextComponent(zNodeComboBox).setText("");
        if (aNode == null) {
            setState(State.NothingEntered);
        } else {
            setState(State.ANodeEntered);
        }

    }

    private void setState(State state) {

        try {
            if (state.equals(State.AZEntered)) {
                setNodeZ();
                QI.XPropertySet(aModel).setPropertyValue("Enabled", Boolean.TRUE);
                QI.XPropertySet(zModel).setPropertyValue("Enabled", Boolean.TRUE);
                QI.XPropertySet(linkButtonModel).setPropertyValue("Enabled", Boolean.TRUE);
                QI.XPropertySet(aNodeResetButtonModel).setPropertyValue("Enabled", Boolean.TRUE);
                QI.XPropertySet(zNodeResetButtonModel).setPropertyValue("Enabled", Boolean.TRUE);
            }

            if (state.equals(State.ANodeEntered)) {
                setNodeA();
                QI.XPropertySet(aModel).setPropertyValue("Enabled", Boolean.TRUE);
                QI.XPropertySet(linkButtonModel).setPropertyValue("Enabled", Boolean.FALSE);
                QI.XPropertySet(aNodeResetButtonModel).setPropertyValue("Enabled", Boolean.TRUE);
            }


            this.state = state;

        } catch (UnknownPropertyException | PropertyVetoException | IllegalArgumentException | WrappedTargetException e) {
            throw new RuntimeException("error", e);
        }
    }

    public void setNodeA(String nodeName) {

        assert nodeName != null;
        aNode = getNodeMap().get(nodeName);

//        assert aNode != null;
        if (aNode == null) {
            zNode = null;
            setState(State.NothingEntered);
            return;
        }
        if (zNode != null) {
            setState(State.AZEntered);
        } else {
            setState(State.ANodeEntered);
        }
    }

    public void setNodeZ(String nodeName) {

        assert nodeName != null;
        zNode = getNodeMap().get(nodeName);

//        assert zNode != null;
        if (zNode == null) {
            aNode = null;
            setState(State.NothingEntered);
            return;
        }
        if (aNode != null) {
            setState(State.AZEntered);
        } else {
            setState(State.ZNodeEntered);
        }
    }

    public DiagramController.NodeSelectionListener getNodeSelectionListener() {
        return nodeSelectionListener;
    }

    private void setComboText(XControlModel model, Node node) {
        try {
            String name = node.getName();
            String item = Strings.isNullOrEmpty(name) ? node.getId() : node.getName();
            QI.XPropertySet(model).setPropertyValue("Text", item);
        } catch (UnknownPropertyException | PropertyVetoException | WrappedTargetException | IllegalArgumentException e) {
            e.printStackTrace();
            throw new com.sun.star.uno.RuntimeException("error", e);
        }
    }

    public MyDialogHandler getDialogHandler() {
        return new MyDialogHandler(Maps.newHashMap(ImmutableMap.<MyDialogHandler.Event, MyDialogHandler.EventHandler>builder()
                .put(event("aNodeSet"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        return setNodeA();
                    }
                })
                .put(event("zNodeSet"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        return setNodeZ();
                    }


                })
                .put(event("aNodeReset"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        resetNodeA();
                        return true;
                    }


                })
                .put(event("zNodeReset"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        resetNodeZ();
                        return true;
                    }
                })
                .put(event("aNodeComboboxExecute"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        return onANodeCBExecute();  //To change body of implemented methods use File | Settings | File Templates.
                    }
                })
                .put(event("zNodeComboboxExecute"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        return onZNodeCBExecute();
                    }
                })

                .put(event("zNodeComboboxTextModified"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        return zNodeTextModified();
                    }
                })
                .put(event("zNodeComboboxItemStatusChanged"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        return zNodeItemStatusChanged();
                    }
                })

                .put(event("aNodeComboboxTextModified"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        return aNodeTextModified();
                    }
                })
                .put(event("aNodeComboboxItemStatusChanged"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        return aNodeItemStatusChanged();
                    }
                })

                .put(event("zNodeReceivedFocus"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        zNodeComboBox.removeItems((short) 0, zNodeComboBox.getItemCount());
                        short i = 0;
                        zNodeComboBox.setDropDownLineCount((short) diagramModel.getNodes().size());
                        for (Node node : diagramModel.getNodes()) {
                            zNodeComboBox.addItem(Strings.isNullOrEmpty(node.getName()) ? node.getId() : node.getName(), i++);
                        }
                        return true;
                    }
                })


                .put(event("aNodeReceivedFocus"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        aNodeComboBox.removeItems((short) 0, aNodeComboBox.getItemCount());
                        short i = 0;
                        aNodeComboBox.setDropDownLineCount((short) diagramModel.getNodes().size());
                        for (Node node : diagramModel.getNodes()) {
                            aNodeComboBox.addItem(Strings.isNullOrEmpty(node.getName()) ? node.getId() : node.getName(), i++);
                        }
                        return true;
                    }
                })

                .put(event("linkButtonPerformAction"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        DiagramService diagramService = dispatch.getDiagramService();

                        Link link = diagramService.createLink("", Link.LinkType.valueOf(linkListBox.getSelectedItem()));
                        diagramService.insertLink(link);

                        try {
                            setNodeA(QI.XPropertySet(aModel).getPropertyValue("Text").toString());
                            setNodeZ(QI.XPropertySet(zModel).getPropertyValue("Text").toString());
                        } catch (UnknownPropertyException | WrappedTargetException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                        diagramService.linkNodes(getaNode(), getzNode(), link);
                        return true;
                    }
                })
                .build()));
    }

    private boolean aNodeItemStatusChanged() {
        return true;
    }

    private boolean aNodeTextModified() {

        return true;
    }

    private boolean zNodeItemStatusChanged() {

        // TODO
        return true;
    }

    private boolean zNodeTextModified() {

        // TODO
        return true;
    }

    private boolean onZNodeCBExecute() {
        //TODO
        return true;
    }

    private boolean onANodeCBExecute() {
        //TODO
        return true;
    }

    private boolean setNodeZ() {
        // TODO
        try {
            Object text = QI.XPropertySet(zModel).getPropertyValue("Text");
            Node node = getNodeMap().get(text);
            zNode = node;
        } catch (UnknownPropertyException | WrappedTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("error", e);
        }

        return true;

    }

    private boolean setNodeA() {

        return true;
    }

    public void init(LinkNodesPanel linkNodesPanel) {
        this.linkNodesPanel = linkNodesPanel;
        XWindow window = linkNodesPanel.getWindow();
        XControlContainer xControlCont = (XControlContainer) UnoRuntime.queryInterface(
                XControlContainer.class, window);

        xControl = UnoRuntime.queryInterface(XControl.class, linkNodesPanel);

        XControl aNodeComboBoxControl = xControlCont.getControl(A_NODE_COMBO_BOX);

        aNodeComboBox = QI.XCombobox(aNodeComboBoxControl);
        aModel = aNodeComboBoxControl.getModel();

        XControl aNodeResetButton = xControlCont.getControl(A_NODE_RESET_BUTTON);
        XControl zNodeResetButton = xControlCont.getControl(Z_NODE_RESET_BUTTON);

        try {
            aNodeResetButtonModel = aNodeResetButton.getModel();
            QI.XPropertySet(aNodeResetButtonModel).setPropertyValue("Enabled", Boolean.FALSE);
            zNodeResetButtonModel = zNodeResetButton.getModel();
            QI.XPropertySet(zNodeResetButtonModel).setPropertyValue("Enabled", Boolean.FALSE);
        } catch (UnknownPropertyException | WrappedTargetException | IllegalArgumentException | PropertyVetoException e) {
            throw new RuntimeException("error", e);
        }


        XControl zNodeComboBoxControl = xControlCont.getControl(Z_NODE_COMBO_BOX);
        zNodeComboBox = QI.XCombobox(zNodeComboBoxControl);
        zModel = zNodeComboBoxControl.getModel();


        XControl linkTypeListBoxControl = xControlCont.getControl("linkTypeListBox");
        linkTypeListBoxControlModel = linkTypeListBoxControl.getModel();
        linkListBox = QI.XListBox(linkTypeListBoxControl);
        short i = 0;
        for (Link.LinkType type : Link.LinkType.values()) {
            linkListBox.addItem(String.valueOf(type), i++);
        }


        linkButtonModel = xControlCont.getControl("linkButton").getModel();

        refreshNodes(diagramModel.getNodes());
        setState(State.NothingEntered);
    }


    private void refreshNodes(Collection<Node> nodes) {
        assert aModel != null;
        assert zModel != null;
        fillComboBox(QI.XItemList(aModel), nodes);
        fillComboBox(QI.XItemList(zModel), nodes);

    }

    private void fillComboBox(XItemList xItemList, Collection<Node> nodes) {
        int i = 0;
        for (Node node : nodes) {
            try {
                String name = node.getName();
                String item = Strings.isNullOrEmpty(name) ? node.getId() : node.getName();
                xItemList.insertItem(i++, item, item);
            } catch (com.sun.star.lang.IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }

    private Link.LinkType getLinkType() {
        return Link.LinkType.ControlFlow;
    }

    /**
     * Lookup the XControl object in the dialog for the given name.
     */
    private XControl GetControl(final String sControlName) {
        final XControlContainer xControlContainer = (XControlContainer) UnoRuntime.queryInterface(XControlContainer.class, mxWindow);
        if (xControlContainer == null) {

            return null;
        } else
            return xControlContainer.getControl(sControlName);
    }

    /**
     * Look up a named control in the dialog, cast it to XTextComponent and return its text content.
     */
    private String GetTextFromTextComponent(final String sControlName) {
        final XTextComponent xSearchField = (XTextComponent) UnoRuntime.queryInterface(
                XTextComponent.class,
                GetControl(sControlName));
        if (xSearchField == null) {

            return null;
        }

        return xSearchField.getText();
    }

    /**
     * Return the XWindow associated with the control that is specified by the given name.
     */
    private XWindow GetControlWindow(final String sControlName) {
        final XControl xControl = GetControl(sControlName);
        if (xControl == null)
            return null;
        else
            return (XWindow) UnoRuntime.queryInterface(
                    XWindow.class,
                    xControl.getPeer());
    }

    @Override
    protected void Layout(Size aWindowSize) {
        //To change body of implemented methods use File | Settings | File Templates.
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

    /**
     * Handle size changes of the dialog window by adapting the size of some of its controls.
     */
    private void ProcessResize() {
//        final Rectangle aWindowBox = mxWindow.getPosSize();
////        Log.Instance().printf("new window size is %dx%d\n", aWindowBox.Width, aWindowBox.Height);
//
//        // Get access to the search controls.  Try to shorten the text field to the width
//        // of the dialog (with border a both sides) but don't make it shorter than the button.
//        final XWindow aSearchButton = GetControlWindow(CONTROL_NAME_BUTTON_SEARCH);
//        final XWindow aSearchField = GetControlWindow(CONTROL_NAME_FIELD_SEARCH);
////        Log.Instance().printf("search controls are '%s' and '%s'\n", aSearchButton.toString(), aSearchField.toString());
//        if (aSearchButton!=null && aSearchField!=null)
//        {
//            final Rectangle aButtonBox = aSearchButton.getPosSize();
//            final Rectangle aFieldBox = aSearchField.getPosSize();
//            final int nNewWidth = Math.max(
//                    aButtonBox.Width,
//                    aWindowBox.Width - 2*aFieldBox.X);
//            aSearchField.setPosSize(0,0,nNewWidth,0, PosSize.WIDTH);
//        }
//
//        // Do the same for the replacement controls.
//        final XWindow aReplaceButton = GetControlWindow(CONTROL_NAME_BUTTON_REPLACE);
//        final XWindow aReplaceField = GetControlWindow(CONTROL_NAME_FIELD_REPLACE);
//        if (aReplaceButton!=null && aReplaceField!=null)
//        {
//            final Rectangle aButtonBox = aReplaceButton.getPosSize();
//            final Rectangle aFieldBox = aReplaceField.getPosSize();
//            final int nNewWidth = Math.max(
//                    aButtonBox.Width,
//                    aWindowBox.Width - 2*aFieldBox.X);
//            aReplaceField.setPosSize(0,0,nNewWidth,0, PosSize.WIDTH);
//        }
    }

    private void ThrowRuntimeException(final String sMessage) {

        throw new com.sun.star.uno.RuntimeException(sMessage);
    }

    /**
     * The XSidebarPanel is an optional interface.  We do not really need it for this demo but is included
     * to show how it works.
     */
    @Override
    public LayoutSize getHeightForWidth(final int nNewWidth) {
        return new LayoutSize(
                // The height of this dialog is fixed.  Just return the value that was set during its construction.
                mxWindow.getPosSize().Height,
                // No upper bound.  The dialog can be enlarged until the bottom of the sidebar.
                -1,
                // No preferred size.
                0);
    }

    @Override
    public boolean callHandlerMethod(XDialog xDialog, Object o, String s) throws WrappedTargetException {
        return getDialogHandler().callHandlerMethod(xDialog, o, s);
    }

    @Override
    public boolean callHandlerMethod(XWindow xWindow, Object o, String s) throws WrappedTargetException {
        return getDialogHandler().callHandlerMethod((XWindow) null, o, s);
    }

    @Override
    public String[] getSupportedMethodNames() {
        return getDialogHandler().getSupportedMethodNames();
    }

    private enum State {
        NothingEntered,
        ANodeEntered,
        ZNodeEntered,
        AZEntered
    }
}
