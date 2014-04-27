
package ru.ssau.graphplus.gui;

import com.sun.star.awt.*;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNameContainer;
import com.sun.star.container.XNamed;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.*;
import com.sun.star.ui.dialogs.XExecutableDialog;
import com.sun.star.ui.dialogs.XFilePicker;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import ru.ssau.graphplus.commons.MiscHelper;
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.commons.ShapeHelper;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author 1
 */
public class Gui {

    private static final String _buttonName = "Button1";
    private static final String _cancelButtonName = "CancelButton";
    private static final String _labelName = "Label1";
    private static final String _labelPrefix = "Number of button clicks: ";
    private static final String _textfieldName = "TextField1";
    private static final String _textfieldName2 = "TextField2";//_textfieldName
    private static final String _checkboxName = "Checkbox1";
    private static final String _idLabelName = "idLabel";
    private static final String _typeLabelName = "typeLabel";

    public static void createDialog(final XNamed xNmd, XShape xShp, XComponentContext _xComponentContext, Map<String, XShape> elements) throws com.sun.star.uno.Exception {

        Object dialogModel = null;
        Object[] objs = onlyCreateDialog(xNmd, xShp, _xComponentContext, elements);
        Object dialog = objs[0];
        XDialog xDialog = (XDialog) UnoRuntime.queryInterface(XDialog.class, dialog);


        xDialog.execute();
        // dispose the dialog
        XComponent xComponent = (XComponent) UnoRuntime.queryInterface(
                XComponent.class, xDialog);

        xComponent.dispose();
    }

    public static void createDialogForShape(Object objShape, XComponentContext _xComponentContext, Map<String, XShape> elements) throws com.sun.star.uno.Exception {
        XShape xShape = (XShape) UnoRuntime.queryInterface(
                XShape.class, objShape);
        XNamed xNamed = (XNamed) UnoRuntime.queryInterface(
                XNamed.class, objShape);

        Object[] objs = onlyCreateDialog(xNamed, xShape, _xComponentContext, elements);
        Object dialog = objs[0];
        Object dialogModel = objs[1];
//        XController xContr = UnoRuntime.queryInterface(XController.class, dialog);
//        XModel xModel = xContr.getModel();
        XDialog xDialog = (XDialog) UnoRuntime.queryInterface(XDialog.class, dialog);

        XNameContainer xNameCont = (XNameContainer) UnoRuntime.queryInterface(
                XNameContainer.class, dialogModel);
        Object tb1 = xNameCont.getByName(_textfieldName);

        XPropertySet xPS = (XPropertySet) UnoRuntime.queryInterface(
                XPropertySet.class, tb1);
        xPS.setPropertyValue("Text", xNamed.getName());
//        xPS
        Object tb2 = xNameCont.getByName(_textfieldName2);
        xPS = (XPropertySet) UnoRuntime.queryInterface(
                XPropertySet.class, tb2);

        xPS.setPropertyValue("Text", MiscHelper.getUserDefinedAttributeValue(xShape, "AnotherField"));
        xDialog.execute();
        XComponent xComponent = (XComponent) UnoRuntime.queryInterface(
                XComponent.class, xDialog);

        xComponent.dispose();

    }

    public static Object[] onlyCreateDialog(final XNamed xNmd, XShape xShp, XComponentContext _xComponentContext, Map<String, XShape> elements) throws com.sun.star.uno.Exception {
        XMultiComponentFactory xMultiComponentFactory = _xComponentContext.getServiceManager();
        Object[] objs;
        // create the dialog model and set the properties
        Object dialogModel = xMultiComponentFactory.createInstanceWithContext(
                "com.sun.star.awt.UnoControlDialogModel", _xComponentContext);
        XPropertySet xPSetDialog = (XPropertySet) UnoRuntime.queryInterface(
                XPropertySet.class, dialogModel);
        xPSetDialog.setPropertyValue(
                "PositionX", new Integer(100));
        xPSetDialog.setPropertyValue(
                "PositionY", new Integer(100));
        xPSetDialog.setPropertyValue(
                "Width", new Integer(150));
        xPSetDialog.setPropertyValue(
                "Height", new Integer(100));


        xPSetDialog.setPropertyValue(
                "Title", new String("Properties " + MiscHelper.getGraphElementType(xShp)));

        // get the service manager from the dialog model
        XMultiServiceFactory xMultiServiceFactory = (XMultiServiceFactory) UnoRuntime.queryInterface(
                XMultiServiceFactory.class, dialogModel);

        // create the button model and set the properties
        Object buttonModel = xMultiServiceFactory.createInstance(
                "com.sun.star.awt.UnoControlButtonModel");
        MiscHelper.printInfo(buttonModel);
        XPropertySet xPSetButton = (XPropertySet) UnoRuntime.queryInterface(
                XPropertySet.class, buttonModel);

        xPSetButton.setPropertyValue("PositionX", new Integer(20));
        xPSetButton.setPropertyValue(
                "PositionY", new Integer(70));
        xPSetButton.setPropertyValue(
                "Width", new Integer(50));
        xPSetButton.setPropertyValue(
                "Height", new Integer(14));
        xPSetButton.setPropertyValue(
                "Name", _buttonName);
        xPSetButton.setPropertyValue(
                "TabIndex", new Short((short) 0));

        xPSetButton.setPropertyValue(
                "Label", new String("Ok"));


        // create the textbox model and set the properties

        Object editModel = xMultiServiceFactory.createInstance(
                "com.sun.star.awt.UnoControlEditModel");

        XTextComponent xTextComp; // = UnoRuntime.queryInterface(XTextComponent.class, editModel);

        XPropertySet xPSetEdit = (XPropertySet) UnoRuntime.queryInterface(
                XPropertySet.class, editModel);

        xPSetEdit.setPropertyValue("PositionX", new Integer(40));
        xPSetEdit.setPropertyValue(
                "PositionY", new Integer(30));
        xPSetEdit.setPropertyValue(
                "Width", new Integer(100));
        xPSetEdit.setPropertyValue(
                "Height", new Integer(14));
        xPSetEdit.setPropertyValue(
                "Name", _textfieldName);
        xPSetEdit.setPropertyValue(
                "TabIndex", new Short((short) 0));

        Object editModel2 = xMultiServiceFactory.createInstance(
                //                "com.sun.star.awt.UnoControlEditModel");
                "com.sun.star.awt.UnoControlEditModel");
//        XEd xButton = (XButton) UnoRuntime.queryInterface(
//                XButton.class, objectButton);
        XTextComponent xTextComp2; // = UnoRuntime.queryInterface(XTextComponent.class, editModel);

        XPropertySet xPSetEdit2 = (XPropertySet) UnoRuntime.queryInterface(
                XPropertySet.class, editModel2);

        xPSetEdit2.setPropertyValue("PositionX", new Integer(40));
        xPSetEdit2.setPropertyValue(
                "PositionY", new Integer(50));
        xPSetEdit2.setPropertyValue(
                "Width", new Integer(100));
        xPSetEdit2.setPropertyValue(
                "Height", new Integer(14));
        xPSetEdit2.setPropertyValue(
                "Name", _textfieldName2);
        xPSetEdit2.setPropertyValue(
                "TabIndex", new Short((short) 0));

        //Object propertyEdit

        //

        Object cbModel = xMultiServiceFactory.createInstance(
                "com.sun.star.awt.UnoControlCheckBoxModel");
        XPropertySet xPSetCheckBox = (XPropertySet) UnoRuntime.queryInterface(
                XPropertySet.class, cbModel);

        xPSetCheckBox.setPropertyValue("PositionX", new Integer(40));
        xPSetCheckBox.setPropertyValue(
                "PositionY", new Integer(50));

        xPSetCheckBox.setPropertyValue(
                "Name", _checkboxName);
        xPSetCheckBox.setPropertyValue(
                "TabIndex", new Short((short) 1));

        xPSetCheckBox.setPropertyValue(
                "Label", "??????????????????????????");


        // create a Cancel button model and set the properties
        Object cancelButtonModel = xMultiServiceFactory.createInstance(
                "com.sun.star.awt.UnoControlButtonModel");
        XPropertySet xPSetCancelButton = (XPropertySet) UnoRuntime.queryInterface(
                XPropertySet.class, cancelButtonModel);


        xPSetCancelButton.setPropertyValue("PositionX", new Integer(80));
        xPSetCancelButton.setPropertyValue(
                "PositionY", new Integer(70));
        xPSetCancelButton.setPropertyValue(
                "Width", new Integer(50));
        xPSetCancelButton.setPropertyValue(
                "Height", new Integer(14));
        xPSetCancelButton.setPropertyValue(
                "Name", _cancelButtonName);
        xPSetCancelButton.setPropertyValue(
                "TabIndex", new Short((short) 2));
        xPSetCancelButton.setPropertyValue(
                "PushButtonType", new Short((short) 2));
        xPSetCancelButton.setPropertyValue(
                "Label", new String("Cancel"));


        ///
        Object label1Model = xMultiServiceFactory.createInstance("com.sun.star.awt.UnoControlFixedTextModel");
        XPropertySet xPSetLabel1 = (XPropertySet) UnoRuntime.queryInterface(
                XPropertySet.class, label1Model);
        xPSetLabel1.setPropertyValue("PositionX", new Integer(10));
        xPSetLabel1.setPropertyValue("PositionY", new Integer(30));
        xPSetLabel1.setPropertyValue("Label", new String("ID"));
        xPSetLabel1.setPropertyValue("Name", _idLabelName);
        xPSetLabel1.setPropertyValue("Width", new Integer(20));
        xPSetLabel1.setPropertyValue(
                "Height", new Integer(14));


        Object label2Model = xMultiServiceFactory.createInstance("com.sun.star.awt.UnoControlFixedTextModel");
        XPropertySet xPSetLabel2 = (XPropertySet) UnoRuntime.queryInterface(
                XPropertySet.class, label2Model);
        xPSetLabel1.setPropertyValue("PositionX", new Integer(10));
        xPSetLabel1.setPropertyValue("PositionY", new Integer(50));
        xPSetLabel1.setPropertyValue("Label", new String("Type"));
        xPSetLabel1.setPropertyValue("Name", _typeLabelName);
        xPSetLabel1.setPropertyValue("Width", new Integer(20));
        xPSetLabel1.setPropertyValue(
                "Height", new Integer(14));


        // add the model to the NameContainer of the dialog model
        // insert the control models into the dialog model


        XNameContainer xNameCont = (XNameContainer) UnoRuntime.queryInterface(
                XNameContainer.class, dialogModel);

        xNameCont.insertByName(_buttonName, buttonModel);
        xNameCont.insertByName(_textfieldName, editModel);
        //xNameCont.insertByName(_checkboxName, cbModel);
        xNameCont.insertByName(_cancelButtonName, cancelButtonModel);
        xNameCont.insertByName(_textfieldName2, editModel2);
        xNameCont.insertByName(_idLabelName, label1Model);
        xNameCont.insertByName(_typeLabelName, label2Model);


        // create the dialog control and set the model
        Object dialog = xMultiComponentFactory.createInstanceWithContext(
                "com.sun.star.awt.UnoControlDialog", _xComponentContext);

        XControlContainer xControlCont = (XControlContainer) UnoRuntime.queryInterface(
                XControlContainer.class, dialog);

        XControl xControl = (XControl) UnoRuntime.queryInterface(
                XControl.class, dialog);
        XControlModel xControlModel = (XControlModel) UnoRuntime.queryInterface(
                XControlModel.class, dialogModel);

        xControl.setModel(xControlModel);
        // add an action listener to the button control

        Object objectButton = xControlCont.getControl("Button1");
        XButton xButton = (XButton) UnoRuntime.queryInterface(
                XButton.class, objectButton);
        XDialog xDialog = (XDialog) UnoRuntime.queryInterface(XDialog.class, dialog);

        xButton.addActionListener(new ActionListenerImpl(xControlCont, xNmd, xDialog,
                xShp));

        XControl xTextControl = (XControl) UnoRuntime.queryInterface(XControl.class, xControlCont.getControl(_textfieldName));
        xTextComp = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class, xTextControl);
        xTextComp.addTextListener(new TextListenerImpl(elements, buttonModel));


//        XControl xCBControl = xControlCont.getControl(_checkboxName);
//        XCheckBox xCB = UnoRuntime.queryInterface(XCheckBox.class, xCBControl);
//        xCB.addItemListener(
//                new XItemListener() {
//
//                    public void itemStateChanged(ItemEvent arg0) {
//                        System.out.println("auto");
//                    }
//
//                    public void disposing(com.sun.star.lang.EventObject arg0) {
//                        //throw new UnsupportedOperationException("Not supported yet.");
//                    }
//                });

// create a peer
        Object toolkit = xMultiComponentFactory.createInstanceWithContext(
                "com.sun.star.awt.ExtToolkit", _xComponentContext);
        XToolkit xToolkit = (XToolkit) UnoRuntime.queryInterface(
                XToolkit.class, toolkit);

        XWindow xWindow = (XWindow) UnoRuntime.queryInterface(
                XWindow.class, xControl);

        xWindow.setVisible(
                false);

        xControl.createPeer(xToolkit,
                null);


        objs = new Object[]{dialog, dialogModel};
        //outDialogModel = dialogModel;
        return objs;
    }

    //public static ActionListenerImpl actList = new ActionListenerImpl(null, null, null, null)
    public static class ActionListenerImpl
            implements XActionListener {

        private XControlContainer _xControlCont;
        private XNamed _xNamed;
        private XDialog _xDialog;
        private XShape _xShape;

        public ActionListenerImpl(XControlContainer xControlCont, XNamed xNamed, XDialog xDialog, XShape xShape) {
            _xControlCont = xControlCont;
            _xNamed = xNamed;
            _xDialog = xDialog;
            _xShape = xShape;
        }

        public void actionPerformed(ActionEvent arg0) {
            try {
                Object text = _xControlCont.getControl(_textfieldName);
                XTextComponent xTextComp = (XTextComponent) UnoRuntime.queryInterface(
                        XTextComponent.class, text);
                _xNamed.setName(xTextComp.getText());
                try {
                    //ShapeHelper.
                    ShapeHelper.addPortion(_xShape, xTextComp.getText(), false);
                } catch (com.sun.star.lang.IllegalArgumentException ex) {
                }


                XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, _xShape);
                //XPropertySet propertySet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xTextSection);

//                Object text2 = _xControlCont.getControl(_textfieldName2);
//                XTextComponent xTextComp2 = (XTextComponent) UnoRuntime.queryInterface(
//                        XTextComponent.class, text2);

                //           
                Object userDefinedAttrs = xPropSet.getPropertyValue("UserDefinedAttributes");
                XNameContainer container = (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class,
                        xPropSet.getPropertyValue("UserDefinedAttributes"));


                //            XNameContainer container = (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class,
                //                    userDefinedAttrs);
                //XNameConta
//                String name = "AnotherField";

//                AttributeData data = new AttributeData();
//                data.Type = name;
//                data.Value = xTextComp2.getText();
//                try {
//
////                    container.insertByName(data.Type, data);
//
//                } catch (com.sun.star.container.ElementExistException ex) {
//                    try {
//                        try {
////                            container.removeByName(data.Type);
//                        } catch (NoSuchElementException ex1) {
//                            Logger.getLogger(Misc.class.getName()).log(Level.SEVERE, null, ex1);
//                        }
//                        container.insertByName(data.Type, data);
//                    } catch (ElementExistException ex1) {
//                        Logger.getLogger(Gui.class.getName()).log(Level.SEVERE, null, ex1);
//                    }
//                }

                //container.insertByName("ShapeIsNode")
                xPropSet.setPropertyValue("UserDefinedAttributes", container);


                _xDialog.endExecute();

            } catch (PropertyVetoException ex) {
                Logger.getLogger(Gui.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnknownPropertyException ex) {
                Logger.getLogger(Gui.class.getName()).log(Level.SEVERE, null, ex);
            } catch (WrappedTargetException ex) {
                Logger.getLogger(Gui.class.getName()).log(Level.SEVERE, null, ex);
            } catch (com.sun.star.lang.IllegalArgumentException ex) {
                Logger.getLogger(Gui.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        public void disposing(com.sun.star.lang.EventObject arg0) {
            _xControlCont = null;
        }
    }

    public static class TextListenerImpl implements XTextListener {

        Map<String, XShape> elements = null;
        Object buttonModel = null;

        public TextListenerImpl(Map<String, XShape> e, Object buttonModel) {
            elements = e;
            this.buttonModel = buttonModel;
        }

        public boolean nameIsFree(String name) {
            for (Entry<String, XShape> ent : elements.entrySet()) {
                if (QI.XNamed(ent.getValue()).getName().equals(name)) {
                    return false;
                }

            }


            return true;
        }

        public void textChanged(TextEvent arg0) {
            XTextComponent xTextComp = (XTextComponent) UnoRuntime.queryInterface(
                    XTextComponent.class, arg0.Source);
            String t = xTextComp.getText();

            try {
                if (!nameIsFree(t)) {
                    QI.XPropertySet(buttonModel).setPropertyValue("Enabled", false);
                } else {
                    QI.XPropertySet(buttonModel).setPropertyValue("Enabled", true);
                }
            } catch (UnknownPropertyException ex) {
                Logger.getLogger(Gui.class.getName()).log(Level.SEVERE, null, ex);
            } catch (PropertyVetoException ex) {
                Logger.getLogger(Gui.class.getName()).log(Level.SEVERE, null, ex);
            } catch (com.sun.star.lang.IllegalArgumentException ex) {
                Logger.getLogger(Gui.class.getName()).log(Level.SEVERE, null, ex);
            } catch (WrappedTargetException ex) {
                Logger.getLogger(Gui.class.getName()).log(Level.SEVERE, null, ex);
            }
            MiscHelper.printInfo(xTextComp);
        }

        public void disposing(EventObject arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static String raiseSaveAsDialog(XMultiComponentFactory m_xMCF, XComponentContext m_xContext) {
        String sStorePath = "";
        XComponent xComponent = null;
        try {
            // the filepicker is instantiated with the global Multicomponentfactory...
            Object oFilePicker = m_xMCF.createInstanceWithContext("com.sun.star.ui.dialogs.FilePicker", m_xContext);
            XFilePicker xFilePicker = (XFilePicker) UnoRuntime.queryInterface(XFilePicker.class, oFilePicker);

            // the defaultname is the initially proposed filename..
//            xFilePicker.setDefaultName("MyExampleDocument");
//
//            // set the initial displaydirectory. In this example the user template directory is used
//            Object oPathSettings = m_xMCF.createInstanceWithContext("com.sun.star.util.PathSettings", m_xContext);
//            XPropertySet xPropertySet = (XPropertySet) com.sun.star.uno.UnoRuntime.queryInterface(XPropertySet.class, oPathSettings);
//            String sTemplateUrl = (String) xPropertySet.getPropertyValue("Template_writable");
//            xFilePicker.setDisplayDirectory(sTemplateUrl);
//
//            // set the filters of the dialog. The filternames may be retrieved from
//            // http://wiki.services.openoffice.org/wiki/Framework/Article/Filter
//            XFilterManager xFilterManager = (XFilterManager) UnoRuntime.queryInterface(XFilterManager.class, xFilePicker);
//            xFilterManager.appendFilter("OpenDocument Text Template", "writer8_template");
//            xFilterManager.appendFilter("OpenDocument Text", "writer8");

            // choose the template that defines the capabilities of the filepicker dialog
//            XInitialization xInitialize = (XInitialization) UnoRuntime.queryInterface(XInitialization.class, xFilePicker);
//            Short[] listAny = new Short[]{new Short(com.sun.star.ui.dialogs.TemplateDescription.FILESAVE_AUTOEXTENSION)};
//            xInitialize.initialize(listAny);

            // add a control to the dialog to add the extension automatically to the filename...
//            XFilePickerControlAccess xFilePickerControlAccess = (XFilePickerControlAccess) UnoRuntime.queryInterface(XFilePickerControlAccess.class, xFilePicker);
//            xFilePickerControlAccess.setValue(com.sun.star.ui.dialogs.ExtendedFilePickerElementIds.CHECKBOX_AUTOEXTENSION, (short) 0, new Boolean(true));

            xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, xFilePicker);

            // execute the dialog...
            XExecutableDialog xExecutable = (XExecutableDialog) UnoRuntime.queryInterface(XExecutableDialog.class, xFilePicker);
            short nResult = xExecutable.execute();

            // query the resulting path of the dialog...
            if (nResult == com.sun.star.ui.dialogs.ExecutableDialogResults.OK) {
                String[] sPathList = xFilePicker.getFiles();
                if (sPathList.length > 0) {
                    sStorePath = sPathList[0];
                }
            }

        } catch (com.sun.star.uno.Exception exception) {
            exception.printStackTrace();
        } finally {
            //make sure always to dispose the component and free the memory!
            if (xComponent != null) {
                xComponent.dispose();
            }
        }

        return sStorePath;
    }

    /**
     * shows an error messagebox
     *
     * @param _xParentWindowPeer the windowpeer of the parent window
     * @param _sTitle            the title of the messagebox
     * @param _sMessage          the message of the messagebox
     */
    public static void showErrorMessageBox(XWindowPeer _xParentWindowPeer, String _sTitle, String _sMessage, XMultiComponentFactory m_xMCF, XComponentContext m_xContext) {

        showMesageBox(MessageBoxType.ERRORBOX, _xParentWindowPeer, _sTitle, _sMessage, m_xMCF, m_xContext);
    }

    public static void showMesageBox(MessageBoxType messageBoxType, XWindowPeer _xParentWindowPeer, String _sTitle, String _sMessage, XMultiComponentFactory m_xMCF, XComponentContext m_xContext) {

        XComponent xComponent = null;
        try {
            Object oToolkit = m_xMCF.createInstanceWithContext("com.sun.star.awt.Toolkit", m_xContext);
            XMessageBoxFactory xMessageBoxFactory = (XMessageBoxFactory) UnoRuntime.queryInterface(XMessageBoxFactory.class, oToolkit);
            // rectangle may be empty if position is in the center of the parent peer
            Rectangle aRectangle = new Rectangle();
            XMessageBox xMessageBox = xMessageBoxFactory.createMessageBox(_xParentWindowPeer, messageBoxType, com.sun.star.awt.MessageBoxButtons.BUTTONS_OK, _sTitle, _sMessage);
            xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, xMessageBox);
            if (xMessageBox != null) {
                short nResult = xMessageBox.execute();
            }
        } catch (com.sun.star.uno.Exception ex) {
            ex.printStackTrace(System.out);
        } finally {
            //make sure always to dispose the component and free the memory!
            if (xComponent != null) {
                xComponent.dispose();
            }
        }

    }

    //======================
    public static Object[] onlyCreateDialog2(final XNamed xNmd, XShape xShp, XComponentContext _xComponentContext, Map<String, XShape> elements) throws com.sun.star.uno.Exception {
        XMultiComponentFactory xMultiComponentFactory = _xComponentContext.getServiceManager();
        Object[] objs;
        // create the dialog model and set the properties
        Object dialogModel = xMultiComponentFactory.createInstanceWithContext(
                "com.sun.star.awt.UnoControlDialogModel", _xComponentContext);
        XPropertySet xPSetDialog = (XPropertySet) UnoRuntime.queryInterface(
                XPropertySet.class, dialogModel);
        xPSetDialog.setPropertyValue(
                "PositionX", new Integer(100));
        xPSetDialog.setPropertyValue(
                "PositionY", new Integer(100));
        xPSetDialog.setPropertyValue(
                "Width", new Integer(150));
        xPSetDialog.setPropertyValue(
                "Height", new Integer(100));


        xPSetDialog.setPropertyValue(
                "Title", new String("Properties " + MiscHelper.getGraphElementType(xShp)));

        // get the service manager from the dialog model
        XMultiServiceFactory xMultiServiceFactory = (XMultiServiceFactory) UnoRuntime.queryInterface(
                XMultiServiceFactory.class, dialogModel);

        // create the button model and set the properties
        Object buttonModel = xMultiServiceFactory.createInstance(
                "com.sun.star.awt.UnoControlButtonModel");
        MiscHelper.printInfo(buttonModel);
        XPropertySet xPSetButton = (XPropertySet) UnoRuntime.queryInterface(
                XPropertySet.class, buttonModel);

        xPSetButton.setPropertyValue("PositionX", new Integer(20));
        xPSetButton.setPropertyValue(
                "PositionY", new Integer(70));
        xPSetButton.setPropertyValue(
                "Width", new Integer(50));
        xPSetButton.setPropertyValue(
                "Height", new Integer(14));
        xPSetButton.setPropertyValue(
                "Name", _buttonName);
        xPSetButton.setPropertyValue(
                "TabIndex", new Short((short) 0));

        xPSetButton.setPropertyValue(
                "Label", new String("Ok"));


        // create the textbox model and set the properties

        Object editModel = xMultiServiceFactory.createInstance(
                "com.sun.star.awt.UnoControlEditModel");

        XTextComponent xTextComp; // = UnoRuntime.queryInterface(XTextComponent.class, editModel);

        XPropertySet xPSetEdit = (XPropertySet) UnoRuntime.queryInterface(
                XPropertySet.class, editModel);

        xPSetEdit.setPropertyValue("PositionX", new Integer(40));
        xPSetEdit.setPropertyValue(
                "PositionY", new Integer(30));
        xPSetEdit.setPropertyValue(
                "Width", new Integer(100));
        xPSetEdit.setPropertyValue(
                "Height", new Integer(14));
        xPSetEdit.setPropertyValue(
                "Name", _textfieldName);
        xPSetEdit.setPropertyValue(
                "TabIndex", new Short((short) 0));


        //Object propertyEdit

        //

        Object cbModel = xMultiServiceFactory.createInstance(
                "com.sun.star.awt.UnoControlCheckBoxModel");
        XPropertySet xPSetCheckBox = (XPropertySet) UnoRuntime.queryInterface(
                XPropertySet.class, cbModel);

        xPSetCheckBox.setPropertyValue("PositionX", new Integer(40));
        xPSetCheckBox.setPropertyValue(
                "PositionY", new Integer(50));

        xPSetCheckBox.setPropertyValue(
                "Name", _checkboxName);
        xPSetCheckBox.setPropertyValue(
                "TabIndex", new Short((short) 1));

        xPSetCheckBox.setPropertyValue(
                "Label", "??????????????????????????");


        // create a Cancel button model and set the properties
        Object cancelButtonModel = xMultiServiceFactory.createInstance(
                "com.sun.star.awt.UnoControlButtonModel");
        XPropertySet xPSetCancelButton = (XPropertySet) UnoRuntime.queryInterface(
                XPropertySet.class, cancelButtonModel);


        xPSetCancelButton.setPropertyValue("PositionX", new Integer(80));
        xPSetCancelButton.setPropertyValue(
                "PositionY", new Integer(70));
        xPSetCancelButton.setPropertyValue(
                "Width", new Integer(50));
        xPSetCancelButton.setPropertyValue(
                "Height", new Integer(14));
        xPSetCancelButton.setPropertyValue(
                "Name", _cancelButtonName);
        xPSetCancelButton.setPropertyValue(
                "TabIndex", new Short((short) 2));
        xPSetCancelButton.setPropertyValue(
                "PushButtonType", new Short((short) 2));
        xPSetCancelButton.setPropertyValue(
                "Label", new String("Cancel"));


        ///
        Object label1Model = xMultiServiceFactory.createInstance("com.sun.star.awt.UnoControlFixedTextModel");
        XPropertySet xPSetLabel1 = (XPropertySet) UnoRuntime.queryInterface(
                XPropertySet.class, label1Model);
        xPSetLabel1.setPropertyValue("PositionX", new Integer(10));
        xPSetLabel1.setPropertyValue("PositionY", new Integer(30));
        xPSetLabel1.setPropertyValue("Label", new String("ID"));
        xPSetLabel1.setPropertyValue("Name", _idLabelName);
        xPSetLabel1.setPropertyValue("Width", new Integer(20));
        xPSetLabel1.setPropertyValue(
                "Height", new Integer(14));


        Object label2Model = xMultiServiceFactory.createInstance("com.sun.star.awt.UnoControlFixedTextModel");
        XPropertySet xPSetLabel2 = (XPropertySet) UnoRuntime.queryInterface(
                XPropertySet.class, label2Model);
        xPSetLabel1.setPropertyValue("PositionX", new Integer(10));
        xPSetLabel1.setPropertyValue("PositionY", new Integer(50));
        xPSetLabel1.setPropertyValue("Label", new String("Type"));
        xPSetLabel1.setPropertyValue("Name", _typeLabelName);
        xPSetLabel1.setPropertyValue("Width", new Integer(20));
        xPSetLabel1.setPropertyValue(
                "Height", new Integer(14));


        // add the model to the NameContainer of the dialog model
        // insert the control models into the dialog model


        XNameContainer xNameCont = (XNameContainer) UnoRuntime.queryInterface(
                XNameContainer.class, dialogModel);

        xNameCont.insertByName(_buttonName, buttonModel);
        xNameCont.insertByName(_textfieldName, editModel);
        //xNameCont.insertByName(_checkboxName, cbModel);
        xNameCont.insertByName(_cancelButtonName, cancelButtonModel);
//        xNameCont.insertByName(_textfieldName2, editModel2);
        xNameCont.insertByName(_idLabelName, label1Model);
        xNameCont.insertByName(_typeLabelName, label2Model);


        // create the dialog control and set the model
        Object dialog = xMultiComponentFactory.createInstanceWithContext(
                "com.sun.star.awt.UnoControlDialog", _xComponentContext);

        XControlContainer xControlCont = (XControlContainer) UnoRuntime.queryInterface(
                XControlContainer.class, dialog);

        XControl xControl = (XControl) UnoRuntime.queryInterface(
                XControl.class, dialog);
        XControlModel xControlModel = (XControlModel) UnoRuntime.queryInterface(
                XControlModel.class, dialogModel);

        xControl.setModel(xControlModel);
        // add an action listener to the button control

        Object objectButton = xControlCont.getControl("Button1");
        XButton xButton = (XButton) UnoRuntime.queryInterface(
                XButton.class, objectButton);
        XDialog xDialog = (XDialog) UnoRuntime.queryInterface(XDialog.class, dialog);

        xButton.addActionListener(new ActionListenerImpl(xControlCont, xNmd, xDialog,
                xShp));

        XControl xTextControl = (XControl) UnoRuntime.queryInterface(XControl.class, xControlCont.getControl(_textfieldName));
        xTextComp = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class, xTextControl);
        xTextComp.addTextListener(new TextListenerImpl(elements, buttonModel));


//        XControl xCBControl = xControlCont.getControl(_checkboxName);
//        XCheckBox xCB = UnoRuntime.queryInterface(XCheckBox.class, xCBControl);
//        xCB.addItemListener(
//                new XItemListener() {
//
//                    public void itemStateChanged(ItemEvent arg0) {
//                        System.out.println("auto");
//                    }
//
//                    public void disposing(com.sun.star.lang.EventObject arg0) {
//                        //throw new UnsupportedOperationException("Not supported yet.");
//                    }
//                });

// create a peer
        Object toolkit = xMultiComponentFactory.createInstanceWithContext(
                "com.sun.star.awt.ExtToolkit", _xComponentContext);
        XToolkit xToolkit = (XToolkit) UnoRuntime.queryInterface(
                XToolkit.class, toolkit);

        XWindow xWindow = (XWindow) UnoRuntime.queryInterface(
                XWindow.class, xControl);

        xWindow.setVisible(
                false);

        xControl.createPeer(xToolkit,
                null);


        objs = new Object[]{dialog, dialogModel};
        //outDialogModel = dialogModel;
        return objs;
    }

    public static void createDialogForShape2(Object objShape, XComponentContext _xComponentContext, Map<String, XShape> elements) throws com.sun.star.uno.Exception {
        XShape xShape = (XShape) UnoRuntime.queryInterface(
                XShape.class, objShape);
        XNamed xNamed = (XNamed) UnoRuntime.queryInterface(
                XNamed.class, objShape);


        Object[] objs = onlyCreateDialog2(xNamed, xShape, _xComponentContext, elements);
        Object dialog = objs[0];
        Object dialogModel = objs[1];
//        XController xContr = UnoRuntime.queryInterface(XController.class, dialog);
//        XModel xModel = xContr.getModel();
        XDialog xDialog = (XDialog) UnoRuntime.queryInterface(XDialog.class, dialog);

        XNameContainer xNameCont = (XNameContainer) UnoRuntime.queryInterface(
                XNameContainer.class, dialogModel);
        Object tb1 = xNameCont.getByName(_textfieldName);

        XPropertySet xPS = (XPropertySet) UnoRuntime.queryInterface(
                XPropertySet.class, tb1);
        xPS.setPropertyValue("Text", xNamed.getName());
//        xPS
//        Object tb2 = xNameCont.getByName(_textfieldName2);
//        xPS = (XPropertySet) UnoRuntime.queryInterface(
//                XPropertySet.class, tb2);

//        xPS.setPropertyValue("Text", Misc.getUserDefinedAttributeValue(xShape, "AnotherField"));
        xDialog.execute();
        XComponent xComponent = (XComponent) UnoRuntime.queryInterface(
                XComponent.class, xDialog);

        xComponent.dispose();

    }


}
