package ru.ssau.graphplus;


import com.google.common.collect.Sets;
import com.sun.star.awt.*;
import com.sun.star.beans.*;
import com.sun.star.comp.loader.FactoryHelper;
import com.sun.star.document.*;
import com.sun.star.document.XEventListener;
import com.sun.star.drawing.*;
import com.sun.star.frame.*;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.*;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.rendering.XCanvas;
import com.sun.star.ui.XContextMenuInterception;
import com.sun.star.ui.XUIElement;
import com.sun.star.ui.XUIElementFactory;
import com.sun.star.uno.*;
import com.sun.star.uno.Exception;
import com.sun.star.util.URL;
import com.sun.star.util.XModifiable;
import ru.ssau.graphplus.document.event.handler.DocumentEventsHandler;
import ru.ssau.graphplus.document.event.handler.impl.DocumentEventsHandlerImpl;
import ru.ssau.graphplus.gui.*;
import ru.ssau.graphplus.gui.sidebar.*;
import ru.ssau.graphplus.xml.XMLGenerator;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.*;

/**───────▄███████████▄▄──────────────
 *──────▄██▀──────────▀▀██▄────────────
 *────▄█▀────────────────▀██───────────
 *──▄█▀────────────────────▀█▄─────────
 *─█▀──██──────────────██───▀██────────
 *█▀──────────────────────────██───────
 *█──███████████████████───────█───────
 *█────────────────────────────█───────
 *█▄───────────────────────────█───────
 *▀█▄─────────────────────────██───────
 *─▀█▄───────────────────────██────────
 *──▀█▄────────────────────▄█▀─────────
 *─────▀█▄──────────────▄█▀────────────
 *───────▀█▄▄▄──────▄▄▄███████▄▄───────
 *────────███████████████───▀██████▄───
 *─────▄███▀▀────────▀███▄──────█─███──
 *───▄███▄─────▄▄▄▄────███────▄▄████▀──
 *─▄███▓▓█─────█▓▓█───████████████▀────
 *─▀▀██▀▀▀▀▀▀▀▀▀▀███████████────█──────
 *────█─▄▄▄▄▄▄▄▄█▀█▓▓─────██────█──────
 *────█─█───────█─█─▓▓────██────█──────
 *────█▄█───────█▄█──▓▓▓▓▓███▄▄▄█──────
 *────────────────────────██──────────
 *────────────────────────██───▄███▄───
 *────────────────────────██─▄██▓▓▓██──
 *───────────────▄██████████─█▓▓▓█▓▓██▄
 *─────────────▄██▀───▀▀███──█▓▓▓██▓▓▓█
 *─▄███████▄──███───▄▄████───██▓▓████▓█
 *▄██▀──▀▀█████████████▀▀─────██▓▓▓▓███
 *██▀─────────██──────────────██▓██▓███
 *██──────────███──────────────█████─██
 *██───────────███──────────────█─██──█
 *██────────────██─────────────────█───
 *██─────────────██────────────────────
 *██─────────────███───────────────────
 *██──────────────███▄▄────────────────
 *███──────────────▀▀███───────────────
 *─███─────────────────────────────────
 *──███──────────────────────────────**/
public class OOGraph extends ComponentBase implements
        com.sun.star.lang.XInitialization,
        com.sun.star.task.XJob,
        com.sun.star.frame.XDispatchProvider,
        com.sun.star.lang.XServiceInfo,
        com.sun.star.util.XCloseable,
        com.sun.star.task.XAsyncJob,
        com.sun.star.document.XEventListener,
        XComponent,
        XUIElementFactory {


    private static final String msURLhead = "private:resource/toolpanel/OOGraphPanelFactory";


    private static final String m_implementationName = OOGraph.class.getName();
    public static final String RU_SSAU_GRAPHPLUS_SIDEBAR_OOGRAPH_PANEL_FACTORY = "ru.ssau.graphplus.sidebar.OOGraphPanelFactory";
    private static final String[] m_serviceNames = {
            "com.sun.star.frame.ProtocolHandler",
            "com.sun.star.task.Job",
            "com.sun.star.task.AsyncJob",
            RU_SSAU_GRAPHPLUS_SIDEBAR_OOGRAPH_PANEL_FACTORY
    };

    private static final String DIAGRAM_TYPE_CHANGED = "diagramTypeChanged";
    private static final String DIAGRAM_NAME_CHANGED = "diagramNameChanged";
    private static final String CANCEL_BUTTON = "cancelButton";
    private static final String CREATE_BUTTON = "createButton";
    private static final ArrayList<String> m_aSupportedModules = new ArrayList(1);
    public static final String ENV_TYPE = "EnvType";
    public static final String MODEL = "Model";
    public static final String EVENT_NAME = "EventName";
    public static final String DIAGRAM_MODEL = "DiagramModel";
    public static StatusBarInterceptionController aController;
    private static List<WeakReference<OOGraph>> weakReferences = new ArrayList<>();
    private static XMultiComponentFactory xMCF = null;
    private static XMultiServiceFactory xMSF = null;

    private static ArrayList<FrameObject> _frameObjectList = null;
    private static XComponent m_xComponent = null;

    private final XComponentContext m_xContext;

    public static final Logger LOGGER;

    static {
        m_aSupportedModules.add("com.sun.star.drawing.DrawingDocument");

        LOGGER = Logger.getLogger("oograph");
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        LOGGER.addHandler(consoleHandler);

        Handler fileHandler = null;
        try {
            fileHandler = new FileHandler("oograph.log");
            LOGGER.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private MyDispatch myDispatch;
    private com.sun.star.frame.XFrame m_xFrame;
    private Map<MyURL, Set<XStatusListener>> statusListeners = new HashMap();
    private String diagramName;
    private String diagramType;

    private XEventBroadcaster m_xEventBroadcaster = null;
    private boolean isAliveDocumentEventListener = false;
//    private XNameAccess accessLeaves;

    private XModel2 m_xModel;
    private XModuleManager m_xModuleManager;


    private Map<URL, XStatusListener> maListeners;

    public OOGraph(XComponentContext context) {

        LOGGER.info("OOGraph ctor");

        maListeners = new HashMap<URL, XStatusListener>();


        weakReferences.add(new WeakReference(this));
        m_xContext = context;

        try {
            this.m_xModuleManager = UnoRuntime.queryInterface(XModuleManager.class, this.m_xContext.getServiceManager().createInstanceWithContext("com.sun.star.frame.ModuleManager", this.m_xContext));
        } catch (Exception e) {
            e.printStackTrace();
        }

//        this.accessLeaves = ConfigurationAccess.createUpdateAccess(context,
//                "/ru.ssau.graphplus.OOGraph/Leaves");

//        setupDocumentEventsHandler();

    }

//    public static OOGraph temp(XComponentContext xComponentContext) {
//        return new OOGraphProxy(xComponentContext);
//    }

    public static XSingleComponentFactory __getComponentFactory(String sImplementationName) {
        XSingleComponentFactory xFactory = null;

        if (sImplementationName.equals(m_implementationName)){
            xFactory = UnoRuntime.queryInterface(XSingleComponentFactory.class, FactoryHelper.createComponentFactory(OOGraph.class, OOGraph.class.getName()));

        }


         else

        if ( sImplementationName.equals( OptionsDialogHandler.class.getName() ) )
            xFactory = Factory.createComponentFactory(OptionsDialogHandler.class, OptionsDialogHandler.getServiceNames());

        return xFactory;

//        if (sImplementationName.equals(msImplementationName))
//            return Factory.createComponentFactory(PanelBase.class, maServiceNames);
//        else
//            return null;
    }

    public static XSingleServiceFactory __getServiceFactory(
            final String sImplementationName,
            final XMultiServiceFactory xFactory,
            final XRegistryKey xKey) {
        XSingleServiceFactory xResult = null;

        if (sImplementationName.equals(OOGraph.class.getName())) {
            xResult = FactoryHelper.getServiceFactory(
                    OOGraph.class,
                    OOGraph.RU_SSAU_GRAPHPLUS_SIDEBAR_OOGRAPH_PANEL_FACTORY,
                    xFactory,
                    xKey);
        }



        return xResult;
    }

    public static boolean __writeRegistryServiceInfo(XRegistryKey xRegistryKey) {

        boolean bResult = true;

        bResult &= Factory.writeRegistryServiceInfo(m_implementationName,
                m_serviceNames,
                xRegistryKey);


        bResult &= Factory.writeRegistryServiceInfo(OptionsDialogHandler.class.getName(), OptionsDialogHandler.getServiceNames(), xRegistryKey);
        return bResult;
    }

    public static void printInfo(Object obj) {

    }


    @Override
    public void dispose() {
        LOGGER.info("dispose");
    }

    @Override
    public void addEventListener(com.sun.star.lang.XEventListener xEventListener) {
        //TODO implement
    }

    @Override
    public void removeEventListener(com.sun.star.lang.XEventListener xEventListener) {
        //TODO implement
    }


    public String getDiagramType() {
        return diagramType;
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

        LOGGER.info("OOGraph initialize");

        XComponent xDrawDoc;

        if (object.length > 0) {

            m_xFrame = UnoRuntime.queryInterface(XFrame.class, object[0]);

            XController xController = m_xFrame.getController();


            xMCF = m_xContext.getServiceManager();
            m_xComponent = UnoRuntime.queryInterface(XComponent.class, m_xFrame.getController().getModel());
            xMSF = UnoRuntime.queryInterface(
                    XMultiServiceFactory.class, m_xComponent);

            // add the m_xFrame and its diagramController to the static arrayList of _frameObjectList
            // avoid the duplicate gui controls
            boolean isNewFrame;
            if (_frameObjectList == null) {
                _frameObjectList = new ArrayList<FrameObject>();
                isNewFrame = true;
            } else {
                isNewFrame = true;
                for (FrameObject frameObj : _frameObjectList) {
                    if (m_xFrame.equals(frameObj.getFrame())) {
                        isNewFrame = false;
                    }
                }
            }
            if (isNewFrame) {
                try {
                    newFrameCreated(xController);

                        this.m_xEventBroadcaster = UnoRuntime.queryInterface(XEventBroadcaster.class, this.m_xFrame.getController().getModel());
                        addEventListener();
                } catch (java.lang.Exception ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                }


            } else {
                OOGraph.LOGGER.info("not new frame");
            }


        }
    }

    private MyDispatch newFrameCreated(XController xController) throws UnknownPropertyException, WrappedTargetException, com.sun.star.lang.IndexOutOfBoundsException {

        XContextMenuInterception xContMenuInterception =
                UnoRuntime.queryInterface(XContextMenuInterception.class, xController);

        m_xFrame.addFrameActionListener(new MyFrameActionListener());

        XComponent xDrawDoc = UnoRuntime.queryInterface(
                XComponent.class, m_xComponent);
        myDispatch = new MyDispatch(xDrawDoc, m_xContext, m_xFrame, this, xMCF, xMSF, m_xComponent);
        frameToDispatch.put(m_xFrame.getName(), myDispatch);
        dispatchByFrame.put(m_xFrame, myDispatch);
        XDocumentPropertiesSupplier xDocumentInfoSupplier = UnoRuntime.queryInterface(XDocumentPropertiesSupplier.class, xDrawDoc);
        XDocumentProperties documentProperties = xDocumentInfoSupplier.getDocumentProperties();
        //                        documentInfo.setUserFieldName();

        DiagramModel diagramModel = myDispatch.getDiagramModel();
        DiagramController diagramController = myDispatch.getDiagramController();//new DiagramController(m_xContext, m_xFrame, xMSF, xMCF, diagramModel, xDrawDoc, myDispatch);
        xContMenuInterception.registerContextMenuInterceptor(new ContextMenuInterceptor(m_xContext, diagramController));

        addDocumentEventListener(m_xComponent);

        // when the frame is closed we have to remove FrameObject item into the list
        _frameObjectList.add(new FrameObject(m_xFrame, diagramController, diagramModel, myDispatch));


        this.m_xEventBroadcaster = UnoRuntime.queryInterface(XEventBroadcaster.class, this.m_xFrame.getController().getModel());
        addEventListener();

        XDispatchProvider xDispatchProvider = QI.XDispatchProvider(m_xFrame);
        XDispatchProviderInterception xDPI = (XDispatchProviderInterception) UnoRuntime.queryInterface(XDispatchProviderInterception.class, m_xFrame);
        //            xDPI.registerDispatchProviderInterceptor(new MyInterceptor(m_xFrame, xDispatchProvider, null ));


        OOGraph.LOGGER.info("getting Drawpage");
        XDrawPagesSupplier xDPS = UnoRuntime.queryInterface(
                XDrawPagesSupplier.class, m_xComponent);


        QI.XPropertySet(QI.XModel(diagramModel.getDrawDoc()).getCurrentController()).addPropertyChangeListener("CurrentPage", new XPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                OOGraph.LOGGER.info("propertyChange");

            }

            @Override
            public void disposing(EventObject eventObject) {
                //TODO implement
            }
        });

        UnoRuntime.queryInterface(XModel.class, m_xComponent).getCurrentController().addEventListener(new XEventListener() {
            @Override
            public void notifyEvent(com.sun.star.document.EventObject eventObject) {
                //TODO implement
                OOGraph.LOGGER.info("notify");
            }

            @Override
            public void disposing(EventObject eventObject) {
                //TODO implement
            }
        });

        UnoRuntime.queryInterface(XModel.class, m_xComponent).getCurrentController().getModel().addEventListener(new XEventListener() {
            @Override
            public void notifyEvent(com.sun.star.document.EventObject eventObject) {
                OOGraph.LOGGER.info("notify");
            }

            @Override
            public void disposing(EventObject eventObject) {
                //TODO implement
            }
        });


        XDrawPages xDPn = xDPS.getDrawPages();
        com.sun.star.container.XIndexAccess xDPi = UnoRuntime.queryInterface(com.sun.star.container.XIndexAccess.class, xDPn);
        final XDrawPage xDP = UnoRuntime.queryInterface(
                XDrawPage.class, xDPi.getByIndex(0));
        XModifiable xMod = UnoRuntime.queryInterface(
                XModifiable.class, xDrawDoc);
        xMod.addModifyListener(diagramController);
        return myDispatch;
    }

    //TODO remove
    private DocumentEventsHandler documentEventsHandler = new DocumentEventsHandlerImpl();


    private void addDocumentEventListener(XComponent m_xComponent) {
        XDocumentEventBroadcaster xDEB = UnoRuntime.queryInterface(XDocumentEventBroadcaster.class, m_xComponent);

        xDEB.addDocumentEventListener(new XDocumentEventListener() {
            @Override
            public void documentEventOccured(DocumentEvent documentEvent) {

                boolean needToHandle = needToHandle(documentEvent);
                if (!needToHandle) return;
                documentEventsHandler.documentEventOccured(documentEvent);
            }

            @Override
            public void disposing(EventObject eventObject) {

            }
        });
    }


    private boolean needToHandle(DocumentEvent documentEvent) {
        return true;
    }


    // com.sun.star.task.XJob:
    public Object execute(com.sun.star.beans.NamedValue[] Arguments) throws com.sun.star.lang.IllegalArgumentException, com.sun.star.uno.Exception {
        // TODO: Exchange the default return implementation for "execute" !!!
        // NOTE: Default initialized polymorphic structs can cause problems
        // because of missing default initialization of primitive types of
        // some C++ compilers or different Any initialization in Java and C++
        // polymorphic structs.

        XModel model;
        String sEnvType;
        String sEventName;;


        String sModuleIdentifier = null;

        NamedValue[] aEnvironment;

        aEnvironment = (NamedValue[]) getByName(Arguments, "Environment");

        if (aEnvironment == null) {
            throw new IllegalArgumentException("no environment");
        }

        Map<String, Object> byNames = getByNames(aEnvironment, Sets.newHashSet(ENV_TYPE, MODEL, EVENT_NAME));

        sEventName = AnyConverter.toString(byNames.get(EVENT_NAME));
        sEnvType = AnyConverter.toString(byNames.get(ENV_TYPE));
        model = QI.XModel(getByName(aEnvironment, MODEL));
        this.m_xModel = ((XModel2) UnoRuntime.queryInterface(XModel2.class, model));
        try {

            if ((sEnvType == null)) {

                throw new IllegalArgumentException("EnvType is null");
            }

            if (this.m_xModel == null) {
                throw new IllegalArgumentException("The Job needs a XModel reference.");
            }

            sModuleIdentifier = this.m_xModuleManager.identify(this.m_xModel);
            LOGGER.info(String.format("css.frame.XJob.execute: Event: \"%s\" - Module : %s\n", new Object[]{sEventName, sModuleIdentifier}));


            if (!m_aSupportedModules.contains(sModuleIdentifier)) {
                return new Any(Type.VOID, null);
            }

        } catch (java.lang.Exception e) {
            OOGraph.LOGGER.info(e.getMessage());
        }


        if (sEventName != null) {

            switch (sEventName){
                case "RemoveConnectorShapes" : {

                    OOGraph.LOGGER.info("RemoveConnectorShapes");
                    break;
                }
                case "OnSave":
                    onSaveSaveAs(Arguments[0], model);
                    break;
                case "OnSaveAs":
                    onSaveSaveAs(Arguments[0], model);
                    break;
                case "OnNew": {

                    if (m_aSupportedModules.contains(sModuleIdentifier) || sModuleIdentifier == null) {
                        return new Any(Type.VOID, null);
                    } else handleOnNew();

                    break;
                }

                case "onDocumentOpened": {
                    try {

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
                        LOGGER.info(String.format("css.frame.XJob.execute: Event: \"%s\" - Module : %s\n", new Object[]{sEventName, sModuleIdentifier}));

                        aController = new StatusBarInterceptionController(this.m_xContext, this.m_xModel, sModuleIdentifier);

                        if (!m_aSupportedModules.contains(sModuleIdentifier)) {
                            return new Any(Type.VOID, null);
                        }


                    } catch (java.lang.Exception e) {
                    }
                    return new Any(Type.VOID, null);
                }

                case "OnLoad": {
                    OOGraph.LOGGER.info("OnLoad");

                    break;
                }

                case "OnLoadFinished":{

                    OOGraph.LOGGER.info("OnLoadFinished");
                    Global.loaded = Boolean.TRUE;
                    documentEventsHandler.documentEventOccured(sEventName);
                    break;
                }

            }
        }

        return com.sun.star.uno.Any.VOID;
    }

    private void onSaveSaveAs(NamedValue argument, XModel model) throws UnknownPropertyException, PropertyVetoException, IllegalArgumentException, WrappedTargetException {
        OOGraph ooGraph = this;
        Object value = argument.Value;

        NamedValue[] value1 = (NamedValue[]) value;
        XModel xModel = QI.XModel(getByName(value1, "Model"));
        XFrame frame = xModel.getCurrentController().getFrame();
        DiagramModel diagramModel__ = FrameObject.getFrameObjects().get(frame).getDispatch().getDiagramModel();
//                DiagramModel diagramModel__ = ooGraph.getDiagramModel();

        String s = null;

        try {
            s = StringSerializer.toString(diagramModel__);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        setDiagramModelString(model, s);
    }

    private void setDiagramModelString(XModel model, String s) throws IllegalArgumentException, UnknownPropertyException, PropertyVetoException, WrappedTargetException {
        XPropertyContainer userDefinedProperties = null;
        try {

            userDefinedProperties = null;

            XComponent xComponent = QI.XComponent(model);
            XDocumentPropertiesSupplier xDocumentPropertiesSupplier = UnoRuntime.queryInterface(XDocumentPropertiesSupplier.class, xComponent);
            XDocumentProperties documentProperties = xDocumentPropertiesSupplier.getDocumentProperties();
            userDefinedProperties = documentProperties.getUserDefinedProperties();
            userDefinedProperties.addProperty(DIAGRAM_MODEL, PropertyAttribute.MAYBEVOID, s);
        } catch (PropertyExistException e) {
            XPropertySet xPropertySet = QI.XPropertySet(userDefinedProperties);
            xPropertySet.setPropertyValue(DIAGRAM_MODEL, s);
        } catch (IllegalTypeException e) {
            e.printStackTrace();
        }
    }

    Object getByName(NamedValue[] namedValues, String name) {
        for (NamedValue namedValue : namedValues) {
            if (namedValue.Name.equals(name)) {
                return namedValue.Value;
            }
        }
        return null;
    }

    Map<String, Object> getByNames(NamedValue[] namedValues, Set<String> names){
        Map<String,Object> result = new HashMap<>();
        for (NamedValue namedValue : namedValues) {
            if (names.contains(namedValue.Name)) {
                result.put(namedValue.Name, namedValue.Value);
            }
        }
        return result;
    }

    private void handleOnNew() throws Exception {
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


                boolean handled = true;
                boolean end = false;
                String s1 = s;
                switch (s) {
                    case CREATE_BUTTON:
                        if (diagramName != null && diagramType != null) {
                            createFrame();
                            end = true;
                        } else {
                            end = false;
                        }
                        break;
                    case DIAGRAM_TYPE_CHANGED:

                        ItemEvent o1 = (ItemEvent) o;
                        XListBox xListBox = UnoRuntime.queryInterface(XListBox.class, o1.Source);
                        String selectedItem = xListBox.getSelectedItem();
                        String diagramType1 = MiscHelper.diagramTypeConvert(selectedItem);
                        diagramType = diagramType1;
                        //                                diagramModel.setDiagramType(diagramType1);
                        break;

                    case DIAGRAM_NAME_CHANGED:
                        String trim = QI.XTextComponent(((TextEvent) o).Source).getText().trim();
                        diagramName = trim;
                        getDiagramModel().setName(trim);
                        break;

                    case CANCEL_BUTTON:
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
    }

    public com.sun.star.frame.XDispatch queryDispatch(com.sun.star.util.URL aURL, String TargetFrameName, int SearchFlags) {
        OOGraph.LOGGER.info("queryDispatch");

        if (aURL.Protocol.compareTo("ru.ssau.graphplus:") == 0) {
            return getDispatchForFrame(aURL, TargetFrameName, SearchFlags);
        }

        if (!aURL.Complete.startsWith(msProtocol))
            return null;
        else if (aURL.Complete.endsWith(msShowCommand))
            return getDispatchForFrame(aURL, TargetFrameName, SearchFlags);
        ;
//        else
        return null;

//        return null;


    }


    final static String msProtocol = "ru.ssau.graphplus";
    final static String msShowCommand = "ShowOptionsDialog";

    static Map<String, XDispatch> frameToDispatch = new HashMap<>();
    static Map<String, XFrame> frames = new HashMap<>();

    static Map<XFrame, MyDispatch> dispatchByFrame = new WeakHashMap<>();

    static MyDispatch getDispatchByFrame(XFrame frame) {
        return dispatchByFrame.get(frame);
    }


    private XDispatch getDispatchForFrame(URL aURL, String targetFrameName, int searchFlags) {
        XDispatch xDispatch = frameToDispatch.get(targetFrameName);
        if (xDispatch == null) return frameToDispatch.values().iterator().next();
        return xDispatch;
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

        OOGraph.LOGGER.info("executeAsync");
        save();
    }

//    @Override
//    public void dispatch(URL url, PropertyValue[] propertyValues) {
//
//
//    }

    public void addStatusListener(XStatusListener xl, URL url) {
    }

    public void removeStatusListener(XStatusListener xl, URL url) {
    }

    private void createFrame() {
        try {

//            OOGraph ooGraph = MyComponentFactory.oographs.get(MyComponentFactory.oographs.size() - 1);

            XComponent drawDoc = getDiagramModel().getDrawDoc();

            Object instanceWithArguments = QI.XMultiServiceFactory(drawDoc).createInstance("com.sun.star.drawing.AppletShape");
            tableInserter.insertTable(drawDoc);
            XPropertySet xLayerPropSet;
            XLayerManager xLayerManager = UnoRuntime.queryInterface(XLayerManager.class, UnoRuntime.queryInterface(XLayerSupplier.class, drawDoc).getLayerManager());
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

            XDrawPage xDrawPage = DrawHelper.getCurrentDrawPage(drawDoc);
            XPropertySet xPropertySet = QI.XPropertySet(xDrawPage);

            Object width = xPropertySet.getPropertyValue("Width");
            Object heigth = xPropertySet.getPropertyValue("Height");
            Integer w = (Integer) width;
            Integer h = (Integer) heigth;

            ArrayList<XShape> xShapes = new ArrayList<XShape>();
            int FRAME_MARGIN = 1000;

            XShape lineShape = DrawHelper.createLineShape(drawDoc, FRAME_MARGIN, FRAME_MARGIN, 0, h - 2 * FRAME_MARGIN);
            OOoUtils.setIntProperty(lineShape, "LineWidth", 15);

            xShapes.add(lineShape);

            XShape lineShape1 = DrawHelper.createLineShape(drawDoc, FRAME_MARGIN, h - FRAME_MARGIN, w - 2000, 0);
            OOoUtils.setIntProperty(lineShape1, "LineWidth", 15);
            xShapes.add(lineShape1);

            XShape lineShape2 = DrawHelper.createLineShape(drawDoc, w - FRAME_MARGIN, FRAME_MARGIN, 0, h - 2 * FRAME_MARGIN);
            OOoUtils.setIntProperty(lineShape2, "LineWidth", 15);
            xShapes.add(lineShape2);
            XShape lineShape3 = DrawHelper.createLineShape(drawDoc, FRAME_MARGIN, FRAME_MARGIN, w - 2000, 0);
            OOoUtils.setIntProperty(lineShape3, "LineWidth", 15);

            xShapes.add(lineShape3);

            XShape lineShape4 = DrawHelper.createLineShape(drawDoc, FRAME_MARGIN, 2 * FRAME_MARGIN, w - 2000, 0);


            OOoUtils.setIntProperty(lineShape4, "LineWidth", 15);
            xShapes.add(lineShape4);


            XShape textShape = DrawHelper.createTextShape(drawDoc, FRAME_MARGIN, FRAME_MARGIN, w - 2 * FRAME_MARGIN, FRAME_MARGIN);

            xShapes.add(textShape);

            DrawHelper.insertShapesOnCurrentPageAndLayer(xShapes, drawDoc, xLayerManager, xNotEditable);
            ShapeHelper.addPortion(textShape, diagramName == null ? "Diagram Name" : diagramName, false);


            int height = OOoUtils.getIntProperty(drawDoc, "Height");


        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    TableInserter tableInserter = new TableInserterImpl(xMSF);

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
            OOGraph.LOGGER.info("Save");

            XComponent xDD = null;
            m_xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, m_xFrame.getController().getModel());
            // add your own code here


            com.sun.star.drawing.XDrawPagesSupplier xDPS =
                    (com.sun.star.drawing.XDrawPagesSupplier) UnoRuntime.queryInterface(
                            com.sun.star.drawing.XDrawPagesSupplier.class, getDiagramModel().getDrawDoc());
            com.sun.star.drawing.XDrawPages xDPn = xDPS.getDrawPages();
            com.sun.star.container.XIndexAccess xDPi =
                    (com.sun.star.container.XIndexAccess) UnoRuntime.queryInterface(
                            com.sun.star.container.XIndexAccess.class, xDPn);
            XDrawPage xDrawPage = (com.sun.star.drawing.XDrawPage) UnoRuntime.queryInterface(
                    com.sun.star.drawing.XDrawPage.class, xDPi.getByIndex(0));

            XMLGenerator.generateXMLforDocument(getDiagramModel().getDrawDoc(), xDrawPage, path);

            Gui.showErrorMessageBox(null, "Saved", path, xMCF, m_xContext);
            return;
        } catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    @Override
    public void notifyEvent(com.sun.star.document.EventObject docEvent) {
        if (docEvent.EventName.equals("OnViewClosed")) {
            removeEventListener();
            //this.m_Controller.getGui().closeControlDialog();
            for (FrameObject frameObj : _frameObjectList)
                if (this.m_xFrame.equals(frameObj.getFrame()))
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

    public DiagramModel getDiagramModel() {
        return myDispatch.getDiagramModel();
    }

    public enum State {
        Nothing,
        InputTwoShapes,
        AddingLink
    }

    static {
        m_aSupportedModules.add("com.sun.star.drawing.DrawingDocument");
    }


    /**
     * The main factory method has two parts:
     * - Extract and check some values from the given arguments
     * - Check the sResourceURL and create a panel for it.
     */
    @Override
    public XUIElement createUIElement(
            final String sResourceURL,
            final PropertyValue[] aArgumentList)
            throws com.sun.star.container.NoSuchElementException, IllegalArgumentException {


        // Reject all resource URLs that don't have the right prefix.
        if (!sResourceURL.startsWith(msURLhead)) {
            throw new com.sun.star.container.NoSuchElementException(sResourceURL, this);
        }

        // Retrieve the parent window and canvas from the given argument list.
        XWindow xParentWindow = null;
        XCanvas xCanvas = null;

        XFrame xFrame = null;
        for (final PropertyValue aValue : aArgumentList) {

            xFrame = null;
            switch (aValue.Name) {
                case "ParentWindow": {
                    try {
                        xParentWindow = (XWindow) AnyConverter.toObject(XWindow.class, aValue.Value);
                    } catch (IllegalArgumentException aException) {

                    }
                }
                break;
                case "Canvas":
                    xCanvas = (XCanvas) AnyConverter.toObject(XCanvas.class, aValue.Value);
                    break;

                case "Frame": {
                    try {
                        xFrame = (XFrame) AnyConverter.toObject(XFrame.class, aValue.Value);
                    } catch (IllegalArgumentException aException) {
                        ru.ssau.graphplus.temp.sidebar.Log.Instance().PrintStackTrace(aException);
                    }
                }
                break;

                // Other values that are available but not used here are:
                case "SfxBindings":     // This is used as a hack and works only when not crossing compartment boundaries (ie only in local C++ extensions).
                case "Theme":           // An XPropertySet with all the sidebar theme values in it.
                case "Sidebar":         // A com.sun.star.ui.XSidebar object that can be used to trigger layouts of the sidebar.
                    // Use this when the height of your panel changes.
//            case "Canvas":          // A XCanvas object.  This is only provided when the 'WantsCanvas' flag in Sidebar.xcu has been set for the panel.
                case "ApplicationName": // The application part of the current sidebar context.
                case "ContextName":     // The context part of the current sidebar context.
            }
        }
        // Check some arguments.
        if (xParentWindow == null) {
            throw new IllegalArgumentException("No parent window provided to the UIElement factory. Cannot create tool panel.", this, (short) 1);
        }

        // Create the panel.
        final String sElementName = sResourceURL.substring(msURLhead.length() + 1);

        if (sElementName.equals("LinkNodesPanel")) {

            MyDispatch myDispatch1 = dispatchByFrame.get(xFrame);

            LinkNodesPanel aPanel = new LinkNodesPanel(
                    xFrame, xParentWindow, m_xContext, myDispatch1.getDiagramController());
            LinkNodesDialog linkNodesDialog = new LinkNodesDialog(myDispatch1);
            aPanel.setLinkNodesDialog(linkNodesDialog);

            linkNodesDialog.init(aPanel);
            return new UIElement(
                    sResourceURL,
                    aPanel);
        }

        if (sElementName.equals("InsertNodePanel")) {

            MyDispatch myDispatch1 = dispatchByFrame.get(xFrame);

            InsertNodePanel insertNodePanel = new InsertNodePanel(
                    xFrame, xParentWindow, m_xContext, myDispatch1, m_xComponent);

            InsertNodeDialog insertNodeDialog = new InsertNodeDialog(myDispatch1);

            insertNodePanel.setDialog(insertNodeDialog);

            insertNodeDialog.init(insertNodePanel);
            return new UIElement(
                    sResourceURL,
                    insertNodePanel);
        }

        return null;
    }



    // settings dialog


}
