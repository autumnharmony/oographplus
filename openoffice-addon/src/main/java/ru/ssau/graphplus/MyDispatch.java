/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.star.awt.*;
import com.sun.star.awt.tree.XMutableTreeDataModel;
import com.sun.star.beans.*;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.document.*;
import com.sun.star.document.XEventListener;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.drawing.XShapes;
import com.sun.star.frame.*;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.*;
import com.sun.star.uno.*;
import com.sun.star.uno.Exception;
import com.sun.star.uno.RuntimeException;
import com.sun.star.util.URL;
import ru.ssau.graphplus.codegen.impl.DiagramCodeGenerator;
import ru.ssau.graphplus.codegen.impl.DiagramCodeSource;
import ru.ssau.graphplus.codegen.impl.analizer.DiagramWalker;
import ru.ssau.graphplus.api.DiagramService;
import ru.ssau.graphplus.api.DiagramType;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.codegen.CodeGenerator;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.MiscHelper;
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.di.AddonModule;
import ru.ssau.graphplus.document.event.handler.DocumentEventHandler;
import ru.ssau.graphplus.document.event.handler.DocumentEventsHandler;
import ru.ssau.graphplus.document.event.handler.impl.DocumentEventsHandlerImpl;
import ru.ssau.graphplus.gui.DiagramElementObj;
import ru.ssau.graphplus.gui.Gui;
import ru.ssau.graphplus.gui.Layout;
import ru.ssau.graphplus.gui.MyDialog;
import ru.ssau.graphplus.gui.dialogs.CreateNodeDialog;
import ru.ssau.graphplus.gui.dialogs.GetCodeDialog;
import ru.ssau.graphplus.gui.dialogs.ValidationDialog;
import ru.ssau.graphplus.link.*;
import ru.ssau.graphplus.node.*;
import ru.ssau.graphplus.codegen.impl.recognition.CantRecognizeType;
import ru.ssau.graphplus.codegen.impl.recognition.DiagramTypeRecognition;
import ru.ssau.graphplus.codegen.impl.recognition.DiagramTypeRecognitionImpl;
import ru.ssau.graphplus.validation.ValidationResult;
import ru.ssau.graphplus.validation.Validator;
import ru.ssau.graphplus.validation.impl.ValidatorImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.ssau.graphplus.Constants.*;

/**
 * Dispatch wrapper
 */
public class MyDispatch implements XDispatch {

    private final XFrame m_xFrame;
    private final TableInserterImpl tableInserter;
    private final XMultiServiceFactory xMSF;
    private final NodeFactory nodeFactory;
    private final LinkFactory linkFactory;
    private final XMultiComponentFactory xMCF;
    private final XComponentContext m_xContext;
    private final DiagramService diagramService;
    private final XUndoManager undoManager;
    private final DiagramWalker diagramWalker;
    private XComponent xDrawDoc;
    private Logger logger = Logger.getLogger("omg");
    private final Map<MyURL, Set<XStatusListener>> statusListeners = new HashMap();
    private DiagramModel diagramModel;
    private DiagramController diagramController;
    private DocumentEventsHandler documentEventsHandler = new DocumentEventsHandlerImpl();
    private XDocumentEventBroadcaster m_xNewBroadcaster;
    private XEventBroadcaster m_xOldBroadcaster;
    private Layout layout;
    private final DiagramTypeRecognition diagramTypeRecognition;


    public MyDispatch(XComponent xDrawDoc, XComponentContext m_xContext, XFrame m_xFrame, XMultiComponentFactory xMCF, XMultiServiceFactory xMSF) {
        this.xDrawDoc = xDrawDoc;
        XUndoManagerSupplier xUndoManagerSupplier = QI.XUndoManagerSupplier(xDrawDoc);
        undoManager = xUndoManagerSupplier.getUndoManager();


        this.m_xContext = m_xContext;
        this.m_xFrame = m_xFrame;
//        anonymousLogger = Logger.getAnonymousLogger();
//        logger = anonymousLogger;

        this.xMCF = xMCF;
        this.xMSF = xMSF;
        tableInserter = new TableInserterImpl(xMSF);


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
            diagramTypeRecognition = new DiagramTypeRecognitionImpl();
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

    public NodeFactory getNodeFactory() {
        return nodeFactory;
    }

    public LinkFactory getLinkFactory() {
        return linkFactory;
    }

    public XFrame getFrame() {
        return m_xFrame;
    }

    public XMultiServiceFactory getxMSF() {
        return xMSF;
    }

    public XMultiComponentFactory getxMCF() {
        return xMCF;
    }

    private void onLoadHandler() {
        xDrawDoc = UnoRuntime.queryInterface(XComponent.class, m_xFrame.getController().getModel());
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

        logger.info("setupDocumentEventsHandler");

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

                    XDocumentPropertiesSupplier xDocumentPropertiesSupplier = UnoRuntime.queryInterface(XDocumentPropertiesSupplier.class, xDrawDoc);
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

    public void setDiagramModel(DiagramModel diagramModel) {
        this.diagramModel = diagramModel;
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

    @Override
    public void dispatch(URL url, PropertyValue[] propertyValues) {

        logger.info("dispatch");
        logger.info(url.toString());


        try {

            if (url.Protocol.compareTo("ru.ssau.graphplus:") == 0) {


                Link link = null;
                NodeBase node = null;

                if (url.Path.compareTo(PROCESS_NODE) == 0) {
                    try {

                        NodeBase processNode = nodeFactory.create(Node.NodeType.StartMethodOfProcess);
                        node = processNode;
                        DrawHelper.insertShapeOnCurrentPage(node.getShape(), getDiagramModel().getDrawDoc());
                        MiscHelper.addUserDefinedAttributes(node.getShape(), xMSF);
                        MiscHelper.tagShapeAsNode(node.getShape());
                        MiscHelper.setNodeType(node.getShape(), Node.NodeType.StartMethodOfProcess);
                        DrawHelper.setShapeSize(node.getShape(), 1800, 1500);
                    } catch (com.sun.star.uno.Exception ex) {
                        Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                if (url.Path.compareTo(PROCEDURE_NODE) == 0) {
                    try {

                        ProcedureNode procedureNode = (ProcedureNode) nodeFactory.create(Node.NodeType.MethodOfProcess);//createAndInsert(NodeBase.NodeType.StartMethodOfProcess, xDrawDoc, xShapes);
                        node = procedureNode;

                        DrawHelper.insertShapeOnCurrentPage(procedureNode.getShape(), getDiagramModel().getDrawDoc());

                        MiscHelper.addUserDefinedAttributes(procedureNode.getShape(), xMSF);
                        MiscHelper.setNodeType(procedureNode.getShape(), Node.NodeType.MethodOfProcess);

                        MiscHelper.tagShapeAsNode(procedureNode.getShape());

                        DrawHelper.setShapeSize(procedureNode.getShape(), 2000, 1500);

                        //return;

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
                        Set<XShape> allShapes = Sets.newHashSet();

                        for (int i = 0; i < xShapes.getCount(); i++) {
                            allShapes.add(QI.XShape(xShapes.getByIndex(i)));
                        }



                        DiagramType diagramType = diagramTypeRecognition.recognise(allShapes);

                        //TODO DI

                        diagramWalker.setDiagramType(diagramType);
                        List<ConnectedShapesComplex> collectedConnectedShapes = diagramWalker.walk(allShapes, null);



                        CodeGenerator codeGenerator = new DiagramCodeGenerator(DrawHelper.getPageName(currentDrawPage));
                        diagramModel.setConnectedShapesComplexes(collectedConnectedShapes);




                        Set<XShape> usedShapes = new HashSet<>();
                        for (ConnectedShapesComplex connectedShapesComplex : collectedConnectedShapes){
                            usedShapes.addAll(Lists.newArrayList(connectedShapesComplex.getShapes()));
                        }


                        Set<XShape> unusedShapes = new HashSet<>(allShapes);
                        unusedShapes.removeAll(usedShapes);


                        Validator validator = new ValidatorImpl(diagramModel, unusedShapes);
                        ValidationResult validate = validator.validate(diagramModel);


                        if (validate.getItems().size() > 0) {
                            ValidationDialog validationDialog = new ValidationDialog();
                            XWindow dialog = createWindow(ValidationDialog.VALIDATION_DIALOG_XDL, xModel, m_xFrame, validationDialog.getDialogHandler(), false);
                            Object instance = xMCF.createInstanceWithContext("com.sun.star.awt.tree.MutableTreeDataModel", m_xContext);

                            XMutableTreeDataModel treeDataModel = QI.XMutableTreeDataModel(instance);



                            validationDialog.init(dialog, validate, treeDataModel, diagramService);
                            dialog.setVisible(true);
                        } else {

                            String code = codeGenerator.generateCode(new DiagramCodeSource(diagramModel, collectedConnectedShapes, diagramType));
                            DiagramCodeGenerator generator = (DiagramCodeGenerator) codeGenerator;


                            GetCodeDialog myDialog = new GetCodeDialog(code, oClipboard);


                            XDialog dialog = createDialog(GetCodeDialog.GET_CODE_DIALOG_XDL, xModel, m_xFrame, myDialog, false);
                            myDialog.init(dialog);
                            dialog.execute();
                        }

                    }
                    catch(CantRecognizeType cantRecognizeType){

                        diagramController.setSelectedShape(cantRecognizeType.getShape());
                        throw new java.lang.RuntimeException(cantRecognizeType);
                    }
                    catch (Exception e) {
                        throw new RuntimeException("", e);
                    }


                }

                if (url.Path.compareTo("About") == 0) {
                    XDialog dialog = createDialog("vnd.sun.star.extension://ru.ssau.graphplus.oograph/dialogs/AboutDialog.xdl", QI.XModel(xDrawDoc), m_xFrame, false);
                    dialog.execute();

                }


                if (url.Path.compareTo("Help") == 0) {

                }

            }
        } catch (java.lang.Exception e) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(byteArrayOutputStream));
            Gui.showErrorMessageBox(null, "Error", "" + e.getMessage() + " " + new String(byteArrayOutputStream.toByteArray()), xMCF, m_xContext);
        }

    }

    private boolean anyLink(URL url) {
        return url.Path.compareTo(MIXED_LINK) == 0 || url.Path.compareTo(CONTROL_LINK) == 0 || url.Path.compareTo(DATA_LINK) == 0;
    }

    private boolean anyNode(URL url) {
        return url.Path.equals(CLIENT_NODE) || url.Path.equals(PROCEDURE_NODE) || url.Path.equals(PROCESS_NODE) || url.Path.equals(SERVER_NODE);
    }

    public XDialog createDialog(String xdlUrl, XModel xModel, XFrame xFrame) {
        return createDialog(xdlUrl, xModel, xFrame, true);
    }

    public XDialog createDialog(String xdlUrl, XModel xModel, XFrame xFrame, boolean execute) {
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


            if (xDialog != null && execute) {
                xDialog.execute();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return xDialog;

    }

    public XWindow createWindow(String xdlUrl, XModel xModel, XFrame xFrame, MyDialogHandler handler, boolean execute) throws Exception {

        XWindow xWindow;
        XMultiComponentFactory xMCF = m_xContext.getServiceManager();
        Object obj;

        // If valid we must pass the XModel when creating a DialogProvider object
        if (xModel != null) {
            Object[] args = new Object[1];
            args[0] = xModel;

            obj = xMCF.createInstanceWithArgumentsAndContext(
                    "com.sun.star.awt.ContainerWindowProvider", args, m_xContext);
        } else {
            obj = xMCF.createInstanceWithContext(
                    "com.sun.star.awt.ContainerWindowProvider", m_xContext);
        }

        XContainerWindowProvider xDialxContainerWindowProvidergProvider = UnoRuntime.queryInterface(XContainerWindowProvider.class, obj);

        XWindow containerWindow = xFrame.getContainerWindow();
        XWindowPeer xParentPeer = UnoRuntime.queryInterface(XWindowPeer.class, containerWindow);


        xWindow = xDialxContainerWindowProvidergProvider.createContainerWindow(xdlUrl, "Validation", xParentPeer,
                handler);


        return xWindow;
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

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }
}
