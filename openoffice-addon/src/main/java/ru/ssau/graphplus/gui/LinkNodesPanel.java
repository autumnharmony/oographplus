/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui;

import com.sun.star.accessibility.XAccessible;
import com.sun.star.awt.*;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.frame.XController;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.DisposedException;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.text.XText;
import com.sun.star.ui.LayoutSize;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import ru.ssau.graphplus.DiagramController;
import ru.ssau.graphplus.OOGraph;
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.gui.sidebar.PanelBase;


public class LinkNodesPanel extends PanelBase {

    public static final String A_NODE_COMBO_BOX = "aNodeComboBox";
    public static final String Z_NODE_COMBO_BOX = "zNodeComboBox";
    public static final String A_NODE_RESET_BUTTON = "aNodeResetButton";
    public static final String Z_NODE_RESET_BUTTON = "zNodeResetButton";
    public static final String LINK_BUTTON = "linkButton";
    private final DiagramController diagramController;
    private LinkNodesDialog linkNodesDialog;

    public LinkNodesDialog getLinkNodesDialog() {
        return linkNodesDialog;
    }

    public void setLinkNodesDialog(LinkNodesDialog linkNodesDialog) {
        this.linkNodesDialog = linkNodesDialog;
    }

    public LinkNodesPanel(XFrame xFrame, XWindow xParentWindow, XComponentContext xContext, DiagramController diagramController) {

        this.diagramController = diagramController;
        mxController = xFrame.getController();

        XWindowPeer xParentPeer = (XWindowPeer) UnoRuntime.queryInterface(XWindowPeer.class, xParentWindow);
        if (xParentPeer == null)
        {
            return;
        }

        // Create the dialog window.
        XContainerWindowProvider xProvider = ContainerWindowProvider.create(xContext);
        if (xProvider == null)
        {
            return;
        }

        try
        {
            mxWindow = xProvider.createContainerWindow(DIALOG_PATH, "", xParentPeer, this);
        }
        catch (Exception aException)
        {
            OOGraph.LOGGER.warning(aException.getLocalizedMessage());
            mxWindow = null;
        }
        if (mxWindow == null)
        {
            OOGraph.LOGGER.warning("LinkNodesPanel: could not create container window");
            return;
        }

        // Add a window listener to get informed about disposing and size changes.
        mxWindow.addWindowListener(
                new XWindowListener2()
                {
                    @Override public void disposing(EventObject arg0) { mxWindow = null; }
                    @Override public void windowShown(EventObject arg0) {}
                    @Override public void windowResized(WindowEvent arg0) { ProcessResize(); }
                    @Override public void windowMoved(WindowEvent arg0) {}
                    @Override public void windowHidden(EventObject arg0) {}
                    @Override public void windowEnabled(EventObject arg0) {}
                    @Override public void windowDisabled(EventObject arg0) {}
                }
        );
        mxWindow.setVisible(true);

        // Setup callbacks for the search and the replace buttons.
        ConnectToButtons();

    }

    private final static String DIALOG_PATH = "vnd.sun.star.extension://ru.ssau.graphplus.oograph/dialogs/LinkNodesDialog.xdl";
    private final static String CONTROL_NAME_BUTTON_SEARCH = "button_search";
    private final static String CONTROL_NAME_BUTTON_REPLACE = "button_replace";
    private final static String CONTROL_NAME_FIELD_SEARCH = "field_search";
    private final static String CONTROL_NAME_FIELD_REPLACE = "field_replace";



    /** Add an XActionListener each to the search and the replace button.
     */
    private void ConnectToButtons ()
    {
        final XControlContainer xControlContainer = (XControlContainer)UnoRuntime.queryInterface(XControlContainer.class, mxWindow);
        if (xControlContainer==null)
            ThrowRuntimeException("SearchAndReplacePanel can not get access to dialog");


        XComboBox xComboBox1 = QI.XCombobox(xControlContainer.getControl(A_NODE_COMBO_BOX));
        XComboBox xComboBox2 = QI.XCombobox(xControlContainer.getControl(Z_NODE_COMBO_BOX));
        setupComboBox(xComboBox1, new XTextListener() {
            @Override
            public void textChanged(TextEvent textEvent) {
                String string = QI.XTextComponent(textEvent.Source).getText();
                getLinkNodesDialog().setNodeA(string);
            }

            @Override
            public void disposing(EventObject eventObject) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        setupComboBox(xComboBox2, new XTextListener() {
            @Override
            public void textChanged(TextEvent textEvent) {
                String string = QI.XTextComponent(textEvent.Source).getText();
                getLinkNodesDialog().setNodeZ(string);
            }

            @Override
            public void disposing(EventObject eventObject) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });


        XButton xButton = QI.XButton(xControlContainer.getControl(A_NODE_RESET_BUTTON));
        xButton.addActionListener(new XActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                getLinkNodesDialog().resetNodeA();
            }

            @Override
            public void disposing(EventObject eventObject) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });


        XButton xButton2 = QI.XButton(xControlContainer.getControl(Z_NODE_RESET_BUTTON));
        xButton2.addActionListener(new XActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                getLinkNodesDialog().resetNodeZ();
            }

            @Override
            public void disposing(EventObject eventObject) {

            }
        });

//        XButton linkButton = QI.XButton(xControlContainer.getControl(LINK_BUTTON));
//        linkButton.addActionListener(new XActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent actionEvent) {
//                diagramController.linkNodes((NodeBase)getLinkNodesDialog().getaNode(), (NodeBase)getLinkNodesDialog().getzNode(), getLinkType());
//            }
//
//            @Override
//            public void disposing(EventObject eventObject) {
//                //To change body of implemented methods use File | Settings | File Templates.
//            }
//        });
    }

    private Link.LinkType getLinkType() {
        return Link.LinkType.ControlFlow;
    }


    private void setupComboBox(XComboBox xComboBox1, XTextListener xTextListener) {
        final XComboBox xComboBox = xComboBox1;

        XText xText = QI.XText(xComboBox);
        try {
            QI.XPropertySet(QI.XControl(xComboBox).getModel()).setPropertyValue("Autocomplete", Boolean.TRUE);
        } catch (UnknownPropertyException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (PropertyVetoException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalArgumentException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (WrappedTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        QI.XTextComponent(xComboBox).addTextListener(xTextListener);
        xComboBox.addItemListener(new XItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {

                String text = QI.XTextComponent(xComboBox).getText();
                QI.XControl(xComboBox).getModel();
            }

            @Override
            public void disposing(EventObject eventObject) {

            }
        });
        xComboBox.addActionListener(new XActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.out.println(actionEvent.ActionCommand);
            }

            @Override
            public void disposing(EventObject eventObject) {

            }
        });
    }


    /** Lookup the XControl object in the dialog for the given name.
     */
    private XControl GetControl (final String sControlName)
    {
        final XControlContainer xControlContainer = (XControlContainer)UnoRuntime.queryInterface(XControlContainer.class, mxWindow);
        if (xControlContainer == null)
        {

            return null;
        }
        else
            return xControlContainer.getControl(sControlName);
    }




    /** Look up a named control in the dialog, cast it to XTextComponent and return its text content.
     */
    private String GetTextFromTextComponent (final String sControlName)
    {
        final XTextComponent xSearchField = (XTextComponent)UnoRuntime.queryInterface(
                XTextComponent.class,
                GetControl(sControlName));
        if (xSearchField == null)
        {

            return null;
        }

        return xSearchField.getText();
    }




    /** Return the XWindow associated with the control that is specified by the given name.
     */
    private XWindow GetControlWindow (final String sControlName)
    {
        final XControl xControl = GetControl(sControlName);
        if (xControl == null)
            return null;
        else
            return (XWindow)UnoRuntime.queryInterface(
                    XWindow.class,
                    xControl.getPeer());
    }


    @Override
    protected void Layout(Size aWindowSize) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public XAccessible createAccessible(XAccessible arg0)
    {
        return (XAccessible)UnoRuntime.queryInterface(XAccessible.class, getWindow());
    }




    @Override
    public XWindow getWindow()
    {
        if (mxWindow == null)
        {
            throw new DisposedException("DemoPanel disposed", this);
        }

        return mxWindow;
    }




    /** Handle size changes of the dialog window by adapting the size of some of its controls.
     */
    private void ProcessResize ()
    {
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




    private void ThrowRuntimeException (final String sMessage)
    {

        throw new com.sun.star.uno.RuntimeException(sMessage);
    }




    /** The XSidebarPanel is an optional interface.  We do not really need it for this demo but is included
     *  to show how it works.
     */
    @Override
    public LayoutSize getHeightForWidth (final int nNewWidth)
    {
        return new LayoutSize(
                // The height of this dialog is fixed.  Just return the value that was set during its construction.
                mxWindow.getPosSize().Height,
                // No upper bound.  The dialog can be enlarged until the bottom of the sidebar.
                -1,
                // No preferred size.
                0);
    }

    private XWindow mxWindow;
    private XController mxController;

    @Override
    public boolean callHandlerMethod(XDialog xDialog, Object o, String s) throws WrappedTargetException {
        return linkNodesDialog.getDialogHandler().callHandlerMethod(xDialog, o, s);
    }

    @Override
    public boolean callHandlerMethod(XWindow xWindow, Object o, String s) throws WrappedTargetException {
        return linkNodesDialog.getDialogHandler().callHandlerMethod((XWindow) null, o ,s);
    }

    @Override
    public String[] getSupportedMethodNames() {
        return linkNodesDialog.getDialogHandler().getSupportedMethodNames();
    }
}
