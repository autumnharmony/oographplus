/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.sun.star.awt.*;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.UnoRuntime;
import ru.ssau.graphplus.*;
import ru.ssau.graphplus.api.DiagramService;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.events.*;
import ru.ssau.graphplus.gui.sidebar.PanelBase;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;


public class LinkNodesDialog implements MyDialog {

    public static final String A_NODE_COMBO_BOX = "aNodeComboBox";
    public static final String Z_NODE_COMBO_BOX = "zNodeComboBox";
    public static final String LINK_BUTTON = "linkButton";
    public static WeakHashMap<MyDispatch, LinkNodesDialog> map = new WeakHashMap<>();
    private final Collection<Node> allNodes;
    private final MyDispatch dispatch;

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


    public LinkNodesDialog(final DiagramModel diagramModel, MyDispatch myDispatch1) {
        this.diagramModel = diagramModel;
        if (diagramModel == null) this.diagramModel = myDispatch1.getDiagramModel();
        allNodes = diagramModel.getNodes();
        dispatch = myDispatch1;
        diagramModel.addEventListener(NodeAddedEvent.class, new EventListener() {
            @Override
            public void onEvent(Event event) {
                NodeAddedEvent nodeAddedEvent = (NodeAddedEvent) event;
                aNodeComboBox.setDropDownLineCount((short) (aNodeComboBox.getItemCount() + 1));
                Node node = nodeAddedEvent.getNode();
                String name = node.getName();
                String item = Strings.isNullOrEmpty(name) ? node.getId() : node.getName();
                aNodeComboBox.addItem(item, (short) (aNodeComboBox.getItemCount() - 1));
            }
        });

        diagramModel.addEventListener(NodeRemovedEvent.class, new EventListener() {
            @Override
            public void onEvent(Event event) {
                NodeRemovedEvent nodeRemovedEvent = (NodeRemovedEvent) event;
                removeComboboxItem(nodeRemovedEvent, aNodeComboBox);
                removeComboboxItem(nodeRemovedEvent, zNodeComboBox);
            }

            private void removeComboboxItem(NodeRemovedEvent nodeRemovedEvent, XComboBox comboBox) {
                short i = 0;
                for (i = 0; i < comboBox.getItemCount(); i++) {
                    String item = comboBox.getItem((short) i);

                    if (item.equals(nodeRemovedEvent.getNode().getName())) {
                        break;
                    }
                }
                comboBox.removeItems(i, (short) 1);
            }
        });

        diagramModel.addEventListener(NodeModifiedEvent.class, new EventListener() {
            @Override
            public void onEvent(Event event) {
                clearComboBoxes();
                refreshNodes(diagramModel.getNodes());
            }

            private void clearComboBoxes() {
                clearComboBox(aNodeComboBox);
                clearComboBox(zNodeComboBox);
            }

            private void clearComboBox(XComboBox comboBox) {
                comboBox.removeItems((short) 0, comboBox.getItemCount());
            }
        });

    }

    private Map<String, Node> getNodeMap() {
        Map<String, Node> nodeMap = new HashMap<>();
        for (Node node : diagramModel.getNodes()) {
            nodeMap.put(Strings.isNullOrEmpty(node.getName())? node.getId() : node.getName() , node);
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

    private MyDispatch getDispatch() {
        return dispatch;
    }

    private void setState(State state) {

        try {
            if (state.equals(State.AZEntered)) {
                onZNodeSet();
                QI.XPropertySet(aModel).setPropertyValue("Enabled", Boolean.TRUE);
                QI.XPropertySet(zModel).setPropertyValue("Enabled", Boolean.TRUE);
                QI.XPropertySet(linkButtonModel).setPropertyValue("Enabled", Boolean.TRUE);
                QI.XPropertySet(aNodeResetButtonModel).setPropertyValue("Enabled", Boolean.TRUE);
                QI.XPropertySet(zNodeResetButtonModel).setPropertyValue("Enabled", Boolean.TRUE);
            }

            if (state.equals(State.ANodeEntered)) {
                onANodeSet();
                QI.XPropertySet(aModel).setPropertyValue("Enabled", Boolean.TRUE);

//                QI.XPropertySet(resetNodeAModel).setPropertyValue("Enabled", Boolean.TRUE);
                QI.XPropertySet(linkButtonModel).setPropertyValue("Enabled", Boolean.FALSE);
                QI.XPropertySet(aNodeResetButtonModel).setPropertyValue("Enabled", Boolean.TRUE);
            }


            this.state = state;

        } catch (UnknownPropertyException | PropertyVetoException | IllegalArgumentException | WrappedTargetException e) {
            throw new RuntimeException(e);
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
        } catch (UnknownPropertyException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (PropertyVetoException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (com.sun.star.lang.IllegalArgumentException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (WrappedTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public MyDialogHandler getDialogHandler() {
        return new MyDialogHandler(Maps.newHashMap(ImmutableMap.<MyDialogHandler.Event, MyDialogHandler.EventHandler>builder()
                .put(
                        MyDialogHandler.Event.event("aNodeSet"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        return onANodeSet();
                    }

                })
                .put(
                        MyDialogHandler.Event.event("zNodeSet"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        return onZNodeSet();
                    }


                })
                .put(
                        MyDialogHandler.Event.event("aNodeReset"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        return onANodeReset();
                    }


                })
                .put(MyDialogHandler.Event.event("zNodeReset"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        return onZNodeReset();
                    }
                })
                .put(MyDialogHandler.Event.event("aNodeComboboxExecute"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        return onANodeCBExecute();  //To change body of implemented methods use File | Settings | File Templates.
                    }
                })
                .put(MyDialogHandler.Event.event("zNodeComboboxExecute"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        return onZNodeCBExecute();
                    }
                })

                .put(MyDialogHandler.Event.event("zNodeComboboxTextModified"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        return zNodeTextModified();
                    }
                })
                .put(MyDialogHandler.Event.event("zNodeComboboxItemStatusChanged"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        return zNodeItemStatusChanged();
                    }
                })
                .put(MyDialogHandler.Event.event("zNodeReceivedFocus"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        zNodeComboBox.removeItems((short) 0, zNodeComboBox.getItemCount());
                        short i = 0;
                        zNodeComboBox.setDropDownLineCount((short) diagramModel.getNodes().size());
                        for (Node node : diagramModel.getNodes()) {
                            zNodeComboBox.addItem(node.getName(), i++);
                        }
                        return true;
                    }
                })
                        // TODO refactor copy paste
                .put(MyDialogHandler.Event.event("aNodeReceivedFocus"), new MyDialogHandler.EventHandler() {
                    @Override
                    public boolean handle(XDialog xDialog, Object o, String s) {
                        aNodeComboBox.removeItems((short) 0, aNodeComboBox.getItemCount());
                        short i = 0;
                        aNodeComboBox.setDropDownLineCount((short) diagramModel.getNodes().size());
                        for (Node node : diagramModel.getNodes()) {
                            aNodeComboBox.addItem(node.getName(), i++);
                        }
                        return true;
                    }
                })
                .build()));
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

    private boolean onZNodeSet() {
        // TODO
        try {
            Object text = QI.XPropertySet(zModel).getPropertyValue("Text");
            Node node = getNodeMap().get(text);
            zNode = node;
        } catch (UnknownPropertyException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (WrappedTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return true;

    }

    private boolean onANodeSet() {

        return true;
    }

    private boolean onZNodeReset() {
        try {
            QI.XPropertySet(zModel).setPropertyValue("Text", "");
        } catch (UnknownPropertyException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (PropertyVetoException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalArgumentException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (WrappedTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return true;


    }

    private boolean onANodeReset() {
        // TODO
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

        XControl aNodeResetButton = xControlCont.getControl("aNodeResetButton");
        XControl zNodeResetButton = xControlCont.getControl("zNodeResetButton");

        try {
            aNodeResetButtonModel = aNodeResetButton.getModel();
            QI.XPropertySet(aNodeResetButtonModel).setPropertyValue("Enabled", Boolean.FALSE);
            zNodeResetButtonModel = zNodeResetButton.getModel();
            QI.XPropertySet(zNodeResetButtonModel).setPropertyValue("Enabled", Boolean.FALSE);
        } catch (UnknownPropertyException | WrappedTargetException | IllegalArgumentException | PropertyVetoException e) {
            throw new RuntimeException(e);
        }


        XControl zNodeComboBoxControl = xControlCont.getControl(Z_NODE_COMBO_BOX);
        zNodeComboBox = QI.XCombobox(zNodeComboBoxControl);
        zModel = zNodeComboBoxControl.getModel();


        XControl linkTypeListBoxControl = xControlCont.getControl("linkTypeListBox");
        linkTypeListBoxControlModel = linkTypeListBoxControl.getModel();
        final XListBox linkListBox = QI.XListBox(linkTypeListBoxControl);
        short i = 0;
        for (Link.LinkType type : Link.LinkType.values()) {
            linkListBox.addItem(String.valueOf(type), i++);
        }

        XControl buttonControl = xControlCont.getControl(LINK_BUTTON);

        linkButton = QI.XButton(buttonControl);
        linkButtonModel = buttonControl.getModel();

        linkButton.addActionListener(new XActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                DiagramService diagramService = dispatch.getDiagramService();

                Link link = diagramService.createLink("",  Link.LinkType.valueOf(linkListBox.getSelectedItem()));

                diagramService.insertLink(link);
                diagramService.linkNodes(aNode, zNode, link);
            }

            @Override
            public void disposing(EventObject eventObject) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });


        refreshNodes(allNodes);
        setupCompoBoxes();

        setState(State.NothingEntered);

    }

    private void setupCompoBoxes() {
        aNodeComboBox.addActionListener(new XActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.out.println(actionEvent.ActionCommand);
            }

            @Override
            public void disposing(EventObject eventObject) {

            }
        });

        zNodeComboBox.addActionListener(new XActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.out.println(actionEvent.ActionCommand);

            }

            @Override
            public void disposing(EventObject eventObject) {

            }
        });
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
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }

    private enum State {
        NothingEntered,
        ANodeEntered,
        ZNodeEntered,
        AZEntered
    }
}
