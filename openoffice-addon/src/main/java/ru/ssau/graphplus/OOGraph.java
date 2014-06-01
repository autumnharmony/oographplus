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
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.document.event.handler.DocumentEventsHandler;
import ru.ssau.graphplus.document.event.handler.impl.DocumentEventsHandlerImpl;
import ru.ssau.graphplus.gui.*;
import ru.ssau.graphplus.gui.sidebar.*;

import java.io.*;
import java.lang.RuntimeException;
import java.lang.ref.WeakReference;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;

/**
 * ───────▄███████████▄▄──────────────
 * ──────▄██▀──────────▀▀██▄────────────
 * ────▄█▀────────────────▀██───────────
 * ──▄█▀────────────────────▀█▄─────────
 * ─█▀──██──────────────██───▀██────────
 * █▀──────────────────────────██───────
 * █──███████████████████───────█───────
 * █────────────────────────────█───────
 * █▄───────────────────────────█───────
 * ▀█▄─────────────────────────██───────
 * ─▀█▄───────────────────────██────────
 * ──▀█▄────────────────────▄█▀─────────
 * ─────▀█▄──────────────▄█▀────────────
 * ───────▀█▄▄▄──────▄▄▄███████▄▄───────
 * ────────███████████████───▀██████▄───
 * ─────▄███▀▀────────▀███▄──────█─███──
 * ───▄███▄─────▄▄▄▄────███────▄▄████▀──
 * ─▄███▓▓█─────█▓▓█───████████████▀────
 * ─▀▀██▀▀▀▀▀▀▀▀▀▀███████████────█──────
 * ────█─▄▄▄▄▄▄▄▄█▀█▓▓─────██────█──────
 * ────█─█───────█─█─▓▓────██────█──────
 * ────█▄█───────█▄█──▓▓▓▓▓███▄▄▄█──────
 * ────────────────────────██──────────
 * ────────────────────────██───▄███▄───
 * ────────────────────────██─▄██▓▓▓██──
 * ───────────────▄██████████─█▓▓▓█▓▓██▄
 * ─────────────▄██▀───▀▀███──█▓▓▓██▓▓▓█
 * ─▄███████▄──███───▄▄████───██▓▓████▓█
 * ▄██▀──▀▀█████████████▀▀─────██▓▓▓▓███
 * ██▀─────────██──────────────██▓██▓███
 * ██──────────███──────────────█████─██
 * ██───────────███──────────────█─██──█
 * ██────────────██─────────────────█───
 * ██─────────────██────────────────────
 * ██─────────────███───────────────────
 * ██──────────────███▄▄────────────────
 * ███──────────────▀▀███───────────────
 * ─███─────────────────────────────────
 * ──███──────────────────────────────*
 */
public class OOGraph extends ComponentBase implements
        com.sun.star.lang.XInitialization,
        com.sun.star.task.XJob,
        com.sun.star.frame.XDispatchProvider,
        com.sun.star.lang.XServiceInfo,
        com.sun.star.util.XCloseable,
        com.sun.star.task.XAsyncJob,
        com.sun.star.document.XEventListener,
        XComponent,
        XUIElementFactory,
        XDialogEventHandler {


    public static final String RU_SSAU_GRAPHPLUS_SIDEBAR_OOGRAPH_PANEL_FACTORY = "ru.ssau.graphplus.sidebar.OOGraphPanelFactory";
    public static final String ENV_TYPE = "EnvType";
    public static final String MODEL = "Model";
    public static final String EVENT_NAME = "EventName";
    public static final String DIAGRAM_MODEL = "DiagramModel";
    final static String msProtocol = "ru.ssau.graphplus";
    final static String msShowCommand = "ShowOptionsDialog";
    private static final ArrayList<String> m_aSupportedModules = new ArrayList(1);

    static {
        m_aSupportedModules.add("com.sun.star.drawing.DrawingDocument");


    }

    private static final String msURLhead = "private:resource/toolpanel/OOGraphPanelFactory";
    private static final String m_implementationName = OOGraph.class.getName();
    private static final String[] m_serviceNames = {
            "com.sun.star.frame.ProtocolHandler",
            "com.sun.star.task.Job",
            "com.sun.star.task.AsyncJob",
            RU_SSAU_GRAPHPLUS_SIDEBAR_OOGRAPH_PANEL_FACTORY
    };
    //    public static StatusBarInterceptionController aController;
    static Map<String, XDispatch> frameToDispatch = new HashMap<>();
    static Map<XFrame, MyDispatch> dispatchByFrame = new WeakHashMap<>();
    private static List<WeakReference<OOGraph>> instances = new ArrayList<>();
    private static XMultiComponentFactory xMCF = null;
    private static XMultiServiceFactory xMSF = null;
    private static ArrayList<FrameObject> _frameObjectList = null;
    private static XComponent m_xComponent = null;
    private final XComponentContext m_xContext;
    private TableInserter tableInserter = new TableInserterImpl(xMSF);
    private MyDispatch myDispatch;
    private com.sun.star.frame.XFrame m_xFrame;
    private Map<MyURL, Set<XStatusListener>> statusListeners = new HashMap();
    private XEventBroadcaster m_xEventBroadcaster = null;
    private boolean isAliveDocumentEventListener = false;
    private XModel2 m_xModel;
    private XModuleManager m_xModuleManager;
    private DocumentEventsHandler documentEventsHandler = new DocumentEventsHandlerImpl();


    public OOGraph(XComponentContext context) {

        instances.add(new WeakReference(this));


        m_xContext = context;

        try {
            this.m_xModuleManager = UnoRuntime.queryInterface(XModuleManager.class, this.m_xContext.getServiceManager().createInstanceWithContext("com.sun.star.frame.ModuleManager", this.m_xContext));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static XSingleComponentFactory __getComponentFactory(String sImplementationName) {
        XSingleComponentFactory xFactory = null;

        if (sImplementationName.equals(m_implementationName)) {
            xFactory = UnoRuntime.queryInterface(XSingleComponentFactory.class, FactoryHelper.createComponentFactory(OOGraph.class, OOGraph.class.getName()));

        } else if (sImplementationName.equals(OptionsDialogHandler.class.getName()))
            xFactory = Factory.createComponentFactory(OptionsDialogHandler.class, OptionsDialogHandler.getServiceNames());

        return xFactory;
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

    @Override
    public void dispose() {
//        LOGGER.info("dispose");
    }

    @Override
    public void addEventListener(com.sun.star.lang.XEventListener xEventListener) {
        //TODO implement
    }

    @Override
    public void removeEventListener(com.sun.star.lang.XEventListener xEventListener) {
        //TODO implement
    }

    private void addEventListener() {
        if (!this.isAliveDocumentEventListener) {
            this.m_xEventBroadcaster.addEventListener(this);
            this.isAliveDocumentEventListener = true;
        }
    }

    private void removeEventListener() {
        if (this.isAliveDocumentEventListener) {
            this.m_xEventBroadcaster.removeEventListener(this);
            this.isAliveDocumentEventListener = false;
        }
    }

    private static Map<XFrame, FrameObject> frameObjectMap = new WeakHashMap<>();

    // com.sun.star.lang.XInitialization:
    @Override
    public void initialize(Object[] object) throws com.sun.star.uno.Exception {

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
                    FrameObject frameObject = newFrameCreated(xController);
                    frameObjectMap.put(m_xFrame, frameObject);
                    this.m_xEventBroadcaster = UnoRuntime.queryInterface(XEventBroadcaster.class, this.m_xFrame.getController().getModel());
                    addEventListener();
                } catch (java.lang.Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    private FrameObject newFrameCreated(XController xController) throws UnknownPropertyException, WrappedTargetException, com.sun.star.lang.IndexOutOfBoundsException {

//        XContextMenuInterception xContMenuInterception =
//                UnoRuntime.queryInterface(XContextMenuInterception.class, xController);

        m_xFrame.addFrameActionListener(new MyFrameActionListener());

        XComponent xDrawDoc = UnoRuntime.queryInterface(
                XComponent.class, m_xComponent);
        myDispatch = new MyDispatch(xDrawDoc, m_xContext, m_xFrame, xMCF, xMSF);
        frameToDispatch.put(m_xFrame.getName(), myDispatch);
        dispatchByFrame.put(m_xFrame, myDispatch);

        DiagramModel diagramModel = myDispatch.getDiagramModel();
        DiagramController diagramController = myDispatch.getDiagramController();//new DiagramController(m_xContext, m_xFrame, xMSF, xMCF, diagramModel, xDrawDoc, myDispatch);
//        xContMenuInterception.registerContextMenuInterceptor(new ContextMenuInterceptor(m_xContext, diagramController));

        addDocumentEventListener(m_xComponent);

        // when the frame is closed we have to remove FrameObject item into the list
        FrameObject e = new FrameObject(m_xFrame, diagramController, diagramModel, myDispatch);
        _frameObjectList.add(e);


        this.m_xEventBroadcaster = UnoRuntime.queryInterface(XEventBroadcaster.class, this.m_xFrame.getController().getModel());
        addEventListener();


        QI.XPropertySet(QI.XModel(diagramModel.getDrawDoc()).getCurrentController()).addPropertyChangeListener("CurrentPage", new XPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            }

            @Override
            public void disposing(EventObject eventObject) {
            }
        });

        UnoRuntime.queryInterface(XModel.class, m_xComponent).getCurrentController().addEventListener(new XEventListener() {
            @Override
            public void notifyEvent(com.sun.star.document.EventObject eventObject) {
            }

            @Override
            public void disposing(EventObject eventObject) {
            }
        });

        UnoRuntime.queryInterface(XModel.class, m_xComponent).getCurrentController().getModel().addEventListener(new XEventListener() {
            @Override
            public void notifyEvent(com.sun.star.document.EventObject eventObject) {
            }

            @Override
            public void disposing(EventObject eventObject) {
            }
        });


        XModifiable xMod = UnoRuntime.queryInterface(
                XModifiable.class, xDrawDoc);
        xMod.addModifyListener(diagramController);
        return e;
    }

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
        String sEventName;


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
        this.m_xModel = UnoRuntime.queryInterface(XModel2.class, model);
        try {

            if ((sEnvType == null)) {

                throw new IllegalArgumentException("EnvType is null");
            }

            if (this.m_xModel == null) {
                throw new IllegalArgumentException("The Job needs a XModel reference.");
            }

            sModuleIdentifier = this.m_xModuleManager.identify(this.m_xModel);
//            LOGGER.info(String.format("css.frame.XJob.execute: Event: \"%s\" - Module : %s\n", new Object[]{sEventName, sModuleIdentifier}));


            if (!m_aSupportedModules.contains(sModuleIdentifier)) {
                return new Any(Type.VOID, null);
            }

        } catch (java.lang.Exception e) {
//            OOGraph.LOGGER.info(e.getMessage());
        }


        if (sEventName != null) {

            switch (sEventName) {
//                case "RemoveConnectorShapes" : {
//
//                    OOGraph.LOGGER.info("RemoveConnectorShapes");
//                    break;
//                }
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
//                        LOGGER.info(String.format("css.frame.XJob.execute: Event: \"%s\" - Module : %s\n", new Object[]{sEventName, sModuleIdentifier}));

//                        aController = new StatusBarInterceptionController(this.m_xContext, this.m_xModel, sModuleIdentifier);

                        if (!m_aSupportedModules.contains(sModuleIdentifier)) {
                            return new Any(Type.VOID, null);
                        }


                    } catch (java.lang.Exception e) {
                    }
                    return new Any(Type.VOID, null);
                }

                case "OnLoad": {
//                    OOGraph.LOGGER.info("OnLoad");

                    break;
                }

                case "OnLoadFinished": {

//                    OOGraph.LOGGER.info("OnLoadFinished");
                    try {
                        Global.loaded = Boolean.TRUE;
                    } catch (java.lang.Exception e) {
//                        OOGraph.LOGGER.log(Level.WARNING, e.getMessage());
                    }

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

        String s = null;

        try {
            s = StringSerializer.toString(diagramModel__);
        } catch (IOException e) {
            e.printStackTrace();
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

    Map<String, Object> getByNames(NamedValue[] namedValues, Set<String> names) {
        Map<String, Object> result = new HashMap<>();
        for (NamedValue namedValue : namedValues) {
            if (names.contains(namedValue.Name)) {
                result.put(namedValue.Name, namedValue.Value);
            }
        }
        return result;
    }

    private void handleOnNew() throws Exception {

    }

    public com.sun.star.frame.XDispatch queryDispatch(com.sun.star.util.URL aURL, String TargetFrameName, int SearchFlags) {
//        OOGraph.LOGGER.info("queryDispatch");

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

//    @Override
//    public void dispatch(URL url, PropertyValue[] propertyValues) {
//
//
//    }

    // com.sun.star.task.XAsyncJob:
    public void executeAsync(com.sun.star.beans.NamedValue[] Arguments, com.sun.star.task.XJobListener Listener) throws com.sun.star.lang.IllegalArgumentException {

//        OOGraph.LOGGER.info("executeAsync");
        save();
    }

    @Override
    public void disposing(EventObject eventObject) {
        // empty body TODO
    }

    public void save() {
        SystemDialog sd = new SystemDialog(m_xContext, xMCF);
        String path = sd.raiseSaveAsDialog();
        save(path);
    }

    public void save(String path) {
        Gui.showErrorMessageBox(null, "Saved", path, xMCF, m_xContext);
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
            if (myDispatch1 == null) {
                myDispatch1 = dispatchByFrame.values().iterator().next();
            }
            if (xFrame == null){
                xFrame = dispatchByFrame.keySet().iterator().next();
            }
            LinkNodesPanel aPanel = new LinkNodesPanel(
                    xFrame, xParentWindow, m_xContext, myDispatch1);
            XModel xModel = QI.XModel(myDispatch1.getDiagramModel().getDrawDoc());


            return new UIElement(
                    sResourceURL,
                    aPanel);
        }

        if (sElementName.equals("InsertNodePanel")) {

            MyDispatch myDispatch1 = dispatchByFrame.get(xFrame);
            if (myDispatch1 == null) {
                myDispatch1 = dispatchByFrame.values().iterator().next();
            }
            if (xFrame == null){
                xFrame = dispatchByFrame.keySet().iterator().next();
            }
            InsertNodePanel insertNodePanel = new InsertNodePanel(
                    xFrame, xParentWindow, m_xContext, myDispatch1, m_xComponent, myDispatch1.getDiagramService());

            InsertNodeDialog insertNodeDialog = new InsertNodeDialog(myDispatch1);

            insertNodePanel.setDialog(insertNodeDialog);

            insertNodeDialog.init(insertNodePanel);
            return new UIElement(
                    sResourceURL,
                    insertNodePanel);
        }

        return null;
    }

    @Override
    public boolean callHandlerMethod(XDialog xDialog, Object o, String s) throws WrappedTargetException {
        return true;
    }

    @Override
    public String[] getSupportedMethodNames() {
        return new String[0];
    }

    static {
        m_aSupportedModules.add("com.sun.star.drawing.DrawingDocument");
    }

}
