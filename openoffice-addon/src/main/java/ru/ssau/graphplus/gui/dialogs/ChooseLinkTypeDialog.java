package ru.ssau.graphplus.gui.dialogs;

import com.sun.star.awt.*;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XMultiPropertySet;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNameContainer;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.gui.MyXActionListener;
import ru.ssau.graphplus.link.LinkFactory;
import ru.ssau.graphplus.link.Linker;
import ru.ssau.graphplus.link.LinkerImpl;

public class ChooseLinkTypeDialog {
    private XMultiComponentFactory _xMCF;
    private XComponentContext m_xContext;
    private XMultiComponentFactory xMCF;
    private XShape xShape;
    private LinkFactory linkFactory;
    private XComponent m_xComponent;

    public ChooseLinkTypeDialog(XMultiComponentFactory _xMCF, XComponentContext m_xContext, XMultiComponentFactory xMCF, XShape xShape, LinkFactory linkFactory, XComponent m_xComponent) {
        this._xMCF = _xMCF;
        this.m_xContext = m_xContext;
        this.xMCF = xMCF;
        this.xShape = xShape;
        this.linkFactory = linkFactory;
        this.m_xComponent = m_xComponent;
    }

    public short chooseLinkType() {
        try {
            Object oDialogModel = _xMCF.createInstanceWithContext("com.sun.star.awt.UnoControlDialogModel", m_xContext);

            // The XMultiServiceFactory of the dialogmodel is needed to instantiate the controls...
            XMultiServiceFactory m_xMSFDialogModel = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, oDialogModel);

            // The named container is used to insert the created controls into...
            final XNameContainer m_xDlgModelNameContainer = (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class, oDialogModel);

            // create the dialog...
            Object oUnoDialog = _xMCF.createInstanceWithContext("com.sun.star.awt.UnoControlDialog", m_xContext);
            XControl m_xDialogControl = (XControl) UnoRuntime.queryInterface(XControl.class, oUnoDialog);

            // The scope of the dialogControl container is public...
            final XControlContainer m_xDlgContainer = (XControlContainer) UnoRuntime.queryInterface(XControlContainer.class, oUnoDialog);

            XTopWindow m_xTopWindow = (XTopWindow) UnoRuntime.queryInterface(XTopWindow.class, m_xDlgContainer);

            // link the dialog and its model...
            XControlModel xControlModel = (XControlModel) UnoRuntime.queryInterface(XControlModel.class, oDialogModel);
            m_xDialogControl.setModel(xControlModel);


            XPropertySet xPSetDialog = (XPropertySet) UnoRuntime.queryInterface(
                    XPropertySet.class, oDialogModel);
            xPSetDialog.setPropertyValue(
                    "PositionX", new Integer(100));
            xPSetDialog.setPropertyValue(
                    "PositionY", new Integer(100));
            xPSetDialog.setPropertyValue(
                    "Width", new Integer(200));
            xPSetDialog.setPropertyValue(
                    "Height", new Integer(70));


            Object toolkit = xMCF.createInstanceWithContext(
                    "com.sun.star.awt.ExtToolkit", m_xContext);
            XToolkit xToolkit = (XToolkit) UnoRuntime.queryInterface(
                    XToolkit.class, toolkit);

            XWindow xWindow = (XWindow) UnoRuntime.queryInterface(
                    XWindow.class, m_xDialogControl);

            xWindow.setVisible(
                    false);

            m_xDialogControl.createPeer(xToolkit,
                    null);


            Object controlModel = xMCF.createInstanceWithContext("com.sun.star.awt.UnoControlListBoxModel", m_xContext);
            XMultiPropertySet xMPS = (XMultiPropertySet) UnoRuntime.queryInterface(XMultiPropertySet.class, controlModel);
            xMPS.setPropertyValues(new String[]{"Dropdown", "Height", "Name", "StringItemList"}, new Object[]{Boolean.TRUE, new Integer(12), new String("linkType"), new String[]{"Link", "Control", "Message"}});
            m_xDlgModelNameContainer.insertByName("linkTypeListBox", xMPS);

            controlModel = xMCF.createInstanceWithContext("com.sun.star.awt.UnoControlButtonModel", m_xContext);
            xMPS = (XMultiPropertySet) UnoRuntime.queryInterface(XMultiPropertySet.class, controlModel);
            xMPS.setPropertyValues(new String[]{"Height", "Label", "Name", "PositionX", "PositionY", "Width"}, new Object[]{new Integer(14), "Button", "chooseButton", new Integer(10), new Integer("40"), new Integer(30)});
            m_xDlgModelNameContainer.insertByName("chooseLinkTypeButton", xMPS);
            XButton xButton = UnoRuntime.queryInterface(XButton.class, m_xDlgContainer.getControl("chooseLinkTypeButton"));
            xButton.addActionListener(new MyXActionListener(xShape, m_xDialogControl) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    try {
                        Object linkTypeListBox = m_xDlgModelNameContainer.getByName("linkTypeListBox");
                        XControl linkTypeListBox1 = m_xDlgContainer.getControl("linkTypeListBox");
                        XListBox xListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class, linkTypeListBox1);
                        String selectedItem = xListBox.getSelectedItem();
                        System.out.println(selectedItem);
                        XConnectorShape xConnectorShape = (XConnectorShape) UnoRuntime.queryInterface(XConnectorShape.class, xShape);
                        //XConnectorShape xConnectorShape = UnoRuntime.queryInterface(XConnectorShape.class, xShape);
                        XPropertySet xShapeProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xConnectorShape);
                        Object startShape = null;
                        Object endShape = null;
                        try {
                            startShape = xShapeProps.getPropertyValue("StartShape");
                            endShape = xShapeProps.getPropertyValue("EndShape");

                            XShape xShStart = (XShape) UnoRuntime.queryInterface(XShape.class, startShape);
                            XShape xShEnd = (XShape) UnoRuntime.queryInterface(XShape.class, endShape);
                            Link linkReplace = linkFactory.create(Link.LinkType.valueOf(selectedItem));
                            Linker linker = new LinkerImpl(linkReplace, xConnectorShape);
                            linker.link(xShStart, xShEnd);
                            dialogControl.dispose();

                        } catch (UnknownPropertyException e) {
                            e.printStackTrace();
                        } catch (WrappedTargetException e) {
                            e.printStackTrace();
                        }

                        //To change body of implemented methods use File | Settings | File Templates.


                    } catch (com.sun.star.container.NoSuchElementException e) {
                        e.printStackTrace();
                    } catch (WrappedTargetException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void disposing(com.sun.star.lang.EventObject eventObject) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }
            });

            XDialog xDialog = (XDialog) UnoRuntime.queryInterface(XDialog.class, m_xDialogControl);
            short executeResult = xDialog.execute();
            xDialog.endExecute();
            return executeResult;
        } catch (com.sun.star.uno.Exception exception) {
            exception.printStackTrace(System.out);
        }
        return 0;
    }
}
