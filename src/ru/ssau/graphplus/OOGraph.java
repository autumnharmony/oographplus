package ru.ssau.graphplus;


import com.sun.star.awt.*;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.uno.*;
import com.sun.star.uno.Exception;
import ru.ssau.graphplus.gui.ContextMenuInterceptor;
import ru.ssau.graphplus.gui.Gui;
import ru.ssau.graphplus.gui.SystemDialog;
import ru.ssau.graphplus.link.Link;
import ru.ssau.graphplus.link.LinkFactory;
import ru.ssau.graphplus.link.Linker;
import ru.ssau.graphplus.link.LinkerImpl;
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
import com.sun.star.util.URL;
import com.sun.star.util.XModifiable;
import com.sun.star.view.XSelectionSupplier;

import java.util.ArrayList;
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


    
    static XMultiComponentFactory xMCF = null;
    static XMultiServiceFactory xMSF = null;
    static XComponent xDrawDoc = null;
    // store every frame with its DiagramController object
    private static ArrayList<FrameObject> _frameObjectList = null;
    private static XComponent m_xComponent = null;

    private static DiagramController diagramController = null;
    private static DiagramModel diagramModel;



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

            XDispatchProviderInterception dispatchProviderInterception = QI.XDispatchProviderInterception(m_xFrame);



            XController xController = m_xFrame.getController();

            XContextMenuInterception xContMenuInterception =
                    (XContextMenuInterception) UnoRuntime.queryInterface(XContextMenuInterception.class, xController);
            xContMenuInterception.registerContextMenuInterceptor(new ContextMenuInterceptor(m_xContext));


            xMCF = m_xContext.getServiceManager();
//            xMSF = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, xMCF);

            m_xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, m_xFrame.getController().getModel());
            xMSF = (XMultiServiceFactory) UnoRuntime.queryInterface(
                    XMultiServiceFactory.class, m_xComponent);


            XEventBroadcaster xEB = (XEventBroadcaster) UnoRuntime.queryInterface(XEventBroadcaster.class, m_xComponent);
            xEB.addEventListener(new XEventListener() {

                public void notifyEvent(EventObject arg0) {
                    System.out.println("notifyEvent m_xComponent");
                    System.out.print(arg0.EventName);
                    if (arg0.EventName.equals("ShapeInserted")){
                        diagramController.onShapeInserted(arg0);
                    }

                    if (arg0.EventName.equals("ShapeModified")){
                        diagramController.onShapeModified(arg0);
                    }
                }

                public void disposing(com.sun.star.lang.EventObject arg0) {
                    //throw new UnsupportedOperationException("Not supported yet.");
                }
            });

            XDrawPage xDrawPage = null; // = DrawHelper.getDrawPageByIndex(m_xComponent, 0);
            XShape xShape = null; // = DrawHelper.createEllipseShape(m_xComponent, 200, 200, 500, 500);
            XComponent xCompShape; // = (XComponent) UnoRuntime.queryInterface(XComponent.class, xShape);



            // add the m_xFrame and its diagramController to the static arrayList of _frameObjectList
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
//                    XEventBroadcaster xEB2 = (XEventBroadcaster) UnoRuntime.queryInterface(XEventBroadcaster.class, m_xFrame);
//                    xEB2.addEventListener(new XEventListener() {
//                        @Override
//                        public void notifyEvent(EventObject eventObject) {
//                            System.out.println(" m_xFrame notifyEvent");
//                            System.out.print(eventObject.EventName);
//                        }
//
//                        @Override
//                        public void disposing(com.sun.star.lang.EventObject eventObject) {
//
//                        }
//                    });

                    xDrawDoc = (XComponent) UnoRuntime.queryInterface(
                            XComponent.class, m_xComponent);
                    // *************

                    xDrawPage = DrawHelper.getDrawPageByIndex(m_xComponent, 0);

//                    XEventBroadcaster xEB3 = (XEventBroadcaster) UnoRuntime.queryInterface(XEventBroadcaster.class, xDrawPage);
//                    xEB3.addEventListener(new XEventListener() {
//                        @Override
//                        public void notifyEvent(EventObject eventObject) {
//                            System.out.println("notifyEvent");
//                            System.out.print(eventObject.EventName);
//                        }
//
//                        @Override
//                        public void disposing(com.sun.star.lang.EventObject eventObject) {
//
//                        }
//                    });

                    XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class,
                            xDrawPage);

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


                     if (diagramModel == null){
                        diagramModel = new DiagramModel();
                    }

                    if (diagramController == null) {
                        diagramController = new DiagramController(m_xContext, m_xFrame, xMSF, xMCF, xDrawPage, diagramModel, m_xComponent, xDrawDoc);
                    }    


                    if (m_xComponent != null) {
                        m_xComponent.addEventListener(this);
                    }


                   
                    // when the frame is closed we have to remove FrameObject item into the list
                    _frameObjectList.add(new FrameObject(m_xFrame, diagramController));
                } catch (java.lang.Exception ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {

            }



            XDispatchProvider xDispatchProvider = QI.XDispatchProvider(m_xFrame);
            XDispatchProviderInterception xDPI = (XDispatchProviderInterception) UnoRuntime.queryInterface(XDispatchProviderInterception.class, m_xFrame);



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

    public com.sun.star.frame.XDispatch queryDispatch(com.sun.star.util.URL aURL, String TargetFrameName, int SearchFlags)
    {
        if (aURL.Protocol.compareTo("ru.ssau.graphplus:") == 0) {

            return diagramController;
        }
        return null;
    }

    public com.sun.star.frame.XDispatch[] queryDispatches(com.sun.star.frame.DispatchDescriptor[] seqDescriptors)
    {
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




    }

    private void createFrame() {
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
                ShapeHelper.addPortion(textShape, "Name of diagram", false);
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
    
        public static void printInfo(Object obj) {
        XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, obj);
        XPropertySetInfo xPSI = xPropSet.getPropertySetInfo();
        Property[] props = xPSI.getProperties();
        for (Property prop : props) {
            System.out.println(prop.Name);
            System.out.println(prop.Type);
            try {
                Object propertyValue = xPropSet.getPropertyValue(prop.Name);
                if (propertyValue instanceof Size){
                    Size size = (Size) propertyValue;
                    System.out.println(size.Height);
                    System.out.println(size.Width);
                }
                if (propertyValue instanceof Point){
                    Point point = (Point) propertyValue;
                    System.out.println(point.X);
                    System.out.println(point.Y);
                }
            } catch (UnknownPropertyException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (WrappedTargetException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

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









}
