package ru.ssau.graphplus.gui;

import com.sun.star.awt.*;
import com.sun.star.beans.XMultiPropertySet;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNameContainer;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;


public class ChooseNodeTypeDialog {

    public short chooseNodeType(XMultiComponentFactory xMCF, final XShape xShape, XComponentContext m_xContext) {

            try {
                Object oDialogModel = xMCF.createInstanceWithContext("com.sun.star.awt.UnoControlDialogModel", m_xContext);

                // The XMultiServiceFactory of the dialogmodel is needed to instantiate the controls...
                XMultiServiceFactory m_xMSFDialogModel = UnoRuntime.queryInterface(XMultiServiceFactory.class, oDialogModel);

                // The named container is used to insert the created controls into...
                final XNameContainer m_xDlgModelNameContainer = UnoRuntime.queryInterface(XNameContainer.class, oDialogModel);

                // create the dialog...
                Object oUnoDialog = xMCF.createInstanceWithContext("com.sun.star.awt.UnoControlDialog", m_xContext);
                XControl m_xDialogControl = UnoRuntime.queryInterface(XControl.class, oUnoDialog);

                // The scope of the dialogControl container is public...
                final XControlContainer m_xDlgContainer = UnoRuntime.queryInterface(XControlContainer.class, oUnoDialog);

                XTopWindow m_xTopWindow = UnoRuntime.queryInterface(XTopWindow.class, m_xDlgContainer);

                // link the dialog and its model...
                XControlModel xControlModel = UnoRuntime.queryInterface(XControlModel.class, oDialogModel);
                m_xDialogControl.setModel(xControlModel);


                XPropertySet xPSetDialog = UnoRuntime.queryInterface(
                        XPropertySet.class, oDialogModel);
                xPSetDialog.setPropertyValue(
                        "PositionX", new Integer(10));
                xPSetDialog.setPropertyValue(
                        "PositionY", new Integer(500));
                xPSetDialog.setPropertyValue(
                        "Width", new Integer(200));
                xPSetDialog.setPropertyValue(
                        "Height", new Integer(70));

                Object toolkit = xMCF.createInstanceWithContext(
                        "com.sun.star.awt.ExtToolkit", m_xContext);
                XToolkit xToolkit = UnoRuntime.queryInterface(
                        XToolkit.class, toolkit);

                XWindow xWindow = UnoRuntime.queryInterface(
                        XWindow.class, m_xDialogControl);

                xWindow.setVisible(
                        false);

                m_xDialogControl.createPeer(xToolkit,
                        null);


                Object controlModel = xMCF.createInstanceWithContext("com.sun.star.awt.UnoControlListBoxModel", m_xContext);
                XMultiPropertySet xMPS = UnoRuntime.queryInterface(XMultiPropertySet.class, controlModel);
                xMPS.setPropertyValues(new String[]{"Dropdown", "Height", "Name", "StringItemList"}, new Object[]{Boolean.TRUE, new Integer(12), new String("nodeType"), new String[]{"ServerPort", "ClientPort", "StartMethodOfProcess", "MethodOfProcess"}});
                m_xDlgModelNameContainer.insertByName("nodeTypeListBox", xMPS);

                controlModel = xMCF.createInstanceWithContext("com.sun.star.awt.UnoControlButtonModel", m_xContext);
                xMPS = UnoRuntime.queryInterface(XMultiPropertySet.class, controlModel);
                xMPS.setPropertyValues(new String[]{"Height", "Label", "Name", "PositionX", "PositionY", "Width"}, new Object[]{new Integer(14), "Button", "chooseButton", new Integer(10), new Integer("1000"), new Integer(30)});
                m_xDlgModelNameContainer.insertByName("chooseNodeTypeButton", xMPS);
                XButton xButton = UnoRuntime.queryInterface(XButton.class, m_xDlgContainer.getControl("chooseNodeTypeButton"));
                xButton.addActionListener(new MyXActionListener(xShape, m_xDialogControl) {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        try {
                            Object nodeTypeListBox = m_xDlgModelNameContainer.getByName("nodeTypeListBox");
                            XControl nodeTypeListBox1 = m_xDlgContainer.getControl("nodeTypeListBox");
                            XListBox xListBox = UnoRuntime.queryInterface(XListBox.class, nodeTypeListBox1);
                            String selectedItem = xListBox.getSelectedItem();
                            System.out.println(selectedItem);


                            XPropertySet xShapeProps = UnoRuntime.queryInterface(XPropertySet.class, xShape);

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
