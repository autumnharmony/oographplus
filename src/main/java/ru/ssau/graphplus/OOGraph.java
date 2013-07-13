package ru.ssau.graphplus;


import com.sun.star.awt.*;
import com.sun.star.beans.*;
import com.sun.star.container.XNameContainer;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.document.*;
import com.sun.star.document.XEventListener;
import com.sun.star.drawing.*;
import com.sun.star.frame.*;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.*;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.registry.InvalidRegistryException;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.report.XImageControl;
import com.sun.star.table.*;
import com.sun.star.ui.XContextMenuInterception;
import com.sun.star.uno.*;
import com.sun.star.uno.Exception;
import com.sun.star.util.URL;
import com.sun.star.util.XModifiable;
import com.sun.star.util.XModifyListener;
import com.sun.star.view.XSelectionSupplier;
import ru.ssau.graphplus.gui.*;
import ru.ssau.graphplus.link.InputTwoShapesMode;
import ru.ssau.graphplus.link.Link;
import ru.ssau.graphplus.link.LinkFactory;
import ru.ssau.graphplus.node.Node;
import ru.ssau.graphplus.node.NodeBase;
import ru.ssau.graphplus.node.NodeFactory;
import ru.ssau.graphplus.xml.XMLGenerator;
import ru.ssau.graphplus.xml.XMLTransform;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class OOGraph extends WeakBase implements
        com.sun.star.lang.XInitialization,
        com.sun.star.task.XJob,
        com.sun.star.frame.XDispatchProvider,
        com.sun.star.lang.XServiceInfo,
        com.sun.star.util.XCloseable,
        com.sun.star.task.XAsyncJob,
        com.sun.star.frame.XDispatch,
        com.sun.star.frame.XFrameActionListener,
        com.sun.star.document.XEventListener {

    public static final String LINK_LINK = "LinkLink";
    public static final String MESSAGE_LINK = "MessageLink";
    public static final String CONTROL_LINK = "ControlLink";
    public static final String SERVER_NODE = "ServerNode";
    public static final String CLIENT_NODE = "ClientNode";
    public static final String PROCESS_NODE = "ProcessNode";
    public static final String PROCEDURE_NODE = "ProcedureNode";
    private static final String m_implementationName = OOGraph.class.getName();
    private static final String[] m_serviceNames = {
            "com.sun.star.frame.ProtocolHandler",
            "com.sun.star.task.Job",
            "com.sun.star.task.AsyncJob"
    };

    private static List<WeakReference<OOGraph>> weakReferences = new ArrayList<>();

    private com.sun.star.frame.XFrame m_xFrame;
    private static XMultiComponentFactory xMCF = null;
    private static XMultiServiceFactory xMSF = null;
    private static XComponent xDrawDoc = null;
    private static ArrayList<FrameObject> _frameObjectList = null;
    private static XComponent m_xComponent = null;
    private final XComponentContext m_xContext;
    private Map<MyURL, Set<XStatusListener>> statusListeners = new HashMap<>();
    private DiagramController diagramController;
    private DiagramModel diagramModel;
    private String diagramName;
    private String diagramType;
    private NodeFactory nodeFactory;
    private LinkFactory linkFactory;
    private XEventBroadcaster m_xEventBroadcaster = null;
    private boolean isAliveDocumentEventListener = false;

    private static final ArrayList<String> m_aSupportedModules = new ArrayList(1);

    private static WeakHashMap<XFrame, DiagramModel> frameToDM = new WeakHashMap<>();

    static {
        m_aSupportedModules.add("com.sun.star.drawing.DrawingDocument");
    }

    public static StatusBarInterceptionController aController;

    public OOGraph(XComponentContext context) {
        Thread thread = Thread.currentThread();

        Logger logger = Logger.getLogger("oograph");
        logger.info("OOGraph ctor");
        logger.info("Thread " + thread.getName());

        weakReferences.add(new WeakReference<>(this));
        m_xContext = context;

        try {
            this.m_xModuleManager = UnoRuntime.queryInterface(XModuleManager.class, this.m_xContext.getServiceManager().createInstanceWithContext("com.sun.star.frame.ModuleManager", this.m_xContext));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public static OOGraph temp(XComponentContext xComponentContext){
        return new OOGraph(xComponentContext);
    }

    public class OOGraphProxy extends OOGraph

    public void frame(XFrame xFrame){
        this.m_xFrame = xFrame;
        MyComponentFactory.frameToOOGraph.put(xFrame,this);
    }

    private static class MyComponentFactory implements XSingleComponentFactory{

        public static WeakHashMap<XComponentContext, OOGraph> map =  new WeakHashMap<>();
        public static WeakHashMap<XFrame, OOGraph> frameToOOGraph = new WeakHashMap<>();
        public static WeakReference<OOGraph> weakReference;


        @Override
        public Object createInstanceWithContext(XComponentContext xComponentContext) throws Exception {

            OOGraph temp = OOGraph.temp(xComponentContext);
            weakReference = new WeakReference<OOGraph>(temp);
            return temp;
//            return map.get(xComponentContext);
        }

        @Override
        public Object createInstanceWithArgumentsAndContext(Object[] objects, XComponentContext xComponentContext) throws Exception {
            System.out.println("createInstanceWithArgumentAndContext");
            return new OOGraph(xComponentContext);
        }
    }

    public static XSingleComponentFactory __getComponentFactory(String sImplementationName) {
        XSingleComponentFactory xFactory = null;

        if (sImplementationName.equals(m_implementationName))
            xFactory = new MyComponentFactory();
//            xFactory = Factory.createComponentFactory(OOGraph.class, m_serviceNames);
        return xFactory;
    }

    public static boolean __writeRegistryServiceInfo(XRegistryKey xRegistryKey) {
        try {
            String[] keyNames;

            keyNames = xRegistryKey.getKeyNames();
            for (int i = 0; i < keyNames.length; i++){
                System.out.println(keyNames[i]);
            }
        } catch (InvalidRegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return Factory.writeRegistryServiceInfo(m_implementationName,
                m_serviceNames,
                xRegistryKey);
    }

    public static void printInfo(Object obj) {
//        XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, obj);
//        XPropertySetInfo xPSI = xPropSet.getPropertySetInfo();
//        Property[] props = xPSI.getProperties();
//        for (Property prop : props) {
//            System.out.println(prop.Name);
//            System.out.println(prop.Type);
//            try {
//                Object propertyValue = xPropSet.getPropertyValue(prop.Name);
//                if (propertyValue instanceof Size) {
//                    Size size = (Size) propertyValue;
//                    System.out.println(size.Height);
//                    System.out.println(size.Width);
//                }
//                if (propertyValue instanceof Point) {
//                    Point point = (Point) propertyValue;
//                    System.out.println(point.X);
//                    System.out.println(point.Y);
//                }
//            } catch (UnknownPropertyException e) {
//                e.printStackTrace();
//            } catch (WrappedTargetException e) {
//                e.printStackTrace();
//            }
//
//            System.out.println("=======");
//        }
//        System.out.println("===END====");
    }

    public String getDiagramType() {
        return diagramType;
    }

    public DiagramModel getOrCreateDiagramModel(int magic) {
        if (diagramModel == null) {
            System.out.println("diagramModel null");
            int i1 = weakReferences.indexOf(new WeakReference<OOGraph>(this));
            int i2 = i1;

            List<WeakReference<OOGraph>> toRemove = new ArrayList<>();

            for (int i = 0; i < weakReferences.size(); i++) {
                WeakReference<OOGraph> weak = weakReferences.get(i);
                if (weak != null) {
                    OOGraph ooGraph = weak.get();
                    if (ooGraph!=null && ooGraph.equals(this)) {
                        i2 = i;
                    }

                    if (ooGraph == null){
                        toRemove.add(weak);
                    }
                }
            }

            for (WeakReference reference : toRemove){
                weakReferences.remove(reference);
            }

            int i = i2 / 10 * 10;
            DiagramModel diagramModel1 = weakReferences.get(i).get().getDiagramModel();
            if (diagramModel1 == null) {
                diagramModel1 = new DiagramModel();
            }
            this.diagramModel = diagramModel1;
            return diagramModel1;

        } else {
            return diagramModel;
        }
    }

    public DiagramModel getDiagramModel() {
        return diagramModel;
    }

    public DiagramController getDiagramController() {
        if (diagramController == null) {
            System.out.println("diagramController null");
            //TODO
//            int i1 = weakReferences.indexOf(new WeakReference<OOGraph>(this));
            int i2 = 0;

            DiagramController diagramController1 = null;
            for (int i = 0; i < weakReferences.size(); i++) {
                WeakReference<OOGraph> ooGraphWeakReference = weakReferences.get(i);
                if (ooGraphWeakReference.get().equals(this)) {
                    i2 = i;
                    int j = i2 / 10 * 10;
                    diagramController1 = weakReferences.get(j).get().diagramController;
                    this.diagramController = diagramController1;
                    if (diagramController1 != null)
                        break;
                }
            }


            return diagramController1;
        } else {
            return diagramController;
        }
    }

    public void addEventListener() {
        if (!this.isAliveDocumentEventListener) {
            this.m_xEventBroadcaster.addEventListener(this);
            this.isAliveDocumentEventListener = true;
        }
    }

    public void removeEventListener() {
        if (this.isAliveDocumentEventListener) {
            this.m_xEventBroadcaster.removeEventListener(this);
            this.isAliveDocumentEventListener = false;
        }
    }

    // com.sun.star.lang.XInitialization:
    @Override
    public void initialize(Object[] object) throws com.sun.star.uno.Exception {

        Logger.getAnonymousLogger().info("OOGraph initialize");

        if (object.length > 0) {

            m_xFrame = UnoRuntime.queryInterface(XFrame.class, object[0]);

            MyComponentFactory.weakReference.get().frame(m_xFrame);

            XController xController = m_xFrame.getController();

            XContextMenuInterception xContMenuInterception =
                    UnoRuntime.queryInterface(XContextMenuInterception.class, xController);

            xMCF = m_xContext.getServiceManager();
            m_xComponent = UnoRuntime.queryInterface(XComponent.class, m_xFrame.getController().getModel());
            xMSF = UnoRuntime.queryInterface(
                    XMultiServiceFactory.class, m_xComponent);

            nodeFactory = new NodeFactory(xMSF);
            linkFactory = new LinkFactory(xMSF);

            XDrawPage xDrawPage = DrawHelper.getDrawPageByIndex(m_xComponent, 0);

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

                    if (diagramController == null) {
                        diagramModel = new DiagramModel();

                        XComponent xDrawDoc = UnoRuntime.queryInterface(
                                XComponent.class, m_xComponent);
                        diagramController = new DiagramController(m_xContext, m_xFrame, xMSF, xMCF, diagramModel, xDrawDoc);
                        xContMenuInterception.registerContextMenuInterceptor(new ContextMenuInterceptor(m_xContext, diagramController));

                        addDocumentEventListener(m_xComponent);


                        xDrawPage = DrawHelper.getDrawPageByIndex(m_xComponent, 0);


                        if (diagramModel == null) {
                            diagramModel = new DiagramModel();
                        }

                        if (diagramController == null) {
                            diagramController = new DiagramController(m_xContext, m_xFrame, xMSF, xMCF, diagramModel, xDrawDoc);

                        }


                        // when the frame is closed we have to remove FrameObject item into the list
                        _frameObjectList.add(new FrameObject(m_xFrame, diagramController, diagramModel));


                        this.m_xEventBroadcaster = UnoRuntime.queryInterface(XEventBroadcaster.class, this.m_xFrame.getController().getModel());
                        addEventListener();
                    }
                } catch (java.lang.Exception ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                }

                XDispatchProvider xDispatchProvider = QI.XDispatchProvider(m_xFrame);
                XDispatchProviderInterception xDPI = (XDispatchProviderInterception) UnoRuntime.queryInterface(XDispatchProviderInterception.class, m_xFrame);
//            xDPI.registerDispatchProviderInterceptor(new MyInterceptor(m_xFrame, xDispatchProvider, null ));


                System.out.println("getting Drawpage");
                com.sun.star.drawing.XDrawPagesSupplier xDPS = UnoRuntime.queryInterface(
                        XDrawPagesSupplier.class, m_xComponent);

                com.sun.star.drawing.XDrawPages xDPn = xDPS.getDrawPages();
                com.sun.star.container.XIndexAccess xDPi = UnoRuntime.queryInterface(com.sun.star.container.XIndexAccess.class, xDPn);
                final XDrawPage xDP = UnoRuntime.queryInterface(
                        XDrawPage.class, xDPi.getByIndex(0));
                XModifiable xMod = UnoRuntime.queryInterface(
                        XModifiable.class, xDrawDoc);
                xMod.addModifyListener(diagramController);

            } else {
                System.out.println("not new frame");
            }


        }
    }


    private void addDocumentEventListener(XComponent m_xComponent) {
        XDocumentEventBroadcaster xDEB = UnoRuntime.queryInterface(XDocumentEventBroadcaster.class, m_xComponent);

        xDEB.addDocumentEventListener(new XDocumentEventListener() {
            @Override
            public void documentEventOccured(DocumentEvent documentEvent) {
                System.out.println("documentEvent");
                if (documentEvent.EventName.equals("OnSaveDone") || documentEvent.EventName.equals("OnSaveAsDone")) {

                    diagramModel.refreshLinksShapesId();

                    String url = QI.XModel(m_xFrame.getController().getModel()).getURL();
                    String substring = url.substring(7);
                    String[] split = url.split("/");
                    String name = split[split.length - 1];
                    String ext = substring.substring(substring.length() - 4, substring.length());
                    diagramModel.refreshModel();
                    try {

                        XWindowPeer xWindowPeer = UnoRuntime.queryInterface(XWindowPeer.class, m_xFrame.getContainerWindow());
                        if (xWindowPeer == null) {
                            xWindowPeer = UnoRuntime.queryInterface(XWindowPeer.class, m_xFrame.getContainerWindow());
                        }
                        ArrayList<DiagramElement> incorrect = new ArrayList<DiagramElement>();
                        boolean valid = diagramModel.isValid();
                        if (!valid) {
                            List<DiagramElement> invalid = diagramModel.getInvalid();
                            String s = "";
                            for (DiagramElement diagramElement : invalid) {
                                s += diagramElement.getName() + " ";
                            }
                            UnoAwtUtils.showErrorMessageBox(xWindowPeer, "Warning", "Invalid elements " + s);
                        }

//                        boolean correct = Misc.checkNotConnectedConnectors(diagramModel, xDrawDoc, incorrect);
//                        if (!correct) {
//
//
//
//                            UnoAwtUtils.showErrorMessageBox(xWindowPeer, "Warning", "Not connected connectors");
//                            // TODO maybe highlight uncorrect
//                        } else {
//
//                        }
                        String xmlString = XMLGenerator.generateXMLStringByDiagramModel(diagramModel, xDrawDoc);


                        File f = null;
                        FileWriter fw = null;


                        if (".odg".equals(ext.toLowerCase())) {
                            String path = substring.replace(".odg", "").concat(".xml");

                            f = new File(path);

                            if (!f.exists()) {
                                try {
                                    f.createNewFile();


                                } catch (IOException ex) {
                                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                                }


                            }
                            try {

                                System.out.println(f.getCanonicalPath());
                                fw = new FileWriter(f);

                                DiagramType diagramType1 = diagramModel.getDiagramType();

                                XMLTransform xmlTransform = new XMLTransform();
                                String transform = xmlTransform.transform(xmlString, diagramType1);

                                System.out.println(transform);

                                fw.append(transform);

                            } catch (IOException ex) {
                                Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                            } finally {
                                try {
                                    fw.close();
                                } catch (java.lang.Exception ignore) {
                                }


                            }
                        }


                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(substring.replace(".odg", "").concat(".model"))));
                        objectOutputStream.writeObject(diagramModel);
                        objectOutputStream.close();


                    } catch (java.lang.Exception e) {

                    }
                } else if (documentEvent.EventName.equals("OnLoad")) {

                    XComponent drawDoc = QI.XComponent(documentEvent.Source);
                    XDrawPage currentDrawPage = DrawHelper.getCurrentDrawPage(drawDoc);

                    DiagramModel diagramModel_ = getOrCreateDiagramModel(2);
                    if (!diagramModel_.isRestored()) {
                        diagramModel_ = new DiagramModel();
                        diagramModel_.restore(currentDrawPage, xMSF, drawDoc);

                    } else {

                    }

                    OOGraph.this.diagramModel = diagramModel_;

                }
                System.out.println(documentEvent.EventName);
            }

            @Override
            public void disposing(EventObject eventObject) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }

    private XModel2 m_xModel;
    private XModuleManager m_xModuleManager;

    // com.sun.star.task.XJob:
    public Object execute(com.sun.star.beans.NamedValue[] Arguments) throws com.sun.star.lang.IllegalArgumentException, com.sun.star.uno.Exception {
        // TODO: Exchange the default return implementation for "execute" !!!
        // NOTE: Default initialized polymorphic structs can cause problems
        // because of missing default initialization of primitive types of
        // some C++ compilers or different Any initialization in Java and C++
        // polymorphic structs.

        String eventNameString = null;
        XModel model = null;

        for (NamedValue namedValue : Arguments) {
            String name = namedValue.Name;
            Object value = namedValue.Value;
            if (name.equals("Environment")) {
                NamedValue[] values = (NamedValue[]) value;
                for (NamedValue inner : values) {
                    String name1 = inner.Name;
                    Object value1 = inner.Value;
                    if (name1.equals("EventName")) {
                        eventNameString = (String) value1;
                    }
                    if (name1.equals("Model")) {
                        model = QI.XModel(value1);
                    }
                }
            }
        }


        try
        {
            NamedValue[] aEnvironment = null;
            for (int i = 0; i < Arguments.length; i++) {
                if (Arguments[i].Name.equals("Environment")) {
                    aEnvironment = (NamedValue[])AnyConverter.toArray(Arguments[i].Value);

                    break;
                }
            }

            if (aEnvironment == null) {
                throw new IllegalArgumentException("no environment");
            }

            String sEnvType = null;
            String sEventName = null;
            this.m_xModel = null;
            for (int i = 0; i < aEnvironment.length; i++) {
                String sName = aEnvironment[i].Name;
                if (sName.equals("EnvType")) {
                    sEnvType = AnyConverter.toString(aEnvironment[i].Value);
                }
                else if (sName.equals("EventName")) {
                    sEventName = AnyConverter.toString(aEnvironment[i].Value);
                }
                else if (sName.equals("Model")) {
                    this.m_xModel = ((XModel2)UnoRuntime.queryInterface(XModel2.class, aEnvironment[i].Value));
                }

            }

            if ((sEnvType == null) || (!sEnvType.equals("DOCUMENTEVENT"))) {
                throw new IllegalArgumentException("Invalid event type! This Job only works with document events.");
            }

            if ((sEventName == null) || (!sEventName.equals("onDocumentOpened") && !sEventName.equals("OnLoad")) && !sEventName.equals("OnNew")) {
                throw new IllegalArgumentException("Invalid event! This Job only works with onDocumentOpened (OnLoad + OnNew) document event.");
            }

            if (this.m_xModel == null) {
                throw new IllegalArgumentException("The Job needs a XModel reference.");
            }

            String sModuleIdentifier = this.m_xModuleManager.identify(this.m_xModel);
            System.out.printf("css.frame.XJob.execute: Event: \"%s\" - Module : %s\n", new Object[] { sEventName, sModuleIdentifier });

            if (!m_aSupportedModules.contains(sModuleIdentifier)) {
                return new Any(Type.VOID, null);
            }

//            aController = new InterceptionController(this.m_xContext, this.m_xModel, sModuleIdentifier);
        }
        catch (java.lang.Exception e)
        {
            System.out.println(e.getMessage());
        }




        if (eventNameString != null) {

            String sModuleIdentifier = null;
            if (eventNameString.equals("RemoveConnectorShapes")) {

                System.out.println("RemoveConnectorShapes");

            } else if (eventNameString.equals("OnSave")) {


            } else if (eventNameString.equals("OnSave")) {


            } else if (eventNameString.equals("OnSaveAsDone") || eventNameString.equals("OnSaveDone")) {

            } else if (eventNameString.equals("OnNew")) {

                if (m_aSupportedModules.contains(sModuleIdentifier)){
                    return new Any(Type.VOID, null);
                }

                // dialog for diagram name
                XMultiComponentFactory xMCF = m_xContext.getServiceManager();
                Object obj;

                // If valid we must pass the XModel when creating a DialogProvider object

                obj = xMCF.createInstanceWithContext(
                        "com.sun.star.awt.DialogProvider2", m_xContext);

                XDialogProvider2 xDialogProvider = (XDialogProvider2)
                        UnoRuntime.queryInterface(XDialogProvider2.class, obj);

                diagramType = null;
                diagramName = null;


                XDialog xDialog = xDialogProvider.createDialogWithHandler("vnd.sun.star.extension://ru.ssau.graphplus.oograph/dialogs/OnNewDialog.xdl", new XDialogEventHandler() {

                    //diagramNameChanged
                    //diagramTypeChanged
                    @Override
                    public boolean callHandlerMethod(XDialog xDialog, Object o, String s) throws WrappedTargetException {


                        XControlContainer xControlContainer = UnoRuntime.queryInterface(XControlContainer.class, xDialog);

                        boolean handled = true;
                        boolean end = false;
                        String s1 = s;
                        switch (s) {
                            case "createButton":
                                if (diagramName != null && diagramType != null) {
                                    createFrame();
                                    end = true;
                                } else {
                                    end = false;
                                }
                                break;
                            case "diagramTypeChanged":

                                ItemEvent o1 = (ItemEvent) o;
                                XListBox xListBox = UnoRuntime.queryInterface(XListBox.class, o1.Source);
                                String selectedItem = xListBox.getSelectedItem();
                                String diagramType1 = Misc.diagramTypeConvert(selectedItem);
                                diagramType = diagramType1;
//                                diagramModel.setDiagramType(diagramType1);
                                break;

                            case "diagramNameChanged":
                                String trim = QI.XTextComponent(((TextEvent) o).Source).getText().trim();
                                diagramName = trim;
                                diagramModel.setName(trim);
                                break;

                            case "cancelButton":
                                end = true;
                                break;
                        }

                        if (end) {
                            xDialog.endExecute();
                            getDiagramModel().setDiagramType(diagramType);
                        }


                        return handled;
                    }

                    @Override
                    public String[] getSupportedMethodNames() {
                        return new String[]{"diagramNameChanged", "diagramTypeChanged", "createButton", "cancelButton"};
                    }
                });

                xDialog.execute();
            } else if (eventNameString.equals("OnSave")) {

            } else if (eventNameString.equals("OnLoadFinished") || eventNameString.equals("OnLoad")) {


                XComponent xComponent = QI.XComponent(model);


                XComponent drawDoc = QI.XComponent(xComponent);
                XDrawPage currentDrawPage = DrawHelper.getCurrentDrawPage(drawDoc);

                DiagramModel diagramModel_ = getOrCreateDiagramModel(3);
                if (!diagramModel_.isRestored()) {
//                    diagramModel_ = new DiagramModel();
                    diagramModel_.restore(currentDrawPage, xMSF, drawDoc);

                } else {

                }

                DiagramController diagramController_ = new DiagramController(m_xContext, m_xFrame, xMSF, xMCF, diagramModel_, drawDoc);

                OOGraph.this.diagramModel = diagramModel_;
                OOGraph.this.diagramController = diagramController_;

            } else if (eventNameString.equals("onDocumentOpened")) {
                try {
                    NamedValue[] aEnvironment = null;
                    for (int i = 0; i < Arguments.length; i++) {
                        if (Arguments[i].Name.equals("Environment")) {
                            aEnvironment = (NamedValue[]) AnyConverter.toArray(Arguments[i].Value);

                            break;
                        }
                    }

                    if (aEnvironment == null) {
                        throw new IllegalArgumentException("no environment");
                    }

                    String sEnvType = null;
                    String sEventName = null;
                    this.m_xModel = null;
                    for (int i = 0; i < aEnvironment.length; i++) {
                        String sName = aEnvironment[i].Name;
                        if (sName.equals("EnvType")) {
                            sEnvType = AnyConverter.toString(aEnvironment[i].Value);
                        } else if (sName.equals("EventName")) {
                            sEventName = AnyConverter.toString(aEnvironment[i].Value);
                        } else if (sName.equals("Model")) {
                            this.m_xModel = ((XModel2) UnoRuntime.queryInterface(XModel2.class, aEnvironment[i].Value));
                        }

                    }

                    if ((sEnvType == null) || (!sEnvType.equals("DOCUMENTEVENT"))) {
                        throw new IllegalArgumentException("Invalid event type! This Job only works with document events.");
                    }

                    if ((sEventName == null) || (!sEventName.equals("onDocumentOpened"))) {
                        throw new IllegalArgumentException("Invalid event! This Job only works with onDocumentOpened (OnLoad + OnNew) document event.");
                    }

                    if (this.m_xModel == null) {
                        throw new IllegalArgumentException("The Job needs a XModel reference.");
                    }

                    sModuleIdentifier = this.m_xModuleManager.identify(this.m_xModel);
                    System.out.printf("css.frame.XJob.execute: Event: \"%s\" - Module : %s\n", new Object[]{sEventName, sModuleIdentifier});

                    if (!m_aSupportedModules.contains(sModuleIdentifier)) {
                        return new Any(Type.VOID, null);
                    }

                    aController = new StatusBarInterceptionController(this.m_xContext, this.m_xModel, sModuleIdentifier);
                } catch (java.lang.Exception e) {
                }
                return new Any(Type.VOID, null);
            }


        }

        return com.sun.star.uno.Any.VOID;
    }

    public com.sun.star.frame.XDispatch queryDispatch(com.sun.star.util.URL aURL, String TargetFrameName, int SearchFlags) {


        if (aURL.Protocol.compareTo("ru.ssau.graphplus:") == 0) {
            return this;
        }
        return null;
    }

    public com.sun.star.frame.XDispatch[] queryDispatches(com.sun.star.frame.DispatchDescriptor[] seqDescriptors) {
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

    public boolean supportsService(String sService) {
        int len = m_serviceNames.length;

        for (int i = 0; i < len; i++) {
            if (sService.equals(m_serviceNames[i]))
                return true;
        }
        return false;
    }

    public String[] getSupportedServiceNames() {
        return m_serviceNames;
    }

    // com.sun.star.util.XCloseBroadcaster:
    public void addCloseListener(com.sun.star.util.XCloseListener Listener) {
        // TODO: Insert your implementation for "addCloseListener" here.
    }

    public void removeCloseListener(com.sun.star.util.XCloseListener Listener) {
        // TODO: Insert your implementation for "removeCloseListener" here.
    }

    // com.sun.star.util.XCloseable:
    public void close(boolean DeliverOwnership) throws com.sun.star.util.CloseVetoException {
        // TODO: Insert your implementation for "close" here.
    }

    // com.sun.star.task.XAsyncJob:
    public void executeAsync(com.sun.star.beans.NamedValue[] Arguments, com.sun.star.task.XJobListener Listener) throws com.sun.star.lang.IllegalArgumentException {

        System.out.println("executeAsync");
        save();
    }

    @Override
    public void dispatch(URL url, PropertyValue[] propertyValues) {
        if (url.Protocol.compareTo("ru.ssau.graphplus:") == 0) {

            if (url.Complete.equals("ru.ssau.graphplus:Omg")) {
                insertTable(xDrawDoc);
                System.out.println(propertyValues.length);
            }


            if (url.Complete.equals("ru.ssau.graphplus:DropdownCmd")) {

                System.out.println(url.Complete);
                Logger.getGlobal().log(Level.INFO, "PropertyValue[]", propertyValues);
                Object value = propertyValues[1].Value;
                String string = (String) propertyValues[1].Value;

                // TODO
//                    setDiagramType(string);
                return;

            }


            Object nodeObject;
            Object linkObject;

            Link link = null;
            Node node = null;


            if (url.Path.compareTo("Node") == 0) {

                try {
                    XDrawPage xDrawPage = DrawHelper.getDrawPageByIndex(xDrawDoc, 0);
                    XShape xShape = ShapeHelper.createShape(m_xComponent, new Point(800, 600), new Size(1500, 1500), "com.sun.star.drawing.EllipseShape");// .createEllipseShape(m_xComponent, 800, 800, 1500, 1500);
                    node = nodeFactory.create(Node.NodeType.Client, m_xComponent);
                    XPropertySet xPS = QI.XPropertySet(xShape);

                    try {
                        Object ncObj = xMSF.createInstance("com.sun.star.drawing.BitmapTable");
                        XNameContainer xNamedCont = (XNameContainer) QI.XNameContainer(ncObj);


                    } catch (ServiceNotRegisteredException ex) {
                    }

                    XNameContainer xNC = QI.XNameContainer(xPS.getPropertyValue("ShapeUserDefinedAttributes"));

                    XPropertySet xPropSet = QI.XPropertySet(xShape);

                    XComponent xCompShape = (XComponent) UnoRuntime.queryInterface(XComponent.class, xShape);
                    xCompShape.addEventListener(new com.sun.star.document.XEventListener() {

                        public void notifyEvent(com.sun.star.document.EventObject arg0) {
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


            if (url.Path.compareTo(PROCESS_NODE) == 0) {
                try {

                    XDrawPage xPage = PageHelper.getDrawPageByIndex(xDrawDoc, 0);
                    XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, xPage);


                    NodeBase processNode = nodeFactory.create(Node.NodeType.Process, m_xComponent);
                    node = processNode;


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

            if (url.Path.compareTo(PROCEDURE_NODE) == 0) {
                try {


                    XDrawPage xPage = PageHelper.getDrawPageByIndex(xDrawDoc, 0);
                    XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, xPage);
                    NodeBase procedureNode = nodeFactory.create(Node.NodeType.Procedure, m_xComponent);//createAndInsert(NodeBase.NodeType.Process, m_xComponent, xShapes);
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


            if (url.Path.compareTo(CLIENT_NODE) == 0) {
                try {


                    Node clientNode = nodeFactory.create(Node.NodeType.Client, m_xComponent);
                    node = clientNode;

                    DrawHelper.insertNodeOnCurrentPage(clientNode, xDrawDoc);

                    Misc.addUserDefinedAttributes(clientNode.getShape(), xMSF);
                    Misc.setNodeType(clientNode.getShape(), Node.NodeType.Client);

                    Misc.tagShapeAsNode(clientNode.getShape());

                    clientNode.setProps();
                    DrawHelper.setShapePositionAndSize(clientNode.getShape(), 100, 100, 1500, 1500);
                    Gui.createDialogForShape2(clientNode.getShape(), m_xContext, new HashMap<String, XShape>());

                    System.out.println("omg");
                    //return;
                } catch (java.lang.Exception ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }

            }

            if (url.Path.compareTo(SERVER_NODE) == 0) {
                try {
                    NodeBase serverNode = nodeFactory.create(Node.NodeType.Server, m_xComponent);
                    node = serverNode;

                    DrawHelper.insertShapeOnCurrentPage(serverNode.getShape(), xDrawDoc);

                    Misc.addUserDefinedAttributes(serverNode.getShape(), xMSF);
                    Misc.tagShapeAsNode(serverNode.getShape());
                    Misc.setNodeType(serverNode.getShape(), Node.NodeType.Server);


                    node.setProps();
                    DrawHelper.setShapePositionAndSize(serverNode.getShape(), 100, 100, 1500, 1500);
                    Gui.createDialogForShape2(serverNode.getShape(), m_xContext, new HashMap<String, XShape>());

                } catch (PropertyVetoException ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);

                }
            }

            Iterable<XShape> linkShapes = new ArrayList<XShape>();

            if (url.Path.compareTo(LINK_LINK) == 0) {

                Link linkLink = linkFactory.create(Link.LinkType.Link, xDrawDoc);
                link = linkLink;
                linkShapes = linkLink.getShapes();

            }

            if (url.Path.compareTo(MESSAGE_LINK) == 0) {

                Link messageLink = linkFactory.create(Link.LinkType.Message, xDrawDoc);
                link = messageLink;
                linkShapes = messageLink.getShapes();

            }

            if (url.Path.compareTo(CONTROL_LINK) == 0) {

                Link controlLink = linkFactory.create(Link.LinkType.Control, xDrawDoc);
                link = controlLink;
                linkShapes = controlLink.getShapes();

            }


            if (url.Path.contains("Link")) {

                for (XShape shape : linkShapes) {
                    DrawHelper.insertShapeOnCurrentPage(shape, xDrawDoc);

                    linkFactory.setId(shape, link);
                }

                link.setProps();

                // common for all links


                getDiagramController().setLinker(link);
                getDiagramController().setInputMode(new InputTwoShapesMode(getDiagramController(), link));

                //inputMode = new InputTwoShapesMode(getDiagramController());
                statusChangedDisable(url);

//                    lastURL = url;

                if (link != null) {
                    getOrCreateDiagramModel(445).addDiagramElement(link);
                    diagramController.configureListeners(link);
                }
                return;
            }


            if (url.Path.compareTo("Save") == 0) {
                // TODO implement or delete
                return;
            }

            if (url.Path.compareTo("Tag") == 0) {
                // add your own code here
                System.out.println("Tag");
                if (Status.isTagAllNewShapes()) {
                    Status.setTagAllNewShapes(false);
                } else {
                    Status.setTagAllNewShapes(true);
                }
                return;
            }

            if (url.Path.compareTo("Assoc") == 0) {
                // add your own code here
                System.out.println("Assoc");

                return;
            }


            if (url.Path.compareTo("TagAsLink") == 0) {

                try {

                    XDrawPage xPage = PageHelper.getDrawPageByIndex(xDrawDoc, 0);

                    XController xController = m_xFrame.getController();

                    //                            Object ddv = xMCF.createInstanceWithContext("com.sun.star.drawing.DrawingDocumentDrawView", m_xContext);
                    //XSelectionSupplier
                    XSelectionSupplier xSelectSup = QI.XSelectionSupplier(xController);
                    Object selectionObj = xSelectSup.getSelection();
                    XShapes xShapes = (XShapes) UnoRuntime.queryInterface(
                            XShapes.class, selectionObj);
                    try {
                        final XShape xShape = (XShape) QI.XShape(xShapes.getByIndex(0));
                        System.out.println(xShape.getShapeType());
//                        if (xShape.getShapeType().contains("Connector")) {
//                            Misc.tagShapeAsLink(xShape);
//                            chooseLinkType(xShape);
//
//                        }

                        String packageLocation = getPackageLocation();
                        System.out.println(packageLocation);
                        try {
                            XMultiComponentFactory xMCF = m_xContext.getServiceManager();
                            Object obj;

                            // If valid we must pass the XModel when creating a DialogProvider object

                            obj = xMCF.createInstanceWithContext(
                                    "com.sun.star.awt.DialogProvider2", m_xContext);

                            XDialogProvider2 xDialogProvider = (XDialogProvider2)
                                    UnoRuntime.queryInterface(XDialogProvider2.class, obj);


                            XDialog xDialog = xDialogProvider.createDialogWithHandler("vnd.sun.star.extension://ru.ssau.graphplus.oograph/dialogs/Dialog2.xdl", new XDialogEventHandler() {

                                private Integer selected;
                                private Boolean convertShape = true;

                                @Override
                                public boolean callHandlerMethod(XDialog xDialog, Object o, String s) throws WrappedTargetException {
                                    System.out.println(o);
                                    System.out.println(s);


                                    XControlContainer xControlContainer = UnoRuntime.queryInterface(XControlContainer.class, xDialog);
//                                    xControlContainer.getControl("")
                                    boolean handled = true;
                                    boolean end = false;


                                    if (s.equals("chooseType")) {

                                        Misc.tagShapeAsNode(xShape);

                                        XControl comboBox1 = xControlContainer.getControl("ComboBox1");
                                        XComboBox xComboBox = UnoRuntime.queryInterface(XComboBox.class, comboBox1);

                                        String nodeType = xComboBox.getItem(selected.shortValue());
                                        Link linkReplace = null;
                                        final boolean finalConvertShape = convertShape;
                                        final Link finalLinkReplace = linkReplace;
                                        if (convertShape) {
//                                                TODO xDP
//                                                linkReplace = linkFactory.create(LinkBase.LinkType.valueOf(nodeType), m_xComponent, xDP);

//                                            NodeBase.PostCreationAction postCreationAction = new NodeBase.PostCreationAction() {
//                                                @Override
//                                                public void postCreate(XShape shape) {
//                                                    if (finalConvertShape) {
//                                                        if (finalLinkReplace != null) {
//                                                            Misc.tagShapeAsLink(finalLinkReplace.getConnShape1());
//                                                            Misc.tagShapeAsLink(finalLinkReplace.getConnShape2());
//                                                            Misc.tagShapeAsLink(finalLinkReplace.getTextShape());
//
//                                                            xDP.remove(xShape);
//                                                        }
//                                                    } else {
//                                                        Misc.tagShapeAsNode(xShape);
//                                                    }
//                                                }
//                                            };

//                                            ShapeHelper.insertShape(linkReplace.getShape(), xDP , postCreationAction);
//                                            try {
//                                                linkReplace.getShape().setPosition(xShape.getPosition());
//                                                linkReplace.getShape().setSize(xShape.getSize());
//                                            } catch (PropertyVetoException e) {
//                                                e.printStackTrace();
//                                            }
                                        }


                                        end = true;
                                        handled = true;

                                    } else if (s.equals("itemStatusChanged")) {
                                        selected = ((ItemEvent) o).Selected;
                                        System.out.println(o);

                                        handled = true;
                                        end = false;
                                    } else if (s.equals("convertShapeCheckboxExecute")) {
//                                        convertShape = !convertShape;
                                        handled = true;
                                        end = false;
                                    } else if (s.equals("convertShapeCheckboxItemStatusChanged")) {
                                        convertShape = !convertShape;
                                        handled = true;
                                        end = false;
                                    } else {
                                        handled = false;
                                    }

                                    if (end) {
                                        xDialog.endExecute();
                                    }

                                    return handled;
                                }

                                @Override
                                public String[] getSupportedMethodNames() {
                                    return new String[]{"chooseTypeNode", "chooseTypeLink", "chooseType",
                                            "itemStatusChanged", "convertShapeCheckboxExecute",
                                            "convertShapeCheckboxItemStatusChanged"};
                                }
                            });
//                    xDialog.execute();
                            if (xDialog != null)
                                xDialog.execute();


                        } catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
                            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (WrappedTargetException ex) {
                            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (Exception ex) {
                            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    } catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
                        Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (WrappedTargetException ex) {
                        Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (WrappedTargetException e) {
                    e.printStackTrace();
                }
            }


            if (url.Path.compareTo("TagAsNode") == 0) {
                try {

                    XDrawPage xPage = PageHelper.getDrawPageByIndex(xDrawDoc, 0);
                    XController xController = m_xFrame.getController();

                    XSelectionSupplier xSelectSup = QI.XSelectionSupplier(xController);
                    Object selectionObj = xSelectSup.getSelection();
                    XShapes xShapes = (XShapes) UnoRuntime.queryInterface(
                            XShapes.class, selectionObj);
                    try {
                        final XShape xShape = (XShape) QI.XShape(xShapes.getByIndex(0));
                        System.out.println(xShape.getShapeType());


                        Object valueByName = m_xContext.getValueByName("/singletons/com.sun.star.deployment.PackageInformationProvider");
                        XPackageInformationProvider xPackageInformationProvider = UnoRuntime.queryInterface(XPackageInformationProvider.class, valueByName);
                        String packageLocation = xPackageInformationProvider.getPackageLocation("ru.ssau.graphplus.oograph");
                        System.out.println(packageLocation);
                        try {
                            XMultiComponentFactory xMCF = m_xContext.getServiceManager();
                            Object obj;

                            // If valid we must pass the XModel when creating a DialogProvider object

                            obj = xMCF.createInstanceWithContext(
                                    "com.sun.star.awt.DialogProvider2", m_xContext);

                            XDialogProvider2 xDialogProvider = (XDialogProvider2)
                                    UnoRuntime.queryInterface(XDialogProvider2.class, obj);


                            XDialog xDialog = xDialogProvider.createDialogWithHandler("vnd.sun.star.extension://ru.ssau.graphplus.oograph/dialogs/Dialog1.xdl", new XDialogEventHandler() {

                                private Integer selected;
                                private Boolean convertShape = true;

                                @Override
                                public boolean callHandlerMethod(XDialog xDialog, Object o, String s) throws WrappedTargetException {


                                    XControlContainer xControlContainer = UnoRuntime.queryInterface(XControlContainer.class, xDialog);

                                    boolean handled = true;
                                    boolean end = false;


                                    if (s.equals("chooseType")) {

                                        Misc.tagShapeAsNode(xShape);

                                        XControl comboBox1 = xControlContainer.getControl("ComboBox1");
                                        XComboBox xComboBox = UnoRuntime.queryInterface(XComboBox.class, comboBox1);

                                        String nodeType = xComboBox.getItem(selected.shortValue());
                                        NodeBase nodeReplace = null;
                                        final boolean finalConvertShape = convertShape;
                                        final NodeBase finalNodeReplace = nodeReplace;
                                        if (convertShape) {
                                            final NodeBase.NodeType type = Node.NodeType.valueOf(nodeType);
                                            nodeReplace = nodeFactory.create(type, m_xComponent);

                                            NodeBase.PostCreationAction postCreationAction = new NodeBase.DefaultPostCreationAction(convertShape) {
                                                @Override
                                                public void postCreate(XShape shape) {
                                                    super.postCreate(shape);    //To change body of overridden methods use File | Settings | File Templates.

                                                }
                                            };

                                            if (finalConvertShape) {
//                                                    TODO xDP
//                                                    ShapeHelper.insertShape(nodeReplace.getShape(), xDP, postCreationAction);
                                                try {
                                                    nodeReplace.getShape().setPosition(xShape.getPosition());
                                                    nodeReplace.getShape().setSize(xShape.getSize());
                                                    DiagramElement diagramElement = diagramModel.getShapeToDiagramElementMap().get(xShape);

                                                    // TODO get DRAWPAGE of current
//                                                        xDP.remove(xShape);
                                                } catch (PropertyVetoException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {

                                            }


                                        }


                                        end = true;
                                        handled = true;

                                    } else if (s.equals("itemStatusChanged")) {
                                        selected = ((ItemEvent) o).Selected;
                                        System.out.println(o);
                                        XControl control = xControlContainer.getControl("ImageControl1");
                                        XImageControl xImageControl = UnoRuntime.queryInterface(XImageControl.class, control);

                                        XControl comboBox1 = xControlContainer.getControl("ComboBox1");
                                        XComboBox xComboBox = UnoRuntime.queryInterface(XComboBox.class, comboBox1);

                                        String nodeType = xComboBox.getItem(selected.shortValue());
                                        nodeType = nodeType.toLowerCase().trim().replace("node", "");

                                        //QI.XPropertySet(control.getModel()).setPropertyValue("ImageURL", "vnd.sun.star.extension://ru.ssau.graphplus.oograph/images/server.png")
                                        try {
                                            QI.XPropertySet(control.getModel()).setPropertyValue("ImageURL", "vnd.sun.star.extension://ru.ssau.graphplus.oograph/images/" + nodeType + ".png");
                                        } catch (UnknownPropertyException e) {
                                            e.printStackTrace();
                                        } catch (PropertyVetoException e) {
                                            e.printStackTrace();
                                        } catch (IllegalArgumentException e) {
                                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                        }
                                        //xImageControl.setImageURL("vnd.sun.star.extension://ru.ssau.graphplus.oograph/images/"+nodeType +".png");

                                        handled = true;
                                        end = false;
                                    } else if (s.equals("convertShapeCheckboxExecute")) {
//                                        convertShape = !convertShape;
                                        handled = true;
                                        end = false;
                                    } else if (s.equals("convertShapeCheckboxItemStatusChanged")) {
                                        convertShape = !convertShape;
                                        handled = true;
                                        end = false;
                                    } else {
                                        handled = false;
                                    }

                                    if (end) {
                                        xDialog.endExecute();
                                    }

                                    return handled;
                                }

                                @Override
                                public String[] getSupportedMethodNames() {
                                    return new String[]{"chooseTypeNode", "chooseTypeLink", "chooseType",
                                            "itemStatusChanged", "convertShapeCheckboxExecute",
                                            "convertShapeCheckboxItemStatusChanged"};
                                }
                            });

                            xDialog.execute();
//                            if (xDialog != null)
//                                xDialog.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } catch (java.lang.RuntimeException e) {
                            Logger.getGlobal().warning(e.getMessage());
                        }


                    } catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
                        Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (WrappedTargetException ex) {
                        Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (Exception ex) {
                        Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                } catch (WrappedTargetException ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                }

                return;

            }


            if (url.Path.contains("NodeBase")) {
                // common for all nodes
                if (node != null) {
                    getOrCreateDiagramModel(34123).addDiagramElement(node);
                    Misc.setId(node.getShape(), node.getName());
//                        diagramModel.addDiagramElement(node);
                }

                return;
            }


        }


    }

    public void addStatusListener(XStatusListener xl, URL url) {
    }

    public void removeStatusListener(XStatusListener xl, URL url) {
    }

    private void createFrame() {
        try {
            xDrawDoc = (XComponent) UnoRuntime.queryInterface(XComponent.class, m_xComponent);


            insertTable(xDrawDoc);
            XPropertySet xLayerPropSet;
            XLayerManager xLayerManager = UnoRuntime.queryInterface(XLayerManager.class, UnoRuntime.queryInterface(XLayerSupplier.class, xDrawDoc).getLayerManager());
            XLayer xNotVisibleAndEditable = xLayerManager.insertNewByIndex(0);
            xLayerPropSet = (XPropertySet) UnoRuntime.queryInterface(
                    XPropertySet.class, xNotVisibleAndEditable);

            xLayerPropSet.setPropertyValue("Name", "FrameDefenderLayer");

            xLayerPropSet.setPropertyValue("IsVisible", new Boolean(false));
            xLayerPropSet.setPropertyValue("IsLocked", new Boolean(true));
            // create a second layer
            XLayer xNotEditable = xLayerManager.insertNewByIndex(0);
            xLayerPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xNotEditable);
            xLayerPropSet.setPropertyValue("Name", "NotEditable");
            xLayerPropSet.setPropertyValue("IsVisible", new Boolean(true));
            xLayerPropSet.setPropertyValue("IsLocked", new Boolean(true));
            // attach the layer to the rectangles

//            xLayerManager.attachShapeToLayer(xRect2, xNotEditable);

            XDrawPage xDrawPage = DrawHelper.getCurrentDrawPage(xDrawDoc);
            XPropertySet xPropertySet = QI.XPropertySet(xDrawPage);

            Object width = xPropertySet.getPropertyValue("Width");
            Object heigth = xPropertySet.getPropertyValue("Height");
            Integer w = (Integer) width;
            Integer h = (Integer) heigth;

            ArrayList<XShape> xShapes = new ArrayList<XShape>();
            int FRAME_MARGIN = 1000;

            XShape lineShape = DrawHelper.createLineShape(xDrawDoc, FRAME_MARGIN, FRAME_MARGIN, 0, h - 2 * FRAME_MARGIN);
            OOoUtils.setIntProperty(lineShape, "LineWidth", 15);

            xShapes.add(lineShape);

            XShape lineShape1 = DrawHelper.createLineShape(xDrawDoc, FRAME_MARGIN, h - FRAME_MARGIN, w - 2000, 0);
            OOoUtils.setIntProperty(lineShape1, "LineWidth", 15);
            xShapes.add(lineShape1);

            XShape lineShape2 = DrawHelper.createLineShape(xDrawDoc, w - FRAME_MARGIN, FRAME_MARGIN, 0, h - 2 * FRAME_MARGIN);
            OOoUtils.setIntProperty(lineShape2, "LineWidth", 15);
            xShapes.add(lineShape2);
            XShape lineShape3 = DrawHelper.createLineShape(xDrawDoc, FRAME_MARGIN, FRAME_MARGIN, w - 2000, 0);
            OOoUtils.setIntProperty(lineShape3, "LineWidth", 15);

            xShapes.add(lineShape3);

            XShape lineShape4 = DrawHelper.createLineShape(xDrawDoc, FRAME_MARGIN, 2 * FRAME_MARGIN, w - 2000, 0);


            OOoUtils.setIntProperty(lineShape4, "LineWidth", 15);
            xShapes.add(lineShape4);


            XShape textShape = DrawHelper.createTextShape(xDrawDoc, FRAME_MARGIN, FRAME_MARGIN, w - 2 * FRAME_MARGIN, FRAME_MARGIN);

            xShapes.add(textShape);

            DrawHelper.insertShapesOnCurrentPageAndLayer(xShapes, xDrawDoc, xLayerManager, xNotEditable);
            ShapeHelper.addPortion(textShape, diagramName == null ? "Diagram Name" : diagramName, false);


            int height = OOoUtils.getIntProperty(xDrawDoc, "Height");


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    Map<String, String> getKeyValueMapFromXTable(XTable xTable) {

        Map<String, String> result = new HashMap<>();
        XTableRows rows = xTable.getRows();
        for (int i = 0; i < rows.getCount(); i++) {
            try {
                Object byIndex = rows.getByIndex(i);
                XCellRange xCellRange = UnoRuntime.queryInterface(XCellRange.class, byIndex);
                XCell cellByPosition = xCellRange.getCellByPosition(0, 0);
                String formula = cellByPosition.getFormula();
                double value = cellByPosition.getValue();

            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (WrappedTargetException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return result;
    }

    void addCellEventListener(XCell xCell) {
//        xCell.
    }

    private void insertTable(XComponent xDrawDoc) {
        try {
            Object instance = xMSF.createInstance("com.sun.star.drawing.TableShape");
            XShape oTableShape = QI.XShape(instance);
            XDrawPage oDrawPage = DrawHelper.getCurrentDrawPage(xDrawDoc);


            int borderBottom = OOoUtils.getIntProperty(oDrawPage, "BorderBottom");
            int borderLeft = OOoUtils.getIntProperty(oDrawPage, "BorderLeft");
            int borderRight = OOoUtils.getIntProperty(oDrawPage, "BorderRight");
            int width = OOoUtils.getIntProperty(oDrawPage, "Width");
            int height = OOoUtils.getIntProperty(oDrawPage, "Height");


            Size aSize = new Size(width - borderLeft - borderRight, 3000);
            oTableShape.setSize(aSize);
            OOoUtils.setBooleanProperty(oTableShape, "SizeProtect", true);
            Point aPos = new Point();

            aPos.X = width / 2 - aSize.Width / 2;
            aPos.Y = height - borderBottom - 3000;


            oTableShape.setPosition(aPos);


            oDrawPage.add(oTableShape);
            Object model = OOoUtils.getProperty(oTableShape, "Model");
//            XModel xModel = QI.XModel(model);
            XTable xTable = UnoRuntime.queryInterface(XTable.class, model);

            XModifiable xModifiable = UnoRuntime.queryInterface(XModifiable.class, model);

            if (xModifiable != null) {
                xModifiable.addModifyListener(new XModifyListener() {
                    @Override
                    public void modified(EventObject eventObject) {
                        Map<String, String> keyValueMapFromXTable = getKeyValueMapFromXTable(UnoRuntime.queryInterface(XTable.class, eventObject.Source));
                        System.out.println(keyValueMapFromXTable.entrySet().size());

                    }

                    @Override
                    public void disposing(EventObject eventObject) {
                        //TODO implement
                    }
                });
            }

            XEventBroadcaster xEventBroadcaster = UnoRuntime.queryInterface(XEventBroadcaster.class, model);
            if (xEventBroadcaster != null) {

                xEventBroadcaster.addEventListener(new XEventListener() {
                    @Override
                    public void notifyEvent(com.sun.star.document.EventObject eventObject) {
                        System.out.println(eventObject.EventName);
                    }

                    @Override
                    public void disposing(EventObject eventObject) {
                        //TODO implement
                    }
                });
            }

            XTableColumns columns = xTable.getColumns();


            columns.insertByIndex(0, 1);


            XTableRows rows = xTable.getRows();
            rows.insertByIndex(0, 1);
            rows.insertByIndex(0, 1);


            for (int i = 0; i < columns.getCount() - 1; i++) {
                Object byIndex = columns.getByIndex(i);
                OOoUtils.setProperty(byIndex, "Width", aSize.Width / columns.getCount());
            }

            for (int i = 0; i < rows.getCount() - 1; i++) {
                Object byIndex = rows.getByIndex(i);
                OOoUtils.setProperty(byIndex, "Height", aSize.Height / rows.getCount());
            }

            oTableShape.setSize(aSize);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disposing(EventObject eventObject) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void save() {
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
        } catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void frameAction(FrameActionEvent fae) {
        System.out.println("frameAcrtion");
    }

    @Override
    public void notifyEvent(com.sun.star.document.EventObject docEvent) {
        if (docEvent.EventName.equals("OnViewClosed")) {
            removeEventListener();
            //this.m_Controller.getGui().closeControlDialog();
            for (FrameObject frameObj : _frameObjectList)
                if (this.m_xFrame.equals(frameObj.getXFrame()))
                    _frameObjectList.remove(frameObj);
        }
    }

    void statusChangedDisable(URL url) {

        MyURL myURL = new MyURL(url);

        FeatureStateEvent featureStateEvent = new FeatureStateEvent();
        featureStateEvent.Source = this;
        featureStateEvent.IsEnabled = false;
        featureStateEvent.FeatureDescriptor = "QWE";
        featureStateEvent.FeatureURL = url;


        statusChanged(url, featureStateEvent);
    }

    void statusChanged(URL url, FeatureStateEvent featureStateEvent) {
        MyURL myURL = new MyURL(url);
        Set<XStatusListener> xStatusListeners = statusListeners.get(myURL);

        if (xStatusListeners == null) {
            statusListeners.put(myURL, new HashSet<XStatusListener>());
            xStatusListeners = statusListeners.get(myURL);
        }
        for (XStatusListener xStatusListener : xStatusListeners) {
            xStatusListener.statusChanged(featureStateEvent);
        }
    }

    private String getPackageLocation() {
        Object valueByName = m_xContext.getValueByName("/singletons/com.sun.star.deployment.PackageInformationProvider");
        XPackageInformationProvider xPackageInformationProvider = UnoRuntime.queryInterface(XPackageInformationProvider.class, valueByName);
        return xPackageInformationProvider.getPackageLocation("ru.ssau.graphplus.oograph");
    }


    public enum State {
        Nothing,
        InputTwoShapes,
        AddingLink
    }

    static
    {
        m_aSupportedModules.add("com.sun.star.drawing.DrawingDocument");
    }


}
