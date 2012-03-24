package ru.ssau.graphplus;

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.ItemEvent;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XCheckBox;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XToolkit;
import com.sun.star.awt.XWindow;
import com.sun.star.beans.XMultiPropertySet;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNameContainer;
import com.sun.star.container.XNamed;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XShape;
import com.sun.star.frame.XController;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.text.ControlCharacter;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextRange;
import com.sun.star.uno.Type;
import com.sun.star.util.XModifiable;
import com.sun.star.util.XModifyListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public final class AddOn4 extends WeakBase
        implements com.sun.star.lang.XInitialization,
        com.sun.star.frame.XDispatch,
        com.sun.star.lang.XServiceInfo,
        com.sun.star.frame.XDispatchProvider {

    private final XComponentContext m_xContext;
    private com.sun.star.frame.XFrame m_xFrame;
    private static final String m_implementationName = AddOn4.class.getName();
    private static final String[] m_serviceNames = {
        "com.sun.star.frame.ProtocolHandler"};
    static com.sun.star.lang.XComponent xDrawDoc = null;
    com.sun.star.drawing.XDrawPage xDrawPage = null;
    com.sun.star.lang.XMultiComponentFactory xMCF = null;
    XMultiServiceFactory xMSF = null;
    int count = 0;
    public volatile String lastEnteredName = "";
    private boolean vertexAutoNaming = false;
    private boolean edgeAutoNaming = false;

    public AddOn4(XComponentContext context) {
        m_xContext = context;
    }

    public static XSingleComponentFactory __getComponentFactory(String sImplementationName) {
        XSingleComponentFactory xFactory = null;

        if (sImplementationName.equals(m_implementationName)) {
            xFactory = Factory.createComponentFactory(AddOn4.class, m_serviceNames);
        }
        return xFactory;
    }

    public static boolean __writeRegistryServiceInfo(XRegistryKey xRegistryKey) {
        return Factory.writeRegistryServiceInfo(m_implementationName,
                m_serviceNames,
                xRegistryKey);
    }

    // com.sun.star.lang.XInitialization:
    public void initialize(Object[] object)
            throws com.sun.star.uno.Exception {
        if (object.length > 0) {
            m_xFrame = (com.sun.star.frame.XFrame) UnoRuntime.queryInterface(
                    com.sun.star.frame.XFrame.class, object[0]);
        }

    }

    // com.sun.star.frame.XDispatch:
    public void dispatch(com.sun.star.util.URL aURL,
            com.sun.star.beans.PropertyValue[] aArguments) {
        if (aURL.Protocol.compareTo("ru.ssau.graphplus.addon4:") == 0) {
            if (aURL.Path.compareTo("Command0") == 0) {


                if (xDrawDoc == null) {

                    xDrawDoc = openDraw(m_xContext);

                    try {
                        System.out.println("getting Drawpage");
                        com.sun.star.drawing.XDrawPagesSupplier xDPS =
                                (com.sun.star.drawing.XDrawPagesSupplier) UnoRuntime.queryInterface(
                                com.sun.star.drawing.XDrawPagesSupplier.class, xDrawDoc);
                        com.sun.star.drawing.XDrawPages xDPn = xDPS.getDrawPages();
                        com.sun.star.container.XIndexAccess xDPi =
                                (com.sun.star.container.XIndexAccess) UnoRuntime.queryInterface(
                                com.sun.star.container.XIndexAccess.class, xDPn);
                        xDrawPage = (com.sun.star.drawing.XDrawPage) UnoRuntime.queryInterface(
                                com.sun.star.drawing.XDrawPage.class, xDPi.getByIndex(0));
                        XModifiable xMod = (XModifiable) UnoRuntime.queryInterface(
                                XModifiable.class, xDrawDoc);

                        xMod.addModifyListener(new XModifyListener() {

                            public void modified(com.sun.star.lang.EventObject arg0) {
                                System.out.println("modified");

                                System.out.println(arg0.toString());

                                if (xDrawPage.getCount() > count) {

                                    System.out.println("added new shape");


                                    // берем последний
                                    Object obj = null;
                                    try {
                                        obj = xDrawPage.getByIndex(xDrawPage.getCount() - 1);
                                    } catch (IndexOutOfBoundsException ex) {
                                        Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (WrappedTargetException ex) {
                                        Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    XShape xShape = (XShape) UnoRuntime.queryInterface(XShape.class, obj);
                                    System.out.println(xShape.getShapeType());
                                    if (xShape.getShapeType().equals("com.sun.star.drawing.ConnectorShape")) {
                                        count++;

                                        System.out.println("ConnectorShape added");
                                        XConnectorShape xConnSh = UnoRuntime.queryInterface(XConnectorShape.class, xShape);

                                        xMCF = m_xContext.getServiceManager();
                                        xMSF = UnoRuntime.queryInterface(XMultiServiceFactory.class, xMCF);

                                        try {


                                            //Object shobj = xMSF.createInstanceWithArguments("ConnectorShape",new Object[] {xConnSh} );
                                            XPropertySet xShapeProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xConnSh);
                                            Object startShape = xShapeProps.getPropertyValue("StartShape");
                                            Object endShape = xShapeProps.getPropertyValue("EndShape");
                                            XShape xShStart = (XShape) UnoRuntime.queryInterface(XShape.class, startShape);
                                            XShape xShEnd = (XShape) UnoRuntime.queryInterface(XShape.class, endShape);

                                            System.out.println("start shape " + xShStart.getShapeType());

                                            XPropertySet xShStartProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShStart);

                                            System.out.println("props " + xShStartProps.getPropertyValue("Name"));

                                            System.out.println("end shape " + xShEnd.getShapeType());



                                            XPropertySet xShEndProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShEnd);

                                            System.out.println("props " + xShEndProps.getPropertyValue("Name"));
//                                            com.sun.star.lang.XMultiServiceFactory xMSF =
//                                                    (com.sun.star.lang.XMultiServiceFactory) UnoRuntime.queryInterface(
//                                                    com.sun.star.lang.XMultiServiceFactory.class, xDocComp);
                                            //UnoRuntime.
                                            XNamed xNamedConnector = UnoRuntime.queryInterface(XNamed.class, xConnSh);

                                            createDialog(xNamedConnector, xConnSh);

                                            //xConnSh.
                                        } catch (Exception ex) {
                                            Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    } else {

                                        if (xShape.getShapeType().equals("com.sun.star.drawing.EllipseShape")) {
                                            count++;
                                            System.out.println("Not Connector shape");
                                            XPropertySet xShapeProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);




                                            //xShapeProps.setPropertyValue("Name", lastEnteredName);
                                            String s;
                                            XNamed xNamed = UnoRuntime.queryInterface(
                                                    XNamed.class, xShape);
                                            try {
                                                createDialog(xNamed, xShape);
                                            } catch (Exception ex) {
                                                Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
                                            }


                                            //System.out.println("lastEnteredName" + lastEnteredName);
                                            ///xNamed.setName(lastEnteredName);


                                            ///String name = (String) xNamed.getName();
                                            //System.out.println(name);




                                        }
                                    }

                                }

                            }

                            public void disposing(com.sun.star.lang.EventObject arg0) {
                                System.out.println("disposing");
                            }
                        });


                    } catch (Exception e) {
                        System.err.println("Couldn't create document" + e);
                        e.printStackTrace(System.err);





                    }

                    return;




                }
            }
            if (aURL.Path.compareTo("Command1") == 0) {
//                XDispatchProviderInterception xDPI = (XDispatchProviderInterception) UnoRuntime.queryInterface(XDispatchProviderInterception.class, m_xFrame);
//                MyInterceptor interceptor = new MyInterceptor();
//                xDPI.registerDispatchProviderInterceptor(interceptor);

                generateXML();





                return;




            }




            if (aURL.Path.compareTo("Command2") == 0) {
//                try {
//                    //createDialog();
//
//
//
//
//
//
//                } catch (Exception ex) {
//                    Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
//                }
            }
            if (aURL.Path.compareTo("Command3") == 0) {
                // add your own code here
                return;




            }
            if (aURL.Path.compareTo("Command4") == 0) {
                // add your own code here
                return;




            }
        }
    }

    public void addStatusListener(com.sun.star.frame.XStatusListener xControl,
            com.sun.star.util.URL aURL) {
        // add your own code here
    }

    public void removeStatusListener(com.sun.star.frame.XStatusListener xControl,
            com.sun.star.util.URL aURL) {
        // add your own code here
    }

    // com.sun.star.lang.XServiceInfo:
    public String getImplementationName() {
        return m_implementationName;




    }

    public boolean supportsService(String sService) {
        int len = m_serviceNames.length;




        for (int i = 0; i
                < len; i++) {
            if (sService.equals(m_serviceNames[i])) {
                return true;




            }
        }
        return false;




    }

    public String[] getSupportedServiceNames() {
        return m_serviceNames;




    }

    // com.sun.star.frame.XDispatchProvider:
    public com.sun.star.frame.XDispatch queryDispatch(com.sun.star.util.URL aURL,
            String sTargetFrameName,
            int iSearchFlags) {
        if (aURL.Protocol.compareTo("ru.ssau.graphplus.addon4:") == 0) {
            if (aURL.Path.compareTo("Command0") == 0) {
                return this;




            }
            if (aURL.Path.compareTo("Command1") == 0) {
                return this;




            }
            if (aURL.Path.compareTo("Command2") == 0) {
                return this;




            }
            if (aURL.Path.compareTo("Command3") == 0) {
                return this;




            }
            if (aURL.Path.compareTo("Command4") == 0) {
                return this;




            }
        }
        return null;




    }

    // com.sun.star.frame.XDispatchProvider:
    public com.sun.star.frame.XDispatch[] queryDispatches(
            com.sun.star.frame.DispatchDescriptor[] seqDescriptors) {
        int nCount = seqDescriptors.length;
        com.sun.star.frame.XDispatch[] seqDispatcher =
                new com.sun.star.frame.XDispatch[seqDescriptors.length];






        for (int i = 0; i
                < nCount;
                ++i) {
            seqDispatcher[i] = queryDispatch(seqDescriptors[i].FeatureURL, seqDescriptors[i].FrameName, seqDescriptors[i].SearchFlags);




        }
        return seqDispatcher;




    }

    public static com.sun.star.lang.XComponent openDraw(
            com.sun.star.uno.XComponentContext xContext) {
        com.sun.star.frame.XComponentLoader xCLoader;
        com.sun.star.text.XTextDocument xDoc = null;
        com.sun.star.lang.XComponent xComp = null;




        try {
            // get the remote office service manager
            com.sun.star.lang.XMultiComponentFactory xMCF =
                    xContext.getServiceManager();

            Object oDesktop = xMCF.createInstanceWithContext(
                    "com.sun.star.frame.Desktop", xContext);

            xCLoader = (com.sun.star.frame.XComponentLoader) UnoRuntime.queryInterface(com.sun.star.frame.XComponentLoader.class,
                    oDesktop);
            com.sun.star.beans.PropertyValue szEmptyArgs[] =
                    new com.sun.star.beans.PropertyValue[0];
            String strDoc = "private:factory/sdraw";
            xComp = xCLoader.loadComponentFromURL(strDoc, "_blank", 0, szEmptyArgs);
        } catch (Exception e) {
            System.err.println(" Exception " + e);
            e.printStackTrace(System.err);




        }

        return xComp;






    }
    private static final String _buttonName = "Button1";
    private static final String _cancelButtonName = "CancelButton";
    private static final String _labelName = "Label1";
    private static final String _labelPrefix = "Number of button clicks: ";
    private static final String _textfieldName = "TextField1";
    private static final String _checkboxName = "Checkbox1";

    private void createDialog(final XNamed xNmd, XShape xShp) throws com.sun.star.uno.Exception {

        XComponentContext _xComponentContext = m_xContext;


        // get the service manager from the component context
        XMultiComponentFactory xMultiComponentFactory = _xComponentContext.getServiceManager();

        // create the dialog model and set the properties
        final Object dialogModel = xMultiComponentFactory.createInstanceWithContext(
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
                "Title", new String("Свойства элемента"));

        // get the service manager from the dialog model
        XMultiServiceFactory xMultiServiceFactory = (XMultiServiceFactory) UnoRuntime.queryInterface(
                XMultiServiceFactory.class, dialogModel);

        // create the button model and set the properties
        Object buttonModel = xMultiServiceFactory.createInstance(
                "com.sun.star.awt.UnoControlButtonModel");
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
                "Label", new String("Click Me"));


        // create the textbox model and set the properties

        Object editModel = xMultiServiceFactory.createInstance(
                "com.sun.star.awt.UnoControlEditModel");
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
                "TabIndex", new Short((short) 1));

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
                "TabIndex", new Short((short) 2));

        xPSetCheckBox.setPropertyValue(
                "Label", "Автоинкремент");




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
                "TabIndex", new Short((short) 3));
        xPSetCancelButton.setPropertyValue(
                "PushButtonType", new Short((short) 2));
        xPSetCancelButton.setPropertyValue(
                "Label", new String("Cancel"));


        ///


        Object oCBModel = xMultiServiceFactory.createInstance("com.sun.star.awt.UnoControlCheckBoxModel");
        // Set the properties at the model - keep in mind to pass the property names in alphabetical order!
        XMultiPropertySet xCBMPSet = (XMultiPropertySet) UnoRuntime.queryInterface(XMultiPropertySet.class, oCBModel);

        xCBMPSet.setPropertyValues(
                new String[]{"Height",
                    "Label", "Name", "PositionX", "PositionY", "State", "TriState", "Width"},
                new Object[]{new Integer(8), "Auto", _checkboxName,
                    new Integer(50), new Integer(50), new Short((short) 1), Boolean.TRUE,
                    new Integer(10)
                });

        // add the model to the NameContainer of the dialog model
        // insert the control models into the dialog model
        XNameContainer xNameCont = (XNameContainer) UnoRuntime.queryInterface(
                XNameContainer.class, dialogModel);
        xNameCont.insertByName(_checkboxName, oCBModel);
        xNameCont.insertByName(_buttonName, buttonModel);
        xNameCont.insertByName(_textfieldName, editModel);
        //xNameCont.insertByName(_checkboxName, cbModel);
        xNameCont.insertByName(_cancelButtonName, cancelButtonModel);


        // create the dialog control and set the model
        Object dialog = xMultiComponentFactory.createInstanceWithContext(
                "com.sun.star.awt.UnoControlDialog", _xComponentContext);
        XControl xControl = (XControl) UnoRuntime.queryInterface(
                XControl.class, dialog);
        XControlModel xControlModel = (XControlModel) UnoRuntime.queryInterface(
                XControlModel.class, dialogModel);

        xControl.setModel(xControlModel);
        // add an action listener to the button control
        XControlContainer xControlCont = (XControlContainer) UnoRuntime.queryInterface(
                XControlContainer.class, dialog);
        Object objectButton = xControlCont.getControl("Button1");
        XButton xButton = (XButton) UnoRuntime.queryInterface(
                XButton.class, objectButton);
        XDialog xDialog = UnoRuntime.queryInterface(XDialog.class, dialog);

        xButton.addActionListener(new ActionListenerImpl(xControlCont, xNmd, xDialog,
                xShp));

        XControl xCBControl = xControlCont.getControl(_checkboxName);
        XCheckBox xCB = UnoRuntime.queryInterface(XCheckBox.class, xCBControl);
        xCB.addItemListener(
                new XItemListener() {

                    public void itemStateChanged(ItemEvent arg0) {
                        System.out.println("auto");
                    }

                    public void disposing(com.sun.star.lang.EventObject arg0) {
                        //throw new UnsupportedOperationException("Not supported yet.");
                    }
                });

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
        // execute the dialog


        xDialog.execute();
        // dispose the dialog
        XComponent xComponent = (XComponent) UnoRuntime.queryInterface(
                XComponent.class, dialog);

        xComponent.dispose();
    }

    /** add text to a shape.
    the return value is the PropertySet of the text range that has been added
     */
    public static XPropertySet addPortion(
            XShape xShape, String sText, boolean bNewParagraph)
            throws com.sun.star.lang.IllegalArgumentException {
        XText xText = (XText) UnoRuntime.queryInterface(XText.class, xShape);
        XTextCursor xTextCursor = xText.createTextCursor();
        xTextCursor.gotoEnd(
                false);








        if (bNewParagraph) {
            xText.insertControlCharacter(xTextCursor, ControlCharacter.PARAGRAPH_BREAK, false);
            xTextCursor.gotoEnd(false);
        }
        XTextRange xTextRange = (XTextRange) UnoRuntime.queryInterface(XTextRange.class, xTextCursor);

        xTextRange.setString(sText);

        xTextCursor.gotoEnd(true);
        XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xTextRange);
        return xPropSet;
    }

    public void generateXML() {
        OutputStream outputStream = null;




        try {
            File f;
            f = new File("/home/anton/doc.xml");





            if (!f.exists()) {
                try {
                    f.createNewFile();






                } catch (IOException ex) {
                    Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
                }





            }
            System.out.println(f.getCanonicalPath());
            outputStream = new FileOutputStream(f);






        } catch (IOException ex) {
            Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);

        }




        try {
//            Object oDesktop = xMCF.createInstanceWithContext(
//                    "com.sun.star.frame.Desktop", m_xContext);
//
//            XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(
//                    XDesktop.class, oDesktop);
//            XFrame xFrame = xDesktop.getCurrentFrame();
//            XWindow xWindowComponent = xFrame.getComponentWindow();
//            XWindow xWindowsCont = xFrame.getContainerWindow();
//            XController xController = xFrame.getController();
//            XModel xModel = xController.getModel();
            com.sun.star.drawing.XDrawPagesSupplier xDPS =
                    (com.sun.star.drawing.XDrawPagesSupplier) UnoRuntime.queryInterface(
                    com.sun.star.drawing.XDrawPagesSupplier.class, xDrawDoc);
            com.sun.star.drawing.XDrawPages xDPn = xDPS.getDrawPages();
            com.sun.star.container.XIndexAccess xDPi =
                    (com.sun.star.container.XIndexAccess) UnoRuntime.queryInterface(
                    com.sun.star.container.XIndexAccess.class, xDPn);
            xDrawPage = (com.sun.star.drawing.XDrawPage) UnoRuntime.queryInterface(
                    com.sun.star.drawing.XDrawPage.class, xDPi.getByIndex(0));
            try {
                XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter(new OutputStreamWriter(outputStream, "utf-8"));
                out.writeStartDocument();
                out.writeStartElement("graph");


                for (int i = 0; i < xDrawPage.getCount(); i++) {
                    System.out.println(i);
                    XShape xSh = null;
                    try {
                        xSh = (XShape) UnoRuntime.queryInterface(XShape.class, xDrawPage.getByIndex(i));
                    } catch (IndexOutOfBoundsException ex) {
                        Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (WrappedTargetException ex) {
                        Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    XNamed xN = (XNamed) UnoRuntime.queryInterface(XNamed.class, xSh);

                    if (xSh.getShapeType().equals("com.sun.star.drawing.EllipseShape"))out.writeStartElement("node");
                    if (xSh.getShapeType().equals("com.sun.star.drawing.ConnectorShape")) {
                        
                    }
                    if (xSh.getShapeType().equals("com.sun.star.drawing.ConnectorShape"))out.writeStartElement("link");
                    if (xN != null) {
                        out.writeAttribute("id", xN.getName());
                    }
                    out.writeEndElement();
                }
                out.writeEndElement();
                out.writeEndDocument();

                out.flush();
                out.close();

            } catch (XMLStreamException ex) {
                Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
            }

            outputStream.flush();

            outputStream.close();
        
        
        } catch (IndexOutOfBoundsException ex) {
            Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
        
        } //catch (Exception ex) {
//            Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
//        }
































    }

    public class ActionListenerImpl
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
            Object text = _xControlCont.getControl(_textfieldName);
            XTextComponent xTextComp = (XTextComponent) UnoRuntime.queryInterface(
                    XTextComponent.class, text);
            _xNamed.setName(xTextComp.getText());
            try {
                addPortion(_xShape, xTextComp.getText(), false);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
            }
            _xDialog.endExecute();

        }

        public void disposing(com.sun.star.lang.EventObject arg0) {
            _xControlCont = null;
        }
    }
}
