/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.sun.star.accessibility.XAccessible;
import com.sun.star.awt.*;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.drawing.XShape;
import com.sun.star.frame.XController;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.*;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.ui.LayoutSize;
import com.sun.star.uno.RuntimeException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import ru.ssau.graphplus.*;
import ru.ssau.graphplus.api.DiagramService;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.events.Event;
import ru.ssau.graphplus.events.EventListener;
import ru.ssau.graphplus.events.NodeRemovedEvent;
import ru.ssau.graphplus.gui.sidebar.PanelBase;
import ru.ssau.graphplus.link.LinkAdjusterImpl;
import ru.ssau.graphplus.link.LinkBase;
import ru.ssau.graphplus.node.NodeBase;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static ru.ssau.graphplus.MyDialogHandler.Event.event;
import static ru.ssau.graphplus.api.Link.LinkType.*;


public class LinkNodesPanel extends PanelBase {

    public static final String A_NODE_COMBO_BOX = "aNodeComboBox";
    public static final String Z_NODE_COMBO_BOX = "zNodeComboBox";
    public static final String A_NODE_RESET_BUTTON = "aNodeResetButton";
    public static final String Z_NODE_RESET_BUTTON = "zNodeResetButton";
    public static final String LINK_BUTTON = "linkButton";
    private final static String DIALOG_PATH = "vnd.sun.star.extension://ru.ssau.graphplus.oograph/dialogs/LinkNodesDialog.xdl";
    private final MyDispatch dispatch;
    private final DiagramController diagramController;


    private DiagramController.NodeSelectionListener nodeSelectionListener = new DiagramController.NodeSelectionListener() {
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

    private XControlModel addTextModel;
    private XCheckBox addTextControl;
    private XControlContainer xControlCont;


    public LinkNodesPanel(XFrame xFrame, XWindow xParentWindow, XComponentContext xContext, MyDispatch myDispatch) {

        this.diagramController = myDispatch.getDiagramController();
        mxController = xFrame.getController();
        diagramModel = diagramController.getDiagramModel();
        diagramModel.addEventListener(NodeRemovedEvent.class, new EventListener() {
            @Override
            public void onEvent(Event event) {
                NodeRemovedEvent nodeRemovedEvent = (NodeRemovedEvent) event;
                Node node = nodeRemovedEvent.getNode();
                String item = !Strings.isNullOrEmpty(node.getName().trim()) ? node.getName() : node.getId();
                removeItemFromComboboxes(item);
            }
        });
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
//            OOGraph.LOGGER.warning(aException.getLocalizedMessage());
            mxWindow = null;
        }
        if (mxWindow == null) {
//            OOGraph.LOGGER.warning("LinkNodesPanel: could not create container window");
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


    private void removeItemFromComboboxes(String item) {
        removeItemFromCombobox(item, aNodeComboBox);
        removeItemFromCombobox(item, zNodeComboBox);
    }

    private void removeItemFromCombobox(String item, XComboBox aNodeComboBox) {
        XItemList xItemList = QI.XItemList(QI.XControl(aNodeComboBox).getModel());
        for (int i = 0; i < xItemList.getItemCount(); i++) {
            try {
                if (xItemList.getItemText(i).equals(item)) {
                    xItemList.removeItem(i);
                }
                break;
            } catch (IndexOutOfBoundsException e) {
                throw new RuntimeException("removeItemFromCombobox failed");
            }
        }
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

    private boolean resetNodeA() {
        aNode = null;
        QI.XTextComponent(aNodeComboBox).setText("");
        if (zNode == null) {
            setState(State.NothingEntered);
        } else {
            setState(State.ZNodeEntered);
        }
        return true;
    }

    private boolean resetNodeZ() {
        zNode = null;
        QI.XTextComponent(zNodeComboBox).setText("");
        if (aNode == null) {
            setState(State.NothingEntered);
        } else {
            setState(State.ANodeEntered);
        }
        return true;
    }

    private void setState(State state) {
        XControl statusControl = xControlCont.getControl("state");
        XFixedText xFixedText = UnoRuntime.queryInterface(XFixedText.class, statusControl);
        xFixedText.setText(state.toString());

        try {
            if (state.equals(State.AZEntered)) {
//                setNodeZ();

                QI.XPropertySet(linkButtonModel).setPropertyValue("Enabled", Boolean.TRUE);
                QI.XPropertySet(aNodeResetButtonModel).setPropertyValue("Enabled", Boolean.TRUE);
                QI.XPropertySet(zNodeResetButtonModel).setPropertyValue("Enabled", Boolean.TRUE);
            }

            if (state.equals(State.ANodeEntered)) {

                // wait for z
                setNodeA();
//                QI.XPropertySet(aModel).setPropertyValue("Enabled", Boolean.TRUE);
                QI.XPropertySet(linkButtonModel).setPropertyValue("Enabled", Boolean.FALSE);
                QI.XPropertySet(aNodeResetButtonModel).setPropertyValue("Enabled", Boolean.TRUE);
                QI.XPropertySet(zNodeResetButtonModel).setPropertyValue("Enabled", Boolean.FALSE);
            }
            if (state.equals(State.ZNodeEntered)) {

                // wait for a
                setNodeZ();
//                QI.XPropertySet(aModel).setPropertyValue("Enabled", Boolean.TRUE);
                QI.XPropertySet(linkButtonModel).setPropertyValue("Enabled", Boolean.FALSE);
                QI.XPropertySet(zNodeResetButtonModel).setPropertyValue("Enabled", Boolean.TRUE);
                QI.XPropertySet(aNodeResetButtonModel).setPropertyValue("Enabled", Boolean.FALSE);
            }

            if (state.equals(State.NothingEntered)) {

                QI.XPropertySet(aNodeResetButtonModel).setPropertyValue("Enabled", Boolean.FALSE);
                QI.XPropertySet(zNodeResetButtonModel).setPropertyValue("Enabled", Boolean.FALSE);
            }


            this.state = state;

        } catch (UnknownPropertyException | PropertyVetoException | IllegalArgumentException | WrappedTargetException e) {
            throw new RuntimeException("error", e);
        }
    }

    private void setNodeA(String nodeName) {

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

    private void setNodeZ(String nodeName) {

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
                        return resetNodeA();
//                        return true;
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
                        return onANodeCBExecute();
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

                        return nodeComboboxReceivedFocus(zNodeComboBox);
                    }
                })


                .put(event("aNodeReceivedFocus"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        return nodeComboboxReceivedFocus(aNodeComboBox);
                    }
                })

                .put(event("linkButtonPerformAction"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        System.out.println("linkButtonPerformAction");
                        DiagramService diagramService = dispatch.getDiagramService();
                        String s1 = getMap(Global.locale).inverse().get(linkListBox.getSelectedItem());

                        Link link = diagramService.createLink("", Link.LinkType.valueOf(s1));
                        diagramService.insertLink(link);

                        try {
                            setNodeA(QI.XPropertySet(aModel).getPropertyValue("Text").toString());
                            setNodeZ(QI.XPropertySet(zModel).getPropertyValue("Text").toString());
                        } catch (UnknownPropertyException | WrappedTargetException e) {
                            throw new java.lang.RuntimeException(e);
                        }
                        new LinkAdjusterImpl().adjustLink((LinkBase) link, (NodeBase) getaNode(), (NodeBase) getzNode());
                        diagramService.linkNodes(getaNode(), getzNode(), link);
                        return true;
                    }
                })

                .put(event("addTextShapeStatusChanged"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        boolean addTextToShapeToLink = addTextControl.getState() == 1;
                        Settings.getSettings().setAddTextToShapeToLink(addTextToShapeToLink);
                        return true;
                    }
                })
                .build()));
    }
    private boolean nodeComboboxReceivedFocus(XComboBox nodeComboBox) {
        nodeComboBox.removeItems((short) 0, nodeComboBox.getItemCount());
        short i = 0;
        nodeComboBox.setDropDownLineCount((short) diagramModel.getNodes().size());
        Collection<Node> nodes = diagramModel.getNodes();

        XComponent drawDoc = dispatch.getDrawDoc();
        Collection<Node> nodesFromCurrentPage = exclude(nodes, DrawHelper.getShapesByDrawPage(DrawHelper.getCurrentDrawPage(drawDoc)));


        for (Node node : nodesFromCurrentPage) {
            nodeComboBox.addItem(Strings.isNullOrEmpty(node.getName()) ? node.getId() : node.getName(), i++);
        }
        return true;
    }
    private Collection<Node> exclude(Collection<Node> nodes, final Collection<XShape> shapesByDrawPage) {
        return Collections2.filter(nodes, new Predicate<Node>() {
            @Override
            public boolean apply(Node input) {
                for (XShape shape : shapesByDrawPage){
                    if (input.getName().equals(QI.XText(shape).getString())){
                        return true;
                    }

                }
                return false;
            }
        });
    }

    private boolean aNodeItemStatusChanged() {
        setNodeA();
        return true;
    }

    private boolean aNodeTextModified() {
        setNodeA();

        return true;
    }

    private boolean zNodeItemStatusChanged() {
        setNodeZ();
        return true;
    }

    private boolean zNodeTextModified() {
        return true;
    }

    private boolean onZNodeCBExecute() {
        return true;
    }

    private boolean onANodeCBExecute() {
        return true;
    }

    private boolean setNodeZ() {
        try {
            Object text = QI.XPropertySet(zModel).getPropertyValue("Text");
            setNodeZ((String) text);
        } catch (UnknownPropertyException | WrappedTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("error", e);
        }

        return true;

    }

    BiMap<String, String> getMap(String locale){
        return HashBiMap.create(linkTypeHR.get(locale));
    }

    private Map<String, Map<String, String>> linkTypeHR = ImmutableMap.<String, Map<String, String>>builder()
            .put("ru", ImmutableMap.<String,String>builder()
                    .put(ControlFlow.toString(), "Поток управления")
                    .put(DataFlow.toString(), "Поток данных")
                    .put(MixedFlow.toString(), "Cмешанный поток").build())
            .put("en", ImmutableMap.<String,String>builder()
                    .put(ControlFlow.toString(), "Control flow")
                    .put(DataFlow.toString(), "Data flow")
                    .put(MixedFlow.toString(), "Mixed flow").build()).build();

    private Map<String, Map<String, String>> stringsHR = ImmutableMap.<String, Map<String, String>>builder()
            .put("ru", ImmutableMap.<String,String>builder()
                    .put(ControlFlow.toString(), "Поток управления")
                    .put(DataFlow.toString(), "Поток данных")
                    .put(MixedFlow.toString(), "Cмешанный поток").build())
            .put("en", ImmutableMap.<String,String>builder()
                    .put(ControlFlow.toString(), "Control flow")
                    .put(DataFlow.toString(), "Data flow")
                    .put(MixedFlow.toString(), "Mixed flow").build()).build();


    private boolean setNodeA() {
        try {
            Object text = QI.XPropertySet(aModel).getPropertyValue("Text");
            setNodeA((String) text);
        } catch (UnknownPropertyException | WrappedTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("error", e);
        }

        return true;
    }


    public void init(LinkNodesPanel linkNodesPanel) {
        this.linkNodesPanel = linkNodesPanel;
        XWindow window = linkNodesPanel.getWindow();
        xControlCont = UnoRuntime.queryInterface(
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
//        XItemList xItemList = QI.XItemList(linkListBox);
        short i = 0;
        BiMap<String, String> map = getMap(Global.locale);

        for (Link.LinkType type : Link.LinkType.values()) {

            linkListBox.addItem(
                    map.get(type.toString()), i++);
//            xItemList.insertItem(i++, );
        }


        linkButtonModel = xControlCont.getControl("linkButton").getModel();


        XControl addTextControl = xControlCont.getControl("addTextShapeToLink");
        this.addTextControl = QI.XCheckBox(addTextControl);
        addTextModel = addTextControl.getModel();

        boolean addTextToShapeToLink = Settings.getSettings().isAddTextToShapeToLink();
        this.addTextControl.setState((short) (addTextToShapeToLink ? 1 : 0));


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

    /**
     * Handle size changes of the dialog window by adapting the size of some of its controls.
     */
    private void ProcessResize() {

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
