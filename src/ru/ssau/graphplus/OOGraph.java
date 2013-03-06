package ru.ssau.graphplus;

import com.sun.star.awt.*;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.drawing.XConnectorShape;
import ru.ssau.graphplus.gui.ContextMenuInterceptor;
import ru.ssau.graphplus.gui.Gui;
import ru.ssau.graphplus.gui.SystemDialog;
import ru.ssau.graphplus.link.Link;
import ru.ssau.graphplus.link.LinkFactory;
import ru.ssau.graphplus.link.Linker;
import ru.ssau.graphplus.link.LinkerImpl;
import ru.ssau.graphplus.node.Node;
import ru.ssau.graphplus.node.NodeFactory;
import com.sun.star.beans.*;
import com.sun.star.container.XNameContainer;
import com.sun.star.document.EventObject;
import com.sun.star.document.XEventBroadcaster;
import com.sun.star.document.XEventListener;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.drawing.XShapes;
import com.sun.star.frame.*;
import com.sun.star.lang.*;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.ui.XContextMenuInterception;
import com.sun.star.uno.Exception;
import com.sun.star.uno.Type;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.URL;
import com.sun.star.util.XModifiable;
import com.sun.star.view.XSelectionSupplier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class OOGraph extends WeakBase
   implements com.sun.star.lang.XInitialization,
              com.sun.star.task.XJob,
              com.sun.star.frame.XDispatchProvider,
              com.sun.star.lang.XServiceInfo,
              com.sun.star.util.XCloseable,
              com.sun.star.task.XAsyncJob,
              XDispatch,
              XEventListener,
              XFrameActionListener
{
    //public static final String ODG = ".odg";
    private final XComponentContext m_xContext;
    private static com.sun.star.frame.XFrame m_xFrame;
    private static final String m_implementationName = OOGraph.class.getName();
    private static final String[] m_serviceNames = {
        "com.sun.star.frame.ProtocolHandler",
        "com.sun.star.task.Job",
        "com.sun.star.task.AsyncJob" };
    private static final Model diagramModel = new Model();

    
    static XMultiComponentFactory xMCF = null;
    static XMultiServiceFactory xMSF = null;
    XComponent xDrawDoc = null;
    // store every frame with its Controller object
    private static ArrayList<FrameObject> _frameObjectList = null;
    private static XComponent m_xComponent = null;
    private Controller m_Controller = null;
    int count = 1;




    public OOGraph( XComponentContext context )
    {
        m_xContext = context;
    };

    public static XSingleComponentFactory __getComponentFactory( String sImplementationName ) {
        XSingleComponentFactory xFactory = null;

        if ( sImplementationName.equals( m_implementationName ) )
            xFactory = Factory.createComponentFactory(OOGraph.class, m_serviceNames);
        return xFactory;
    }

    public static boolean __writeRegistryServiceInfo( XRegistryKey xRegistryKey ) {
        return Factory.writeRegistryServiceInfo(m_implementationName,
                                                m_serviceNames,
                                                xRegistryKey);
    }

    // com.sun.star.lang.XInitialization:
    public void initialize(Object[] object) throws com.sun.star.uno.Exception
    {
        if (object.length > 0) {
            m_xFrame = (com.sun.star.frame.XFrame) UnoRuntime.queryInterface(com.sun.star.frame.XFrame.class, object[0]);

            XController xController = m_xFrame.getController();
            XContextMenuInterception xContMenuInterception =
                    (XContextMenuInterception) UnoRuntime.queryInterface(XContextMenuInterception.class, xController);
            xContMenuInterception.registerContextMenuInterceptor(new ContextMenuInterceptor(m_xContext));


            xMCF = m_xContext.getServiceManager();
//            xMSF = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, xMCF);

            m_xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, m_xFrame.getController().getModel());
            xMSF = (XMultiServiceFactory) UnoRuntime.queryInterface(
                    XMultiServiceFactory.class, m_xComponent);

//            NodeFactory nodeFactory = new NodeFactory();
            NodeFactory.setXmsf(xMSF);
            LinkFactory.setXmsf(xMSF);


            XEventBroadcaster xEB = (XEventBroadcaster) UnoRuntime.queryInterface(XEventBroadcaster.class, m_xComponent);
            xEB.addEventListener(new XEventListener() {

                public void notifyEvent(EventObject arg0) {
                    System.out.print(arg0.EventName);
                }

                public void disposing(com.sun.star.lang.EventObject arg0) {
                    //throw new UnsupportedOperationException("Not supported yet.");
                }
            });

            XDrawPage xDrawPage = null; // = DrawHelper.getDrawPageByIndex(m_xComponent, 0);
            XShape xShape = null; // = DrawHelper.createEllipseShape(m_xComponent, 200, 200, 500, 500);
            XComponent xCompShape; // = (XComponent) UnoRuntime.queryInterface(XComponent.class, xShape);



            // add the m_xFrame and its m_Controller to the static arrayList of _frameObjectList
            // avoid the duplicate gui controls
            boolean isNewFrame;
            if (_frameObjectList == null) {
                _frameObjectList = new ArrayList<FrameObject>();
                isNewFrame = true;
            } else {
                isNewFrame = true;
                for (FrameObject frameObj : _frameObjectList) {
                    if (m_xFrame.equals(frameObj.getXFrame())) {
                        isNewFrame = false;
                    }
                }
            }
            if (isNewFrame) {
                try {
                    m_xFrame.addFrameActionListener(this);
                    XEventBroadcaster xEB2 = (XEventBroadcaster) UnoRuntime.queryInterface(XEventBroadcaster.class, m_xFrame);


                    xDrawDoc = (XComponent) UnoRuntime.queryInterface(
                            XComponent.class, m_xComponent);
                    // *************

                    xDrawPage = DrawHelper.getDrawPageByIndex(m_xComponent, 0);

                    XEventBroadcaster xEB3 = (XEventBroadcaster) UnoRuntime.queryInterface(XEventBroadcaster.class, xDrawPage);
                    XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class,
                            xDrawPage);

//                    xShape = 
//com.sun.star.drawing.
                    //xShape = DrawHelper.createEllipseShape(xDrawDoc, 200, 200, 2000, 2000);// .createShape(m_xComponent, new Point(200, 200), new Size(500, 500), "com.sun.star.drawing.EllipseShape");
                    Object shapeObj = xMSF.createInstance("com.sun.star.drawing.EllipseShape");
                    xShape = (XShape) QI.XShape(shapeObj);
                    try {
//                    Misc.tagShapeAsNode(xShape);
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }

                    shapeObj = ShapeHelper.createShape(xDrawDoc, new Point(200, 200), new Size(1500, 1500), "com.sun.star.drawing.EllipseShape");
                    try {
//                    Misc.tagShapeAsNode(xShape);
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }
//                    XInitialization xInit = (XInitialization) UnoRuntime.queryInterface(XInitialization.class, shapeObj);
                    //xInit.
//                    XSingleServiceFactory xSSF = xMCF.
//                xShape = (XShape) QI.XShape(shapeObj);

                    xCompShape = (XComponent) UnoRuntime.queryInterface(XComponent.class, xShape);
                    xCompShape.addEventListener(new XEventListener() {

                        public void notifyEvent(EventObject arg0) {
                            System.err.println(arg0.EventName);
                            //                    throw new UnsupportedOperationException("Not supported yet.");
                        }

                        public void disposing(com.sun.star.lang.EventObject arg0) {
                            //                    throw new UnsupportedOperationException("Not supported yet.");
                        }
                    });

                    // *************




                    if (m_xComponent != null) {
                        m_xComponent.addEventListener(this);
                    }
                    //m_xEventBroadcaster = (XEventBroadcaster) UnoRuntime.queryInterface(XEventBroadcaster.class, m_xFrame.getController().getModel());
                    //addEventListener();

                    if (m_Controller == null) {
                        m_Controller = new Controller(m_xContext, m_xFrame, xMSF, xMCF, xDrawPage);
                    }
                    // when the frame is closed we have to remove FrameObject item into the list
                    _frameObjectList.add(new FrameObject(m_xFrame, m_Controller));
                } catch (java.lang.Exception ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
//                for (FrameObject frameObj : _frameObjectList) {
//                    if (m_xFrame.equals(frameObj.getXFrame())) {
//                        m_Controller = frameObj.getController();
//                    }
//                }
            }

//            m_xFrame = (com.sun.star.frame.XFrame) UnoRuntime.queryInterface(
//                    com.sun.star.frame.XFrame.class, object[0]);


//            XController xController = null;
//            XModel xModel = xController.getModel();
//            //xController.getViewData()
//            //xModel.
//            m_xContext.
//            DrawHelper.get
//            DrawHelper.createEllipseShape(object, x, y, width, height)

//            XDispatchProviderInterception xDPI = (XDispatchProviderInterception) UnoRuntime.queryInterface(XDispatchProviderInterception.class, m_xFrame);
////
//            MyInterceptor interceptor = new MyInterceptor();
//            xDPI.registerDispatchProviderInterceptor(interceptor);




//            Misc.tagShapeAsNode(xShape);
//            xDrawPage.add(xShape);


//         
            ///****************************
            //try {
            //xDrawDoc = m_xComponent;
            System.out.println("getting Drawpage");
            com.sun.star.drawing.XDrawPagesSupplier xDPS = (com.sun.star.drawing.XDrawPagesSupplier) UnoRuntime.queryInterface(
                    com.sun.star.drawing.XDrawPagesSupplier.class, m_xComponent);

            com.sun.star.drawing.XDrawPages xDPn = xDPS.getDrawPages();
            com.sun.star.container.XIndexAccess xDPi =
                    (com.sun.star.container.XIndexAccess) UnoRuntime.queryInterface(
                    com.sun.star.container.XIndexAccess.class, xDPn);
            final XDrawPage xDP = (com.sun.star.drawing.XDrawPage) UnoRuntime.queryInterface(
                    com.sun.star.drawing.XDrawPage.class, xDPi.getByIndex(0));
            XModifiable xMod = (XModifiable) UnoRuntime.queryInterface(
                    XModifiable.class, xDrawDoc);
            xMod.addModifyListener(_frameObjectList.get(0).getController());

            ///*****************************



//            }
//            catch(Exception ex){
//                
//            }






//        XDispatchProviderInterception xDPI = (XDispatchProviderInterception) UnoRuntime.queryInterface(XDispatchProviderInterception.class, m_xFrame);
//        MyInterceptor interceptor = new MyInterceptor();
//        xDPI.registerDispatchProviderInterceptor(interceptor);

//
//        XDispatchProviderInterception xDPI = (XDispatchProviderInterception) UnoRuntime.queryInterface(XDispatchProviderInterception.class, m_xFrame);
//        MyInterceptor interceptor = new MyInterceptor();
//        xDPI.registerDispatchProviderInterceptor(interceptor);



//        try {
//            
//        } catch (com.sun.star.uno.Exception ex) {
//            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
//        }


        }
    }

    // com.sun.star.task.XJob:
    public Object execute(com.sun.star.beans.NamedValue[] Arguments) throws com.sun.star.lang.IllegalArgumentException, com.sun.star.uno.Exception
    {
        // TODO: Exchange the default return implementation for "execute" !!!
        // NOTE: Default initialized polymorphic structs can cause problems
        // because of missing default initialization of primitive types of
        // some C++ compilers or different Any initialization in Java and C++
        // polymorphic structs.

        String url = QI.XModel(m_xFrame.getController().getModel()).getURL();
        String substring = url.substring(7);
        String[] split = url.split("/");
        String name = split[split.length - 1];
        String ext = substring.substring(substring.length() - 4, substring.length());
        if (".odg".equals(ext.toLowerCase())) {

            save(substring.replace(".odg", "").concat(".xml"));
        }

        return com.sun.star.uno.Any.VOID;
    }

    // com.sun.star.frame.XDispatchProvider:
    public com.sun.star.frame.XDispatch queryDispatch(com.sun.star.util.URL aURL, String TargetFrameName, int SearchFlags)
    {
        // TODO: Exchange the default return implementation for "queryDispatch" !!!
        // NOTE: Default initialized polymorphic structs can cause problems
        // because of missing default initialization of primitive types of
        // some C++ compilers or different Any initialization in Java and C++
        // polymorphic structs.
        if (aURL.Protocol.compareTo("ru.ssau.graphplus:") == 0) {

            m_Controller.getHistoryOfActions().add(aURL.toString());


            if (aURL.Path.compareTo("Omg") == 0){
                return this;
            }

            if (aURL.Path.compareTo("Node") == 0) {
                return this;
            }

            if (aURL.Path.compareTo("ProcessNode") == 0) {
                return this;
            }

            if (aURL.Path.compareTo("ClientNode") == 0) {
                return this;
            }

            if (aURL.Path.compareTo("ServerNode") == 0) {
                return this;
            }

            if (aURL.Path.compareTo("ProcedureNode") == 0) {
                return this;
            }

            if (aURL.Path.compareTo("Link") == 0) {
                return this;
            }

            if (aURL.Path.compareTo("LinkLink") == 0) {
                return this;
            }

            if (aURL.Path.compareTo("MessageLink") == 0) {
                return this;
            }

            if (aURL.Path.compareTo("ControlLink") == 0) {
                return this;
            }


            if (aURL.Path.compareTo("Save") == 0) {
                return this;
            }


            if (aURL.Path.compareTo("Tag") == 0) {
                return this;
            }

            if (aURL.Path.compareTo("Assoc") == 0) {
                return this;
            }

            if (aURL.Path.compareTo("TagAsLink") == 0) {


                System.out.println("return XDispatch");
                return new XDispatch() {

                    public void dispatch(URL arg0, PropertyValue[] arg1) {
                        try {
                            System.out.println("TagAsLink");
                                //System.out.println(arg0.Arguments);
                            m_xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, m_xFrame.getController().getModel());
                            xDrawDoc = (XComponent) UnoRuntime.queryInterface(
                                    XComponent.class, m_xComponent);



                            XDrawPage xPage = PageHelper.getDrawPageByIndex(xDrawDoc, 0);
                            XController xController = m_xFrame.getController();

                            //                            Object ddv = xMCF.createInstanceWithContext("com.sun.star.drawing.DrawingDocumentDrawView", m_xContext);
                            //XSelectionSupplier
                            XSelectionSupplier xSelectSup = QI.XSelectionSupplier(xController);
                            Object selectionObj = xSelectSup.getSelection();
                            XShapes xShapes = (XShapes) UnoRuntime.queryInterface(
                                    XShapes.class, selectionObj);
                            try {
                                XShape xShape = (XShape) QI.XShape(xShapes.getByIndex(0));
                                System.out.println(xShape.getShapeType());
                                if (xShape.getShapeType().contains("Connector")) {
                                    Misc.tagShapeAsLink(xShape);
                                    chooseLinkType(xShape);
                                    //Gui.createDialog(QI.XNamed(xShape), xShape, m_xContext, _frameObjectList.get(0).getController().getElements());
                                }

                            } catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
                                Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (WrappedTargetException ex) {
                                Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (Exception ex) {
                                Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            for (PropertyValue pv : arg1) {
                                System.out.println(pv.Name);
                            }
                        } catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
                            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (WrappedTargetException ex) {
                            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }

                    public void addStatusListener(XStatusListener arg0, URL arg1) {
                        //throw new UnsupportedOperationException("Not supported yet.");
                    }

                    public void removeStatusListener(XStatusListener arg0, URL arg1) {
                        //throw new UnsupportedOperationException("Not supported yet.");
                    }
                };

            }

            if (aURL.Path.compareTo("TagAsNode") == 0) {
                System.out.println("return XDispatch");
                return new XDispatch() {

                    public void dispatch(URL arg0, PropertyValue[] arg1) {
                        try {
                            System.out.println("TagAsNode");
                            System.out.println(arg0.Arguments);
                            m_xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, m_xFrame.getController().getModel());
                            xDrawDoc = (XComponent) UnoRuntime.queryInterface(
                                    XComponent.class, m_xComponent);

                            XDrawPage xPage = PageHelper.getDrawPageByIndex(xDrawDoc, 0);
                            XController xController = m_xFrame.getController();

                            //                            Object ddv = xMCF.createInstanceWithContext("com.sun.star.drawing.DrawingDocumentDrawView", m_xContext);
                            //XSelectionSupplier
                            XSelectionSupplier xSelectSup = QI.XSelectionSupplier(xController);
                            Object selectionObj = xSelectSup.getSelection();
                            XShapes xShapes = (XShapes) UnoRuntime.queryInterface(
                                    XShapes.class, selectionObj);
                            try {
                                XShape xShape = (XShape) QI.XShape(xShapes.getByIndex(0));
                                System.out.println(xShape.getShapeType());
                                //if (xShape.getShapeType().contains("Ellipse")) {
                                    Misc.tagShapeAsNode(xShape);
                                    
                                    Gui.createDialog(QI.XNamed(xShape), xShape, m_xContext, _frameObjectList.get(0).getController().getElements());
                                //}

                            } catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
                                Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (WrappedTargetException ex) {
                                Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (Exception ex) {
                                Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            for (PropertyValue pv : arg1) {
                                System.out.println(pv.Name);
                            }
                        } catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
                            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (WrappedTargetException ex) {
                            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }

                    public void addStatusListener(XStatusListener arg0, URL arg1) {
                        //throw new UnsupportedOperationException("Not supported yet.");
                    }

                    public void removeStatusListener(XStatusListener arg0, URL arg1) {
                        //throw new UnsupportedOperationException("Not supported yet.");
                    }
                };

            }

        //    return null;
        }
        return null;
    }

    public com.sun.star.frame.XDispatch[] queryDispatches(com.sun.star.frame.DispatchDescriptor[] seqDescriptors)
    {
        // TODO: Exchange the default return implementation for "queryDispatches" !!!
        // NOTE: Default initialized polymorphic structs can cause problems
        // because of missing default initialization of primitive types of
        // some C++ compilers or different Any initialization in Java and C++
        // polymorphic structs.
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

    // com.sun.star.lang.XServiceInfo:
    public String getImplementationName() {
         return m_implementationName;
    }

    public boolean supportsService( String sService ) {
        int len = m_serviceNames.length;

        for( int i=0; i < len; i++) {
            if (sService.equals(m_serviceNames[i]))
                return true;
        }
        return false;
    }

    public String[] getSupportedServiceNames() {
        return m_serviceNames;
    }

    // com.sun.star.util.XCloseBroadcaster:
    public void addCloseListener(com.sun.star.util.XCloseListener Listener)
    {
        // TODO: Insert your implementation for "addCloseListener" here.
    }

    public void removeCloseListener(com.sun.star.util.XCloseListener Listener)
    {
        // TODO: Insert your implementation for "removeCloseListener" here.
    }

    // com.sun.star.util.XCloseable:
    public void close(boolean DeliverOwnership) throws com.sun.star.util.CloseVetoException
    {
        // TODO: Insert your implementation for "close" here.
    }

    // com.sun.star.task.XAsyncJob:
    public void executeAsync(com.sun.star.beans.NamedValue[] Arguments, com.sun.star.task.XJobListener Listener) throws com.sun.star.lang.IllegalArgumentException
    {
        System.out.println("executeAsync");
        save();
    }

    public void dispatch(URL aURL, PropertyValue[] pvs) {

        if (aURL.Protocol.compareTo("ru.ssau.graphplus:") == 0) {
            Link link = null;
            Node node = null;



            
            if (aURL.Path.compareTo("Omg") == 0){
               xDrawDoc = (XComponent) UnoRuntime.queryInterface(XComponent.class, m_xComponent);
                XDrawPage xDrawPage =  DrawHelper.getCurrentDrawPage(xDrawDoc);
                XPropertySet xPropertySet = QI.XPropertySet(xDrawPage);
                try {
                    Object width = xPropertySet.getPropertyValue("Width");
                    Object heigth = xPropertySet.getPropertyValue("Height");
                    Integer w = (Integer) width;
                    Integer h = (Integer) heigth;
                    try {
                        ArrayList<XShape> xShapes = new ArrayList<XShape>();
                        int FRAME_MARGIN = 1000;

                        XShape lineShape = DrawHelper.createLineShape(xDrawDoc, FRAME_MARGIN, FRAME_MARGIN, 0, h - 2 * FRAME_MARGIN);
                        OOoUtils.setIntProperty(lineShape, "LineWidth", 10);

                        xShapes.add(lineShape);

                        XShape lineShape1 = DrawHelper.createLineShape(xDrawDoc, FRAME_MARGIN, h - FRAME_MARGIN, w - 2000, 0);
                        OOoUtils.setIntProperty(lineShape1, "LineWidth", 10);
                        xShapes.add(lineShape1);

                        XShape lineShape2 = DrawHelper.createLineShape(xDrawDoc, w - FRAME_MARGIN, FRAME_MARGIN, 0, h - 2 * FRAME_MARGIN);
                        OOoUtils.setIntProperty(lineShape2, "LineWidth", 10);
                        xShapes.add(lineShape2);
                        XShape lineShape3 = DrawHelper.createLineShape(xDrawDoc, FRAME_MARGIN, FRAME_MARGIN, w - 2000, 0);
                        OOoUtils.setIntProperty(lineShape3, "LineWidth", 10);

                        xShapes.add(lineShape3);

                        XShape lineShape4 = DrawHelper.createLineShape(xDrawDoc, FRAME_MARGIN, 2 * FRAME_MARGIN, w - 2000, 0);


                        OOoUtils.setIntProperty(lineShape4, "LineWidth", 10);
                        xShapes.add(lineShape4);






                        XShape textShape = DrawHelper.createTextShape(xDrawDoc, FRAME_MARGIN, FRAME_MARGIN, w - 2 * FRAME_MARGIN, FRAME_MARGIN);
                        ShapeHelper.addPortion(textShape, "Name of diagram" , false);
                        xShapes.add(textShape);

                        DrawHelper.insertShapesOnCurrentPage(xShapes, xDrawDoc);
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                } catch (UnknownPropertyException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (WrappedTargetException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }




                try {


                    int height = OOoUtils.getIntProperty(xDrawDoc, "Height");
                } catch (UnknownPropertyException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (WrappedTargetException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                //DrawHelper.createLineShape(xDrawDoc, 10, 10)
            }


            if (aURL.Path.compareTo("Node") == 0) {

                try {
                    XDrawPage xDrawPage = DrawHelper.getDrawPageByIndex(xDrawDoc, 0);
                    XShape xShape = ShapeHelper.createShape(m_xComponent, new Point(800, 600), new Size(1500, 1500), "com.sun.star.drawing.EllipseShape");// .createEllipseShape(m_xComponent, 800, 800, 1500, 1500);
                    node = NodeFactory.create(Node.NodeType.Client, m_xComponent);
                    XPropertySet xPS = QI.XPropertySet(xShape);
//                    XEnumerableMap em = EnumerableMap.create(m_xContext, Type.STRING, Type.ANY);

                    try {
                        Object ncObj = xMSF.createInstance("com.sun.star.drawing.BitmapTable");
                        XNameContainer xNamedCont = (XNameContainer) QI.XNameContainer(ncObj);
                        xNamedCont.insertByName("OOOOOMG", "STUPID OPENOFFICE");
                        Object cloneObj;// = ncObj.clone();
                        xPS.setPropertyValue("ShapeUserDefinedAttributes", ncObj);
                        System.out.println("qwe");
                    } catch (ServiceNotRegisteredException ex) {
                    }

                    XNameContainer xNC = QI.XNameContainer(xPS.getPropertyValue("ShapeUserDefinedAttributes"));

                    XPropertySet xPropSet = QI.XPropertySet(xShape);
//                    xPropSet.setPropertyValue("SizeProtect", true);
                    printInfo(xShape);

                    XComponent xCompShape = (XComponent) UnoRuntime.queryInterface(XComponent.class, xShape);
                    xCompShape.addEventListener(new XEventListener() {

                        public void notifyEvent(EventObject arg0) {
                            System.err.println(arg0.EventName);
                            //                    throw new UnsupportedOperationException("Not supported yet.");
                        }

                        public void disposing(com.sun.star.lang.EventObject arg0) {
                            //                    throw new UnsupportedOperationException("Not supported yet.");
                        }
                    });


                    Misc.tagShapeAsNode(xShape);
                    xDrawPage.add(xShape);

                    return;

                } catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                } catch (WrappedTargetException ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                } catch (java.lang.Exception ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
            }


            if (aURL.Path.compareTo("ProcessNode") == 0) {
                try {

                    xDrawDoc = (XComponent) UnoRuntime.queryInterface(
                            XComponent.class, m_xComponent);

                    XDrawPage xPage = PageHelper.getDrawPageByIndex(xDrawDoc, 0);
                    XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, xPage);


                    node = NodeFactory.create(Node.NodeType.Process, m_xComponent);//createAndInsert(Node.NodeType.Process, m_xComponent, xShapes);




                    XPropertySet xPS = QI.XPropertySet(node.getShape());



                    XNameContainer xNC2 = QI.XNameContainer(xPS.getPropertyValue("UserDefinedAttributes"));



                    DrawHelper.insertShapeOnCurrentPage(node.getShape(), xDrawDoc);

                    Misc.addUserDefinedAttributes(node.getShape(), xMSF);

                    Misc.tagShapeAsNode(node.getShape());
                    Misc.setNodeType(node.getShape(), Node.NodeType.Process);
                    DrawHelper.setShapePositionAndSize(node.getShape(), 100, 100, 1800, 1500);
                    Gui.createDialogForShape2(node.getShape(), m_xContext, new HashMap<String, XShape>());
                    //return;

                } catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                } catch (WrappedTargetException ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (aURL.Path.compareTo("ProcedureNode") == 0) {
                try {
                    //                Node processNode = NodeFactory.create(Node.NodeType.Process, m_xComponent);
                    m_xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, m_xFrame.getController().getModel());
                    // add your own code here

                    xDrawDoc = (XComponent) UnoRuntime.queryInterface(
                            XComponent.class, m_xComponent);


                    XDrawPage xPage = PageHelper.getDrawPageByIndex(xDrawDoc, 0);
                    XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, xPage);
                    Node procedureNode = NodeFactory.create(Node.NodeType.Procedure, m_xComponent);//createAndInsert(Node.NodeType.Process, m_xComponent, xShapes);
                    node = procedureNode;


                    DrawHelper.insertShapeOnCurrentPage(procedureNode.getShape(), xDrawDoc);
                    
                    Misc.addUserDefinedAttributes(procedureNode.getShape(), xMSF);
                    Misc.setNodeType(procedureNode.getShape(), Node.NodeType.Procedure);

                    Misc.tagShapeAsNode(procedureNode.getShape());

                    DrawHelper.setShapePositionAndSize(procedureNode.getShape(), 100, 100, 2000, 1500);
                    Gui.createDialogForShape2(procedureNode.getShape(), m_xContext, new HashMap<String, XShape>());

                    //return;

                } catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                } catch (WrappedTargetException ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
            }


            if (aURL.Path.compareTo("ClientNode") == 0) {
                try {

                    m_xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, m_xFrame.getController().getModel());
                    // add your own code here

                    xDrawDoc = (XComponent) UnoRuntime.queryInterface(
                            XComponent.class, m_xComponent);
                    Node processNode = NodeFactory.create(Node.NodeType.Client, m_xComponent);
                    node = processNode;

                    

                    DrawHelper.insertShapeOnCurrentPage(processNode.getShape(), xDrawDoc);
                    
                    Misc.addUserDefinedAttributes(processNode.getShape(), xMSF);
                    Misc.tagShapeAsNode(processNode.getShape());
                    Misc.setNodeType(processNode.getShape(), Node.NodeType.Client);

                    DrawHelper.setShapePositionAndSize(processNode.getShape(), 100, 100, 1500, 1500);
                    Gui.createDialogForShape2(processNode.getShape(), m_xContext, new HashMap<String, XShape>());

                    System.out.println("omg");
                    //return;
                } catch (java.lang.Exception ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }

            }

            if (aURL.Path.compareTo("ServerNode") == 0) {
                try {
                    Node serverNode = NodeFactory.create(Node.NodeType.Server, m_xComponent);
                    node = serverNode;
                    xDrawDoc = (XComponent) UnoRuntime.queryInterface(
                            XComponent.class, m_xComponent);
                    DrawHelper.insertShapeOnCurrentPage(serverNode.getShape(), xDrawDoc);
                    
                    Misc.addUserDefinedAttributes(serverNode.getShape(), xMSF);
                    Misc.tagShapeAsNode(serverNode.getShape());
                    Misc.setNodeType(serverNode.getShape(), Node.NodeType.Server);

                    
                    DrawHelper.setShapePositionAndSize(serverNode.getShape(), 100, 100, 1500, 1500);
                    Gui.createDialogForShape2(serverNode.getShape(), m_xContext, new HashMap<String, XShape>());
                    //return;
                    //                } catch (Exception ex) {
                    //                }
                    //                }
                } catch (PropertyVetoException ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);

                }
            }





            if (aURL.Path.compareTo("LinkLink") == 0) {
                xDrawDoc = (XComponent) UnoRuntime.queryInterface(
                        XComponent.class, m_xComponent);
                Link linkLink = LinkFactory.create(Link.LinkType.Link, xDrawDoc, DrawHelper.getCurrentDrawPage(xDrawDoc));
                link = linkLink;
                for (XShape shape : linkLink.getShapes()) {
                    DrawHelper.insertShapeOnCurrentPage(shape, xDrawDoc);
                }
                
                    //Misc.addUserDefinedAttributes(link.getShapes(), xMSF);
                    //Misc.tagShapeAsNode(processNode.getShape());
                    //Misc.setNodeType(processNode.getShape(), Node.NodeType.Client);


                _frameObjectList.get(0).getController().setState(Controller.State.InputTwoShapes);
                _frameObjectList.get(0).getController().setLinker(linkLink);

               // return;
            }

            if (aURL.Path.compareTo("MessageLink") == 0) {
                xDrawDoc = (XComponent) UnoRuntime.queryInterface(
                        XComponent.class, m_xComponent);
                Link messageLink = LinkFactory.create(Link.LinkType.Message, xDrawDoc, DrawHelper.getCurrentDrawPage(xDrawDoc));

                for (XShape shape : messageLink.getShapes()) {
                    DrawHelper.insertShapeOnCurrentPage(shape, xDrawDoc);
                }

                _frameObjectList.get(0).getController().setState(Controller.State.InputTwoShapes);
                _frameObjectList.get(0).getController().setLinker(messageLink);

                //return;
            }

            if (aURL.Path.compareTo("ControlLink") == 0) {
                xDrawDoc = (XComponent) UnoRuntime.queryInterface(
                        XComponent.class, m_xComponent);
                Link controlLink = LinkFactory.create(Link.LinkType.Control, xDrawDoc, DrawHelper.getCurrentDrawPage(xDrawDoc));

                for (XShape shape : controlLink.getShapes()) {
                    DrawHelper.insertShapeOnCurrentPage(shape, xDrawDoc);
                }

                _frameObjectList.get(0).getController().setState(Controller.State.InputTwoShapes);
                _frameObjectList.get(0).getController().setLinker(controlLink);

                //return;
            }

            if (aURL.Path.compareTo("Save") == 0) {
                save();
                return;
            }

            if (aURL.Path.compareTo("Tag") == 0) {
                // add your own code here
                System.out.println("Tag");
                if (Status.isTagAllNewShapes()) {
                    Status.setTagAllNewShapes(false);
                } else {
                    Status.setTagAllNewShapes(true);
                }
                return;
            }

            if (aURL.Path.compareTo("Assoc") == 0) {
                // add your own code here
                System.out.println("Assoc");

                return;
            }
            
            if (aURL.Path.contains("Link")) {
                // common for all links
                if (link != null)
                diagramModel.addDiagramElement(link);
                return;
            }
            
            if (aURL.Path.contains("Node")) {
                // common for all links
                if (node != null)
                diagramModel.addDiagramElement(node);
                return;
            }

        }


    }

    public void addStatusListener(XStatusListener xl, URL url) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeStatusListener(XStatusListener xl, URL url) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void save(){
        SystemDialog sd = new SystemDialog(m_xContext, xMCF);
        String path = sd.raiseSaveAsDialog();
        save(path);

    }


    public void save(String path) {
        try {
            // add your own code here
            System.out.println("Save");

            XComponent xDD = null;
            m_xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, m_xFrame.getController().getModel());
            // add your own code here
            xDrawDoc = (XComponent) UnoRuntime.queryInterface(
                    XComponent.class, m_xComponent);



            com.sun.star.drawing.XDrawPagesSupplier xDPS =
                    (com.sun.star.drawing.XDrawPagesSupplier) UnoRuntime.queryInterface(
                            com.sun.star.drawing.XDrawPagesSupplier.class, xDrawDoc);
            com.sun.star.drawing.XDrawPages xDPn = xDPS.getDrawPages();
            com.sun.star.container.XIndexAccess xDPi =
                    (com.sun.star.container.XIndexAccess) UnoRuntime.queryInterface(
                            com.sun.star.container.XIndexAccess.class, xDPn);
            XDrawPage xDrawPage = (com.sun.star.drawing.XDrawPage) UnoRuntime.queryInterface(
                    com.sun.star.drawing.XDrawPage.class, xDPi.getByIndex(0));

            XMLGenerator.generateXMLforDocument(xDrawDoc, xDrawPage, path);

            Gui.showErrorMessageBox(null, "Saved", path, xMCF, m_xContext);
            return;
        }catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
        }catch (WrappedTargetException ex) {
            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
        public void printInfo(Object obj) {
        XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, obj);
        XPropertySetInfo xPSI = xPropSet.getPropertySetInfo();
        Property[] props = xPSI.getProperties();
        for (Property prop : props) {
            System.out.println(prop.Name);
            System.out.println(prop.Type);
            System.out.println("=======");
        }
        System.out.println("===END====");
    }

    public void notifyEvent(EventObject eo) {
       // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void disposing(EventObject arg0) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
     public void disposing(com.sun.star.lang.EventObject arg0) {
       // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void frameAction(FrameActionEvent fae) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public void chooseLinkType(XShape xShape){

             chooseTypeDialog(xMCF, xShape);


    }



    public short chooseTypeDialog(XMultiComponentFactory _xMCF, final XShape xShape){
        try {
            Object oDialogModel =  _xMCF.createInstanceWithContext("com.sun.star.awt.UnoControlDialogModel", m_xContext);

            // The XMultiServiceFactory of the dialogmodel is needed to instantiate the controls...
            XMultiServiceFactory m_xMSFDialogModel = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, oDialogModel);

            // The named container is used to insert the created controls into...
            final XNameContainer m_xDlgModelNameContainer = (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class, oDialogModel);

            // create the dialog...
            Object oUnoDialog = _xMCF.createInstanceWithContext("com.sun.star.awt.UnoControlDialog", m_xContext);
            XControl m_xDialogControl = (XControl) UnoRuntime.queryInterface(XControl.class, oUnoDialog);

            // The scope of the control container is public...
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
                    "Width", new Integer(100));
            xPSetDialog.setPropertyValue(
                    "Height", new Integer(50));


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
            xButton.addActionListener(new MyXActionListener(xShape) {
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
                            Link linkReplace = LinkFactory.create(Link.LinkType.valueOf(selectedItem), m_xComponent, DrawHelper.getCurrentDrawPage(m_xComponent), xShStart, xShEnd, false);
                            Linker linker = new LinkerImpl(linkReplace, xConnectorShape);
                            linker.link(xShStart, xShEnd);


                        } catch (UnknownPropertyException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        } catch (WrappedTargetException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }

                        //To change body of implemented methods use File | Settings | File Templates.


                    } catch (NoSuchElementException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (WrappedTargetException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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

    private class MyXActionListener implements XActionListener {
        private XShape xShape;

        private MyXActionListener(XShape xShape) {
            this.xShape = xShape;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            System.out.println("act");
        }

        @Override
        public void disposing(com.sun.star.lang.EventObject eventObject) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }




}
