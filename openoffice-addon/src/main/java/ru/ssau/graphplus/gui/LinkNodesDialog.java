/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui;

import com.google.common.collect.ImmutableMap;
import com.sun.star.awt.*;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.lang.*;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.view.XSelectionChangeListener;
import ru.ssau.graphplus.*;
import ru.ssau.graphplus.gui.sidebar.PanelBase;
import ru.ssau.graphplus.api.Node;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;


public class LinkNodesDialog implements MyDialog {

    public static final String A_NODE_COMBO_BOX = "aNodeComboBox";
    public static final String Z_NODE_COMBO_BOX = "zNodeComboBox";
    private DiagramModel diagramModel;
    private Node aNode;
    private Node zNode;
    private XComboBox aNodeComboBox;
    private XComboBox zNodeComboBox;
    private XControlModel aModel;
    private XControlModel zModel;
    private PanelBase linkNodesPanel;
    private XControl xControl;
    private final Collection<Node> allNodes;
    private Map<String,Node> nodeMap;


    public Node getaNode() {
        return aNode;
    }

    public Node getzNode() {
        return zNode;
    }

    public void setNodeA(Node node){
        aNode = node;
        QI.XTextComponent(aNodeComboBox).setText(node.getName());
    }


    public void setNodeZ(Node node){
        zNode = node;
        QI.XTextComponent(zNodeComboBox).setText(node.getName());
    }

    public static WeakHashMap<MyDispatch, LinkNodesDialog> map = new WeakHashMap<>();

    public LinkNodesDialog(MyDispatch myDispatch1) {

        map.put(myDispatch1, this);
        diagramModel = myDispatch1.getDiagramModel();
        allNodes = diagramModel.getNodes();

        nodeMap = new HashMap<>();
        for (Node node : allNodes) {
            nodeMap.put(node.getName(), node);
        }

    }

    public void resetNodeA() {
        QI.XTextComponent(aNodeComboBox).setText("");
        if (zNode == null){
            setState(State.NothingEntered);
        }
        else
        {
            setState(State.ZNodeEntered);
        }

    }

    public void resetNodeZ() {

        QI.XTextComponent(zNodeComboBox).setText("");
        if (aNode == null){
            setState(State.NothingEntered);
        }
        else
        {
            setState(State.ANodeEntered);
        }

    }


    private enum State {
        NothingEntered,
        ANodeEntered,
        ZNodeEntered,
        AZEntered
    }

    private State state;

    private void setState(State state){

        if (state.equals(State.AZEntered)){
            try {
                QI.XPropertySet(aModel).setPropertyValue("Visible", Boolean.TRUE);
                QI.XPropertySet(zModel).setPropertyValue("Visible", Boolean.TRUE);
            } catch (UnknownPropertyException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (PropertyVetoException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalArgumentException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (WrappedTargetException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public void setNodeA(String nodeName){

        assert nodeName != null;
        aNode = nodeMap.get(nodeName);

        assert aNode != null;
        if (zNode != null) {
            setState(State.AZEntered);
        }
        else {
            setState(State.ANodeEntered);
        }
    }


    public void setNodeZ(String nodeName){

        assert nodeName != null;
        zNode = nodeMap.get(nodeName);

        assert zNode != null;
        if (aNode != null) {
            setState(State.AZEntered);
        }
        else {
            setState(State.ZNodeEntered);
        }
    }

    DiagramController.NodeSelectionListener nodeSelectionListener = new DiagramController.NodeSelectionListener() {
        @Override
        public void nodeSelected(Node node) {
            if (state.equals(State.NothingEntered)){
                setComboText(aModel, node);
            }
            if (state.equals(State.ANodeEntered)){
                setComboText(zModel, node);
            }
        }
    };

    public DiagramController.NodeSelectionListener getNodeSelectionListener() {
        return nodeSelectionListener;
    }

    private void setComboText(XControlModel model, Node node){
        try {
            QI.XPropertySet(model).setPropertyValue("Text", node.getName());
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
      return new MyDialogHandler(ImmutableMap.<MyDialogHandler.Event, MyDialogHandler.EventHandler>builder()
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
//              .put(MyDialogHandler.Event.event(""))
                .build());
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

    XSelectionChangeListener xSelectionChangeListener;

    public LinkNodesDialog(DiagramModel diagramModel, MyDispatch myDispatch1) {
        this.diagramModel = diagramModel;
        if (diagramModel == null) this.diagramModel = myDispatch1.getDiagramModel();
        allNodes = diagramModel.getNodes();

    }

    private boolean onZNodeSet() {
        // TODO
        try {
            Object text = QI.XPropertySet(zModel).getPropertyValue("Text");
            Node node = nodeMap.get(text);
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
            QI.XPropertySet(zModel).setPropertyValue("Text","");
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
        XControlContainer xControlCont = ( XControlContainer ) UnoRuntime.queryInterface(
                XControlContainer.class, window);

        xControl = UnoRuntime.queryInterface(XControl.class, linkNodesPanel);

        XControl aNodeComboBoxControl = xControlCont.getControl(A_NODE_COMBO_BOX);

        aNodeComboBox = UnoRuntime.queryInterface(XComboBox.class, aNodeComboBoxControl);
        aModel = aNodeComboBoxControl.getModel();



        XControl zNodeComboBoxControl = xControlCont.getControl(Z_NODE_COMBO_BOX);
        zNodeComboBox = UnoRuntime.queryInterface(XComboBox.class, zNodeComboBoxControl);
        zModel = zNodeComboBoxControl.getModel();


        fillComboBoxes(allNodes);

        setState(State.NothingEntered);



    }

    private void fillComboBoxes(Collection<Node> nodes) {
        assert aModel != null;
        assert zModel != null;
        fillComboBox(QI.XItemList(aModel), nodes);
        fillComboBox(QI.XItemList(zModel), nodes);
    }

    private void fillComboBox(XItemList xItemList, Collection<Node> nodes) {
        int i = 0;
        for (Node node : nodes){
            try {
                xItemList.insertItem(i++, node.getName(), node.getName());
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }
}
