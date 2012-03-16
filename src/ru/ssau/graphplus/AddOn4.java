package ru.ssau.graphplus;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XEnumeration;
import com.sun.star.document.EventObject;
import com.sun.star.document.XEventListener;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XShape;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XDispatchProviderInterception;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.text.XTextDocument;
import com.sun.star.util.XModifiable;
import com.sun.star.util.XModifyListener;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    com.sun.star.lang.XComponent xDrawDoc = null;
    com.sun.star.drawing.XDrawPage xDrawPage = null;
    com.sun.star.lang.XMultiComponentFactory xMCF = null;
    XMultiServiceFactory xMSF = null;
    int count = 0;

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
//        XDispatchProviderInterception xDPI = (XDispatchProviderInterception) UnoRuntime.queryInterface( XDispatchProviderInterception.class, m_xFrame);
//        MyInterceptor interceptor = new MyInterceptor();
//        xDPI.registerDispatchProviderInterceptor(interceptor);

    }

    // com.sun.star.frame.XDispatch:
    public void dispatch(com.sun.star.util.URL aURL,
            com.sun.star.beans.PropertyValue[] aArguments) {
        if (aURL.Protocol.compareTo("ru.ssau.graphplus.addon4:") == 0) {
            if (aURL.Path.compareTo("Command0") == 0) {


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
                            try {
                                if (xDrawPage.getCount() > count) {

                                    System.out.println("added new shape");
                                    count++;
                                    Object obj = xDrawPage.getByIndex(xDrawPage.getCount() - 1);
                                    XShape xShape = (XShape) UnoRuntime.queryInterface(XShape.class, obj);
                                    System.out.println(xShape.getShapeType());
                                    if (xShape.getShapeType().equals("com.sun.star.drawing.ConnectorShape")) {
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



                                            //xConnSh.
                                        } catch (Exception ex) {
                                            Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }


                                }

                            } catch (IndexOutOfBoundsException ex) {
                                Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (WrappedTargetException ex) {
                                Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
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



//                com.sun.star.lang.XMultiComponentFactory xMCF = m_xContext.getServiceManager();
//                Object desktop;
//                try {
//                     desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", m_xContext);
//
//                    XDesktop xDesktop = (XDesktop)UnoRuntime.queryInterface(XDesktop.class, desktop);
//                      System.out.println(((XTextDocument) ((XDesktop) xDesktop).getCurrentFrame().getController().getModel()).getText().getString());
////                     while (xEnum.hasMoreElements()){
////                         System.out.print(xEnum.nextElement().getClass().toString());
////                     }
//                } catch (Exception ex) {
//                    Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
//                }




//                ((XModifiable) xDrawDoc).addModifyListener(new XModifyListener() {
//
//                    public void modified(com.sun.star.lang.EventObject arg0) {
//                        System.out.print("modified\n");
//                        System.out.print(arg0.toString());
//                    }
//
//                    public void disposing(com.sun.star.lang.EventObject arg0) {
//                        //throw new UnsupportedOperationException("Not supported yet.");
//                    }
//                });
//                xDrawDoc.addEventListener(new XEventListener() {
//
//                    public void notifyEvent(EventObject arg0) {
//
//                    }
//
//                    public void disposing(com.sun.star.lang.EventObject arg0) {
//                        //throw new UnsupportedOperationException("Not supported yet.");
//                    }
//                });
//xDrawDoc.



                return;
            }
            if (aURL.Path.compareTo("Command1") == 0) {
                //m_xFrame.getController().getViewData()
                return;
            }
            if (aURL.Path.compareTo("Command2") == 0) {
                // add your own code here
                return;
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

        for (int i = 0; i < len; i++) {
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

        for (int i = 0; i < nCount; ++i) {
            seqDispatcher[i] = queryDispatch(seqDescriptors[i].FeatureURL,
                    seqDescriptors[i].FrameName,
                    seqDescriptors[i].SearchFlags);
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
}
