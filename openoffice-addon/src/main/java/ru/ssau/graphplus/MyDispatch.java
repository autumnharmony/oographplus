/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.star.awt.*;
import com.sun.star.beans.*;
import com.sun.star.container.XNameContainer;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.document.*;
import com.sun.star.document.XEventListener;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.drawing.XShapes;
import com.sun.star.frame.*;
import com.sun.star.lang.*;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.task.XJobExecutor;
import com.sun.star.uno.*;
import com.sun.star.uno.Exception;
import com.sun.star.uno.RuntimeException;
import com.sun.star.util.URL;
import ru.ssau.graphplus.analizer.DiagramWalker;
import ru.ssau.graphplus.api.DiagramService;
import ru.ssau.graphplus.api.DiagramType;
import ru.ssau.graphplus.codegen.CodeGenerator;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.MiscHelper;
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.di.AddonModule;
import ru.ssau.graphplus.document.event.handler.DocumentEventHandler;
import ru.ssau.graphplus.document.event.handler.DocumentEventsHandler;
import ru.ssau.graphplus.document.event.handler.impl.DocumentEventsHandlerImpl;
import ru.ssau.graphplus.gui.*;
import ru.ssau.graphplus.gui.dialogs.CreateNodeDialog;
import ru.ssau.graphplus.gui.dialogs.GetCodeDialog;
import ru.ssau.graphplus.link.*;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.node.*;
import ru.ssau.graphplus.recognition.DiagramTypeRecognition;
import ru.ssau.graphplus.recognition.DiagramTypeRecognitionImpl;

import static ru.ssau.graphplus.Constants.*;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dispatch wrapper
 */
public class MyDispatch implements XDispatch {


    private final XFrame m_xFrame;
    private final Logger anonymousLogger;
    private final OOGraph ooGraph;
    private final TableInserterImpl tableInserter;
    private final XMultiServiceFactory xMSF;
    private final XComponent m_xComponent;
    private final NodeFactory nodeFactory;
    private final LinkFactory linkFactory;
    private final XMultiComponentFactory xMCF;
    private final XComponentContext m_xContext;
    private final DiagramService diagramService;
    private final XUndoManager undoManager;
    private final DiagramWalker diagramWalker;

    public NodeFactory getNodeFactory() {
        return nodeFactory;
    }

    public LinkFactory getLinkFactory() {
        return linkFactory;
    }

    public XFrame getFrame() {
        return m_xFrame;
    }

    private final XComponent xDrawDoc;
    private DiagramModel diagramModel;
    private final Logger logger;
    private DiagramController diagramController;
    private String packageLocation;

    public XMultiServiceFactory getxMSF() {
        return xMSF;
    }

    public XMultiComponentFactory getxMCF() {
        return xMCF;
    }

    private DocumentEventsHandler documentEventsHandler = new DocumentEventsHandlerImpl();

    public MyDispatch(XComponent xDrawDoc, XComponentContext m_xContext, XFrame m_xFrame, OOGraph ooGraph, XMultiComponentFactory xMCF, XMultiServiceFactory xMSF, XComponent m_xComponent) {
        this.xDrawDoc = xDrawDoc;
        XUndoManagerSupplier xUndoManagerSupplier = QI.XUndoManagerSupplier(xDrawDoc);
        undoManager = xUndoManagerSupplier.getUndoManager();


        this.m_xContext = m_xContext;
        this.m_xFrame = m_xFrame;
        anonymousLogger = Logger.getAnonymousLogger();
        logger = anonymousLogger;
        this.ooGraph = ooGraph;

        this.xMCF = xMCF;
        this.xMSF = xMSF;
        tableInserter = new TableInserterImpl(xMSF);
        this.m_xComponent = m_xComponent;

        nodeFactory = new NodeFactory(xMSF);
        linkFactory = new LinkFactory(xMSF);


        diagramModel = new DiagramModel(xDrawDoc);
        diagramController = new DiagramController(m_xContext, m_xFrame, xMSF, xMCF, diagramModel, xDrawDoc, this, undoManager);

        setupDocumentEventsHandler();


        Injector injector;
        try {
            injector = Guice.createInjector(new AddonModule(diagramModel, xMSF, xDrawDoc, diagramController));
            diagramService = injector.getInstance(DiagramService.class);

            diagramWalker = injector.getInstance(DiagramWalker.class);
        } catch (java.lang.Exception e) {
            e.printStackTrace();

            throw new java.lang.RuntimeException(e);
        }


        layout = injector.getInstance(Layout.class);


        if (Boolean.TRUE.equals(Global.loaded)) {
            onLoadHandler();
            Global.loaded = false;
        }


        m_xNewBroadcaster = UnoRuntime.queryInterface(XDocumentEventBroadcaster.class, xDrawDoc);
        m_xOldBroadcaster = UnoRuntime.queryInterface(XEventBroadcaster.class, xDrawDoc);

        m_xNewBroadcaster.addDocumentEventListener(new XDocumentEventListener() {
            @Override
            public void documentEventOccured(DocumentEvent documentEvent) {
                logger.info("XDocumentEventBroadcaster  documentEventOccured");
                logger.info(documentEvent.EventName);
            }

            @Override
            public void disposing(EventObject eventObject) {
                System.out.println("DISPOSING");
            }
        });


        m_xOldBroadcaster.addEventListener(new XEventListener() {
            @Override
            public void notifyEvent(com.sun.star.document.EventObject eventObject) {
                if (eventObject.EventName.equals("ShapeModified")) {
                    XShape shape = QI.XShape(eventObject.Source);
                    diagramController.onShapeModified(shape);
                }

                if (eventObject.EventName.equals("ShapeInserted")) {
                    Object source = eventObject.Source;
                    XShape xShape = QI.XShape(source);
                }

                if (eventObject.EventName.equals("ShapeRemoved")) {
                    Object source = eventObject.Source;
                    XShape xShape = QI.XShape(source);

                    diagramController.onShapeRemoved(xShape);
                }

            }

            @Override
            public void disposing(EventObject eventObject) {
                System.out.println("DISPOSING");
            }
        });
    }

    private XDocumentEventBroadcaster m_xNewBroadcaster;
    private XEventBroadcaster m_xOldBroadcaster;

    private void onLoadHandler() {

        diagramModel.init(nodeFactory, linkFactory);


    }

    public DiagramService getDiagramService() {
        return diagramService;
    }

    private int getLinkCount(DiagramModel modelFromString) {
        return 0;  //TODO
    }

    private int getNodeCount(DiagramModel modelFromString) {
        return 0;  //TODO
    }

    private void setupDocumentEventsHandler() {

        OOGraph.LOGGER.info("setupDocumentEventsHandler");

        documentEventsHandler.registerHandler(ImmutableList.of("OnLoad", "OnLoadFinished"), new DocumentEventHandler() {
            @Override
            public void documentEventOccured(DocumentEvent documentEvent) {
                onLoadHandler();
            }

            @Override
            public void documentEventOccured(String eventName) {
                onLoadHandler();
            }
        });

        documentEventsHandler.registerHandler(ImmutableList.of("OnSaveDone", "OnSaveAsDone"), new DocumentEventHandler() {
            @Override
            public void documentEventOccured(DocumentEvent documentEvent) {

            }

            @Override
            public void documentEventOccured(String eventName) {

            }
        });

        documentEventsHandler.registerHandler(ImmutableList.of("OnSave", "OnSaveAs"), new DocumentEventHandler() {
            @Override
            public void documentEventOccured(DocumentEvent documentEvent) {
//                OOGraph ooGraph = MyComponentFactory.mapMap.get(diagramModel);

                XPropertyContainer userDefinedProperties = null;

                String s = null;
                try {
                    diagramModel.refreshModel();
                    s = StringSerializer.toString(diagramModel);

                    XDocumentPropertiesSupplier xDocumentPropertiesSupplier = UnoRuntime.queryInterface(XDocumentPropertiesSupplier.class, m_xComponent);
                    XDocumentProperties documentProperties = xDocumentPropertiesSupplier.getDocumentProperties();
                    userDefinedProperties = documentProperties.getUserDefinedProperties();
                    userDefinedProperties.addProperty("DiagramModel", PropertyAttribute.MAYBEVOID, new Any(Type.STRING, s));
                    //                        documentProperties.storeToStorage();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalTypeException e) {
                    e.printStackTrace();
                } catch (PropertyExistException e) {

                    if (userDefinedProperties != null && s != null) {
                        try {
                            XPropertySet xPropertySet = QI.XPropertySet(userDefinedProperties);

                            xPropertySet.setPropertyValue("DiagramModel", s);
                        } catch (UnknownPropertyException e1) {
                            e1.printStackTrace();
                        } catch (PropertyVetoException e1) {
                            e1.printStackTrace();
                        } catch (IllegalArgumentException e1) {
                            e1.printStackTrace();
                        } catch (WrappedTargetException e1) {
                            e1.printStackTrace();
                        }
                    }
                }

            }

            @Override
            public void documentEventOccured(String eventName) {
                documentEventOccured(new DocumentEvent(null, eventName, null, null));
            }
        });
    }

    public DiagramModel getDiagramModel() {
        return diagramModel;
    }


    Map<MyURL, Set<XStatusListener>> statusListeners = new HashMap();


    void statusChangedDisable(URL url) {

        MyURL myURL = new MyURL(url);

        FeatureStateEvent featureStateEvent = new FeatureStateEvent();
        featureStateEvent.Source = this;
        featureStateEvent.IsEnabled = false;
        featureStateEvent.FeatureDescriptor = "QWE";
        featureStateEvent.FeatureURL = url;


        statusChanged(url, featureStateEvent);
    }

    public void statusChangedEnable(URL url) {

        FeatureStateEvent featureStateEvent = new FeatureStateEvent();
        featureStateEvent.Source = this;
        featureStateEvent.IsEnabled = true;
        featureStateEvent.FeatureDescriptor = "QWE";
        featureStateEvent.FeatureURL = url;

        statusChanged(url, featureStateEvent);
    }


    @Override
    public void addStatusListener(XStatusListener xStatusListener, URL url) {

        MyURL myURL = new MyURL(url);
        if (!statusListeners.containsKey(url)) {

            statusListeners.put(myURL, new HashSet<XStatusListener>());
        }

        statusListeners.get(myURL).add(xStatusListener);


        if (url.Complete.equals("ru.ssau.graphplus:DropdownCmd")) {
            FeatureStateEvent featureStateEvent = new FeatureStateEvent();
            ControlCommand controlCommand = new ControlCommand();
            controlCommand.Command = "SetList";
            controlCommand.Arguments = new NamedValue[]{
                    new NamedValue("List", new String[]{"StartMethodOfProcess", "Composition", "Channel"})
            };
            featureStateEvent.State = controlCommand;
            featureStateEvent.IsEnabled = true;
            featureStateEvent.FeatureURL = url;
            xStatusListener.statusChanged(featureStateEvent);
            return;
        }


        FeatureStateEvent featureStateEvent = new FeatureStateEvent();
        //featureStateEvent.Source = this;
        featureStateEvent.IsEnabled = true;
        featureStateEvent.FeatureDescriptor = "QWE";
        featureStateEvent.FeatureURL = url;


        xStatusListener.statusChanged(featureStateEvent);

    }

    @Override
    public void removeStatusListener(XStatusListener xStatusListener, URL url) {

        MyURL myURL = new MyURL(url);

        if (statusListeners.containsKey(myURL)) {
            statusListeners.get(myURL).remove(xStatusListener);
        }
    }

    private Layout layout;


    @Override
    public void dispatch(URL url, PropertyValue[] propertyValues) {
        //To change body of implemented methods use File | Settings | File Templates.  Logger anonymousLogger = Logger.getAnonymousLogger();
        OOGraph.LOGGER.info("dispatch");
        OOGraph.LOGGER.info(url.toString());


        if (url.Protocol.compareTo("ru.ssau.graphplus:") == 0) {


//            if ( url.Path.compareTo("DemoOptionCommand") == 0 )
//            {
//                // Write out to Office
//                XModel xDocModel = this.m_xFrame.getController().getModel();
//
//
//                // Construct the string we want to output to the Office document
//                StringBuffer buf = new StringBuffer();
//                buf.append("OptionsPageDemo - current values stored in OOo configuration:\n");
//                buf.append("===============================================================\n");
//
//                for(String controlName : OptionsDialogEventHandler._DialogEventHandler.ControlNames)
//                {
//                    try
//                    {
//                        // Retrieve the configuration values from the OOo registry.
//                        // load the values from the registry
//                        // To access the registry we have previously created a service instance
//                        // of com.sun.star.configuration.ConfigurationUpdateAccess which supports
//                        // com.sun.star.container.XNameAccess. We obtain now the section
//                        // of the registry which is assigned to this options page.
//                        XPropertySet xLeaf = (XPropertySet) UnoRuntime.queryInterface(
//                                XPropertySet.class, this.accessLeaves.getByName("FooOptionsPage"));
//                        if (xLeaf == null)
//                            buf.append("XPropertySet not supported.");
//
//                        // The properties in the registry have the same name as the respective
//                        // controls. We use the names now to obtain the property values.
//
//                        Object value = xLeaf.getPropertyValue(controlName);
//                        if(controlName.startsWith("lst"))
//                        {
//                            value = ((String[])value)[
//                                    ((short[])xLeaf.getPropertyValue(controlName + "Selected"))[0]];
//                        }
//
//                        buf.append(controlName);
//                        buf.append(": ");
//                        buf.append(value);
//                        buf.append("\n");
//                    }
//                    catch(Exception ex)
//                    {
//                        buf.append(ex.getLocalizedMessage());
//                        buf.append('\n');
//                    }
//                }
//                buf.append('\n');
//
//                OOGraph.LOGGER.info(buf.toString());
//                return;
//            }


            if (url.Complete.equals("ru.ssau.graphplus:Omg")) {
                tableInserter.insertTable(getDiagramModel().getDrawDoc());
            }


            if (url.Complete.equals("ru.ssau.graphplus:DropdownCmd")) {

                OOGraph.LOGGER.info(url.Complete);

                Object value = propertyValues[1].Value;
                String string = (String) propertyValues[1].Value;

                // TODO
                //                    setDiagramType(string);
                return;

            }


            Link link = null;
            NodeBase node = null;

            if (url.Path.compareTo(PROCESS_NODE) == 0) {
                try {


                    Object instanceWithContext = xMCF.createInstanceWithContext("com.sun.star.task.JobExecutor", m_xContext);
                    XJobExecutor jobExecutor = UnoRuntime.queryInterface(XJobExecutor.class, instanceWithContext);

                    jobExecutor.trigger("vnd.sun.star.job:service=ru.ssau.graphplus.OOGraph,alias=MonitorJob,event=BlaBla");

                    XDrawPage xPage = PageHelper.getDrawPageByIndex(getDiagramModel().getDrawDoc(), 0);
                    XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, xPage);


                    NodeBase processNode = nodeFactory.create(Node.NodeType.StartMethodOfProcess);
                    node = processNode;


                    XPropertySet xPS = QI.XPropertySet(node.getShape());

                    XNameContainer xNC2 = QI.XNameContainer(xPS.getPropertyValue("UserDefinedAttributes"));


                    DrawHelper.insertShapeOnCurrentPage(node.getShape(), getDiagramModel().getDrawDoc());

                    MiscHelper.addUserDefinedAttributes(node.getShape(), xMSF);

                    MiscHelper.tagShapeAsNode(node.getShape());
                    MiscHelper.setNodeType(node.getShape(), Node.NodeType.StartMethodOfProcess);

//                    DrawHelper.setShapePositionAndSize(node.getShape(), 100, 100, 1800, 1500);
                    DrawHelper.setShapeSize(node.getShape(), 1800, 1500);

                    //return;

                } catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                } catch (WrappedTargetException ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                } catch (com.sun.star.uno.Exception ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (url.Path.compareTo(PROCEDURE_NODE) == 0) {
                try {

                    XDrawPage xPage = PageHelper.getDrawPageByIndex(getDiagramModel().getDrawDoc(), 0);
                    ProcedureNode procedureNode = (ProcedureNode) nodeFactory.create(Node.NodeType.MethodOfProcess);//createAndInsert(NodeBase.NodeType.StartMethodOfProcess, m_xComponent, xShapes);
                    node = procedureNode;

                    DrawHelper.insertShapeOnCurrentPage(procedureNode.getShape(), getDiagramModel().getDrawDoc());

                    MiscHelper.addUserDefinedAttributes(procedureNode.getShape(), xMSF);
                    MiscHelper.setNodeType(procedureNode.getShape(), Node.NodeType.MethodOfProcess);

                    MiscHelper.tagShapeAsNode(procedureNode.getShape());

//                    DrawHelper.setShapePositionAndSize(procedureNode.getShape(), 100, 100, 2000, 1500);

                    DrawHelper.setShapeSize(procedureNode.getShape(), 2000, 1500);

                    //return;

                } catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                } catch (WrappedTargetException ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                } catch (com.sun.star.uno.Exception ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
            }


            if (url.Path.compareTo(CLIENT_NODE) == 0) {
                try {
                    ClientNode clientNode = (ClientNode) nodeFactory.create(Node.NodeType.ClientPort);
                    node = clientNode;

                    DrawHelper.insertNodeOnCurrentPage(clientNode, getDiagramModel().getDrawDoc());

                    MiscHelper.addUserDefinedAttributes(clientNode.getShape(), xMSF);
                    MiscHelper.setNodeType(clientNode.getShape(), Node.NodeType.ClientPort);

                    MiscHelper.tagShapeAsNode(clientNode.getShape());

                    clientNode.setProps();
//                    DrawHelper.setShapePositionAndSize(clientNode.getShape(), 100, 100, 1500, 1500);

                    DrawHelper.setShapeSize(clientNode.getShape(), 1500, 1500);


                } catch (java.lang.Exception ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }

            }

            if (url.Path.compareTo(SERVER_NODE) == 0) {
                try {
                    ServerNode serverNode = (ServerNode) nodeFactory.create(Node.NodeType.ServerPort);
                    node = serverNode;

                    DrawHelper.insertShapeOnCurrentPage(serverNode.getShape(), getDiagramModel().getDrawDoc());

                    MiscHelper.addUserDefinedAttributes(serverNode.getShape(), xMSF);
                    MiscHelper.tagShapeAsNode(serverNode.getShape());
                    MiscHelper.setNodeType(serverNode.getShape(), Node.NodeType.ServerPort);


                    node.setProps();
//                    DrawHelper.setShapePositionAndSize(serverNode.getShape(), 100, 100, 1500, 1500);
                    DrawHelper.setShapeSize(serverNode.getShape(), 1500, 1500);


                } catch (PropertyVetoException ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);

                }
            }

            Iterable<XShape> linkShapes = new ArrayList<XShape>();

            if (url.Path.compareTo(MIXED_LINK) == 0) {

                MixedLink mixedLink = (MixedLink) linkFactory.create(Link.LinkType.MixedFlow);
                link = mixedLink;
                linkShapes = mixedLink.getShapes();

            }

            if (url.Path.compareTo(DATA_LINK) == 0) {

                DataLink dataLink = (DataLink) linkFactory.create(Link.LinkType.DataFlow);
                link = dataLink;
                linkShapes = dataLink.getShapes();

            }

            if (url.Path.compareTo(CONTROL_LINK) == 0) {

                ControlLink controlLink = (ControlLink) linkFactory.create(Link.LinkType.ControlFlow);
                link = controlLink;
                linkShapes = controlLink.getShapes();

            }


            if (anyLink(url)) {
                // common for all links

                for (XShape shape : linkShapes) {
                    DrawHelper.insertShapeOnCurrentPage(shape, getDiagramModel().getDrawDoc());
                }

                if (link != null) {
                    link.setProps();

//                    getDiagramController().setLinker(link);

                    if (Settings.getSettings().mouseLinkingMode() && diagramModel.getNodes().size() >= 2) {
                        getDiagramController().setInputMode(new InputTwoShapesMode(getDiagramController(), link));
                    }

                    statusChangedDisable(url);
                    getDiagramModel().addDiagramElement(link);
                    diagramController.configureListeners(link);

                    layout.layout(new DiagramElementObj(link));
                }


                return;
            }


            if (anyNode(url)) {
                if (Settings.getSettings().promptForNodeName()) {
                    CreateNodeDialog createNodeDialog = new CreateNodeDialog(node.getShape(), m_xContext);
                    XModel xModel = QI.XModel(xDrawDoc);
                    XDialog dialog = createDialog(CreateNodeDialog.CREATE_NODE_DIALOG_XDL, xModel, m_xFrame, createNodeDialog, false);
                    createNodeDialog.init(dialog);
                    dialog.execute();
                }


                // common for all nodes
                if (node != null) {

                    getDiagramModel().addDiagramElement(node);
                    MiscHelper.setId(node.getShape(), node.getId());
                    MiscHelper.tagShapeAsNode(node.getShape());


                    layout.layout(new DiagramElementObj(node));
                }


                return;
            }

            if (url.Path.compareTo("LinkNodesToolbar") == 0) {
//                XModel xModel = QI.XModel(xDrawDoc);
//                XWindow linkNodesDialog = createLinkNodesDialog(xModel, m_xFrame);

            }

            if (url.Path.compareTo("GetCode") == 0) {
                XModel xModel = QI.XModel(xDrawDoc);

                try {
                    Object oClipboard =
                            xMCF.createInstanceWithContext(
                                    "com.sun.star.datatransfer.clipboard.SystemClipboard",
                                    m_xContext);


                    XShapes xShapes;// = QI.XShapes(xDrawDoc);
                    XDrawPage currentDrawPage = DrawHelper.getCurrentDrawPage(xDrawDoc);
                    xShapes = QI.XShapes(currentDrawPage);
                    Set<XShape> shapes = Sets.newHashSet();

                    for (int i = 0; i < xShapes.getCount(); i++) {
                        shapes.add(QI.XShape(xShapes.getByIndex(i)));
                    }


                    DiagramTypeRecognition diagramTypeRecognition = new DiagramTypeRecognitionImpl();
                    DiagramType diagramType = diagramTypeRecognition.recognise(shapes);

                    //TODO DI

                    diagramWalker.setDiagramType(diagramType);
                    List<ConnectedShapesComplex> collectedConnectedShapes = diagramWalker.walk(shapes, null);

                    CodeGenerator codeGenerator = new DiagramCodeGenerator();
                    diagramModel.setConnectedShapesComplexes(collectedConnectedShapes);
                    String code = codeGenerator.generateCode(new DiagramCodeSource(diagramModel, collectedConnectedShapes, diagramType));


                    GetCodeDialog myDialog = new GetCodeDialog(code, oClipboard);

                    XDialog dialog = createDialog(GetCodeDialog.GET_CODE_DIALOG_XDL, xModel, m_xFrame, myDialog, false);
                    myDialog.init(dialog);
                    dialog.execute();

                } catch (Exception e) {
                    throw new RuntimeException("", e);
                }


            }

            if (url.Path.compareTo("About") == 0) {
                XModel xModel = QI.XModel(xDrawDoc);
                XDialog dialog = createDialog("vnd.sun.star.extension://ru.ssau.graphplus.oograph/dialogs/AboutDialog.xdl", xModel, m_xFrame);

            }


            // TODO add

//            if (url.Complete.endsWith(msShowCommand))
//                PanelOptionDialog.Show();


        }

    }

    private boolean anyLink(URL url) {
        return url.Path.compareTo(MIXED_LINK) == 0 || url.Path.compareTo(CONTROL_LINK) == 0 || url.Path.compareTo(DATA_LINK) == 0;
    }

    final static String msShowCommand = "ShowOptionsDialog";


    private boolean anyNode(URL url) {
        return url.Path.equals(CLIENT_NODE) || url.Path.equals(PROCEDURE_NODE) || url.Path.equals(PROCESS_NODE) || url.Path.equals(SERVER_NODE);
    }

    public XDialog createDialog(String xdlUrl, XModel xModel, XFrame xFrame) {
        XDialog xDialog = null;
        try {
            XMultiComponentFactory xMCF = m_xContext.getServiceManager();
            Object obj;

            // If valid we must pass the XModel when creating a DialogProvider object
            if (xModel != null) {
                Object[] args = new Object[1];
                args[0] = xModel;

                obj = xMCF.createInstanceWithArgumentsAndContext(
                        "com.sun.star.awt.DialogProvider2", args, m_xContext);
            } else {
                obj = xMCF.createInstanceWithContext(
                        "com.sun.star.awt.DialogProvider2", m_xContext);
            }

            XDialogProvider2 xDialogProvider = (XDialogProvider2)
                    UnoRuntime.queryInterface(XDialogProvider2.class, obj);

            xDialog = xDialogProvider.createDialog(xdlUrl);
            XControl xControl = (XControl) UnoRuntime.queryInterface(
                    XControl.class, xDialog);


            if (xDialog != null) {
                xDialog.execute();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return xDialog;

    }


    public XDialog createDialog(String xdlUrl, XModel xModel, XFrame xFrame, MyDialog myDialog, boolean execute) {

        XDialog xDialog = null;
        try {
            XMultiComponentFactory xMCF = m_xContext.getServiceManager();
            Object obj;

            // If valid we must pass the XModel when creating a DialogProvider object
            if (xModel != null) {
                Object[] args = new Object[1];
                args[0] = xModel;

                obj = xMCF.createInstanceWithArgumentsAndContext(
                        "com.sun.star.awt.DialogProvider2", args, m_xContext);
            } else {
                obj = xMCF.createInstanceWithContext(
                        "com.sun.star.awt.DialogProvider2", m_xContext);
            }

            XDialogProvider2 xDialogProvider = (XDialogProvider2)
                    UnoRuntime.queryInterface(XDialogProvider2.class, obj);

            xDialog = xDialogProvider.createDialogWithHandler(xdlUrl,
                    myDialog.getDialogHandler());
            XControl xControl = (XControl) UnoRuntime.queryInterface(
                    XControl.class, xDialog);


            if (xDialog != null && execute) {
                xDialog.execute();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return xDialog;

    }


    void statusChanged(URL url, FeatureStateEvent featureStateEvent) {
        MyURL myURL = new MyURL(url);
        Set<XStatusListener> xStatusListeners = statusListeners.get(myURL);
        for (XStatusListener xStatusListener : xStatusListeners) {
            xStatusListener.statusChanged(featureStateEvent);
        }
    }


    public DiagramController getDiagramController() {
        return diagramController;
    }

    private String getPackageLocation() {
        Object valueByName = m_xContext.getValueByName("/singletons/com.sun.star.deployment.PackageInformationProvider");
        XPackageInformationProvider xPackageInformationProvider = UnoRuntime.queryInterface(XPackageInformationProvider.class, valueByName);
        return xPackageInformationProvider.getPackageLocation("ru.ssau.graphplus.oograph");
    }

    public void setDiagramModel(DiagramModel diagramModel) {
        this.diagramModel = diagramModel;
    }
}
