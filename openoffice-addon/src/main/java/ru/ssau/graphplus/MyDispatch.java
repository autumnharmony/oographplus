/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.sun.star.awt.*;
import com.sun.star.awt.tree.XMutableTreeDataModel;
import com.sun.star.beans.*;
import com.sun.star.container.XNameAccess;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.document.*;
import com.sun.star.document.XEventListener;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.drawing.XShapes;
import com.sun.star.frame.*;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.*;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.uno.*;
import com.sun.star.uno.Exception;
import com.sun.star.uno.RuntimeException;
import com.sun.star.util.URL;
import ru.ssau.graphplus.codegen.impl.DiagramCodeGenerator;
import ru.ssau.graphplus.api.DiagramService;
import ru.ssau.graphplus.api.DiagramType;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.codegen.CodeGenerator;
import ru.ssau.graphplus.codegen.impl.*;
import ru.ssau.graphplus.api.Graph;
import ru.ssau.graphplus.codegen.impl.analizer.Walker;
import ru.ssau.graphplus.codegen.impl.recognition.CantRecognizeType;
import ru.ssau.graphplus.codegen.impl.recognition.DiagramTypeRecognition;
import ru.ssau.graphplus.commons.*;
import ru.ssau.graphplus.di.AddonModule;
import ru.ssau.graphplus.document.event.handler.DocumentEventHandler;
import ru.ssau.graphplus.document.event.handler.DocumentEventsHandler;
import ru.ssau.graphplus.document.event.handler.impl.DocumentEventsHandlerImpl;
import ru.ssau.graphplus.events.*;
import ru.ssau.graphplus.events.EventListener;
import ru.ssau.graphplus.gui.DiagramElementObj;
import ru.ssau.graphplus.gui.Gui;
import ru.ssau.graphplus.gui.Layout;
import ru.ssau.graphplus.gui.MyDialog;
import ru.ssau.graphplus.gui.dialogs.CreateNodeDialog;
import ru.ssau.graphplus.gui.dialogs.GetCodeDialog;
import ru.ssau.graphplus.gui.dialogs.ValidationDialog;
import ru.ssau.graphplus.link.*;
import ru.ssau.graphplus.node.*;
import ru.ssau.graphplus.validation.ValidationResult;
import ru.ssau.graphplus.validation.Validator;
import ru.ssau.graphplus.validation.impl.ValidatorImpl;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

import static ru.ssau.graphplus.Constants.*;

public class MyDispatch implements XDispatch {

    public static final String COM_SUN_STAR_DATATRANSFER_CLIPBOARD_SYSTEM_CLIPBOARD = "com.sun.star.datatransfer.clipboard.SystemClipboard";
    private XFrame m_xFrame;
    private final TableInserterImpl tableInserter;
    private final XMultiServiceFactory xMSF;
    private final NodeFactory nodeFactory;
    private final LinkFactory linkFactory;
    private final XMultiComponentFactory xMCF;
    private final XComponentContext m_xContext;
    private final DiagramService diagramService;
    private final XUndoManager undoManager;
    private final DiagramWalker diagramWalker;
    private final CodeGeneratorFactory codeGeneratorFactory;
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
    private Injector injector;

    public XComponent getDrawDoc() {
        return xDrawDoc;
    }
    public void setFrame(XFrame m_xFrame) {
        this.m_xFrame = m_xFrame;
    }

    private Collection<XShape> getConnected(XShape xShape) {
        XDrawPage currentDrawPage = DrawHelper.getCurrentDrawPage(xDrawDoc);
        XShapes xShapes = QI.XShapes(currentDrawPage);
        List<XShape> result = new ArrayList<>();
        for (int i = 0; i < xShapes.getCount(); i++) {
            try {
                XShape shape = QI.XShape(xShapes.getByIndex(i));
                if (ShapeHelper.isConnectorShape(shape)) {
                    XPropertySet xPropertySet = QI.XPropertySet(shape);
                    XShape startShape = QI.XShape(xPropertySet.getPropertyValue("StartShape"));
                    XShape endShape = QI.XShape(xPropertySet.getPropertyValue("EndShape"));
                    if (startShape != null) {
                        if (UnoRuntime.areSame(startShape, xShape)) {
                            result.add(startShape);
                        }
                    }
                    if (endShape != null) {
                        if (UnoRuntime.areSame(endShape, xShape)) {
                            result.add(endShape);
                        }
                    }
                }
            } catch (IndexOutOfBoundsException | WrappedTargetException | UnknownPropertyException e) {
                e.printStackTrace();
                throw new java.lang.RuntimeException(e);
            }
        }
        return result;
    }

    public MyDispatch(final XComponent xDrawDoc, XComponentContext m_xContext, XFrame m_xFrame, XMultiComponentFactory xMCF, XMultiServiceFactory xMSF) {
        this.xDrawDoc = xDrawDoc;
        XUndoManagerSupplier xUndoManagerSupplier = QI.XUndoManagerSupplier(xDrawDoc);
        undoManager = xUndoManagerSupplier.getUndoManager();
        XUserInputInterception xUserInputInterception = UnoRuntime.queryInterface(XUserInputInterception.class, QI.XModel(xDrawDoc).getCurrentController());
        xUserInputInterception.addKeyHandler(new XKeyHandler() {
            @Override
            public boolean keyPressed(KeyEvent keyEvent) {
                if (keyEvent.KeyCode == 1286) {
                    Object currentSelection = QI.XModel(xDrawDoc).getCurrentSelection();
                    XShapes xShapes = QI.XShapes(currentSelection);
                    int count = xShapes.getCount();
//                    if (count > 1) return false;
                    for (int i = 0; i < xShapes.getCount(); i++) {
                        Object byIndex = null;
                        try {
                            byIndex = xShapes.getByIndex(i);
                            XShape shape = QI.XShape(byIndex);
                            if (ShapeHelper.isConnectorShape(shape)) {
                                XConnectorShape xConnectorShape = QI.XConnectorShape(shape);
                                XShape startShape = QI.XShape(QI.XPropertySet(shape).getPropertyValue("StartShape"));
                                XShape endShape = QI.XShape(QI.XPropertySet(shape).getPropertyValue("EndShape"));
                                if (startShape != null && endShape != null) {
                                    diagramController.onShapeRemoved(shape);
                                    return true;
                                }
                                return false;
                            } else if (ShapeHelper.isTextShape(shape)) {
                                Collection<XShape> connected = getConnected(shape);
                                if (connected.size() == 2) {
                                    diagramController.onShapeRemoved(connected.iterator().next());
                                    return true;
                                }
                            } else {
                            }
                        } catch (IndexOutOfBoundsException | WrappedTargetException | UnknownPropertyException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return false;
            }
            @Override
            public boolean keyReleased(KeyEvent keyEvent) {
                return false;
            }
            @Override
            public void disposing(EventObject eventObject) {
            }
        });
        try {
            Object oProvider = m_xContext.getServiceManager().createInstanceWithContext("com.sun.star.configuration.ConfigurationProvider", m_xContext);
            XMultiServiceFactory xConfigurationServiceFactory =
                    (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, oProvider);
            PropertyValue[] lArgs = new PropertyValue[1];
            lArgs[0] = new PropertyValue();
            lArgs[0].Name = "nodepath";
            lArgs[0].Value = "/org.openoffice.Setup/L10N";
            Object configAccess = xConfigurationServiceFactory.createInstanceWithArguments(
                    "com.sun.star.configuration.ConfigurationAccess", lArgs);
            XNameAccess xNameAccess = (XNameAccess) UnoRuntime.queryInterface(XNameAccess.class, configAccess);
            Object ooLocale = xNameAccess.getByName("ooLocale");
            String sOoLocale = (String) ooLocale;
            Global.locale = sOoLocale;
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.m_xContext = m_xContext;
        this.m_xFrame = m_xFrame;
//        anonymousLogger = Logger.getAnonymousLogger();
//        logger = anonymousLogger;
        this.xMCF = xMCF;
        this.xMSF = xMSF;
        tableInserter = new TableInserterImpl(xMSF);
        try {
            diagramModel = new DiagramModel(xDrawDoc);
            diagramModel.addEventListener(NodeModifiedEvent.class, new EventListener<NodeModifiedEvent>() {

                @Override
                public void onEvent(NodeModifiedEvent event) {
                    if (Settings.getSettings().isAutolayoutComplexLinks()) {
                        Node node = event.getNode();
                        Graph graph = diagramModel.getGraph();
                        if (graph == null) {
                            return;
                        }
                        Table<Node, Node, List<Link>> table = graph.getTable();
                        Map<Node, List<Link>> column = table.column(node);
                        Map<Node, List<Link>> row = table.row(node);
                        adjust(node, column, row);

                    }
                }

                private void adjust(Node node, Map<Node, List<Link>> column, Map<Node, List<Link>> row) {
                    for (Map.Entry<Node, List<Link>> entry : column.entrySet()) {
                        for (Link link : entry.getValue()) {
                            Node key = entry.getKey();

                            adjustImpl((LinkBase) link, (NodeBase) key, (NodeBase) node);
                            diagramService.layoutLink(key, node, link);
                        }
                    }
                    for (Map.Entry<Node, List<Link>> entry : row.entrySet()) {
                        for (Link link : entry.getValue()) {
                            Node key = entry.getKey();
                            adjustImpl((LinkBase) link, (NodeBase) node, (NodeBase) key);
                            diagramService.layoutLink(key, node, link);
                        }
                    }
                }

                private void adjustImpl(LinkBase linkBase , NodeBase nodeBase1, NodeBase nodeBase2){
                    new LinkAdjusterImpl().adjustLink(linkBase, nodeBase1, nodeBase2);
                }
            });
            diagramController = new DiagramController(m_xContext, m_xFrame, xMSF, xMCF, diagramModel, xDrawDoc, this);
            Module combine = Modules.combine(new CommonsModule(), new AddonModule(diagramModel, xMSF, xDrawDoc, diagramController), new CodeGeneratorModule());
            try {
                injector = Guice.createInjector(combine);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            nodeFactory = injector.getInstance(NodeFactory.class);
            linkFactory = injector.getInstance(LinkFactory.class);
            diagramController.setNodeFactory(nodeFactory);
            diagramController.setLinkFactory(linkFactory);
            codeGeneratorFactory = injector.getInstance(CodeGeneratorFactory.class);
            setupDocumentEventsHandler();
            diagramService = injector.getInstance(DiagramService.class);
            diagramWalker = injector.getInstance(DiagramWalker.class);
            diagramTypeRecognition = injector.getInstance(DiagramTypeRecognition.class);
        } catch (java.lang.Exception e) {
            e.printStackTrace();
            throw new java.lang.RuntimeException(e);
        }
        layout = injector.getInstance(Layout.class);
        if (Boolean.TRUE.equals(Global.loaded)) {
            System.out.println("WAS LOADED, NEED change doc");
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
                try {
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
                } catch (Throwable e) {
                    e.printStackTrace();
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
//        xDrawDoc = UnoRuntime.queryInterface(XComponent.class, m_xFrame.getController().getModel());
        diagramModel.init(nodeFactory, linkFactory);
    }

    public DiagramService getDiagramService() {
        return diagramService;
    }

    private void setupDocumentEventsHandler() {
        logger.info("setupDocumentEventsHandler");
        documentEventsHandler.registerHandler(ImmutableList.of("OnLoad", "OnLoadFinished"), new DocumentEventHandler() {
            @Override
            public void documentEventOccured(DocumentEvent documentEvent) {
//                onLoadHandler();
            }

            @Override
            public void documentEventOccured(String eventName) {
//                onLoadHandler();
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
////                OOGraph ooGraph = MyComponentFactory.mapMap.get(diagramModel);
//
//                XPropertyContainer userDefinedProperties = null;
//
//                String s = null;
//                try {
////                    diagramModel.refreshModel();
////                    s = StringSerializer.toString(diagramModel);
////
////                    XDocumentPropertiesSupplier xDocumentPropertiesSupplier = UnoRuntime.queryInterface(XDocumentPropertiesSupplier.class, xDrawDoc);
////                    XDocumentProperties documentProperties = xDocumentPropertiesSupplier.getDocumentProperties();
////                    userDefinedProperties = documentProperties.getUserDefinedProperties();
//////                    userDefinedProperties.addProperty("DiagramModel", PropertyAttribute.MAYBEVOID, new Any(Type.STRING, s));
////                    //                        documentProperties.storeToStorage();
////                } catch (IOException e) {
////                    e.printStackTrace();
////                } catch (IllegalArgumentException e) {
////                    e.printStackTrace();
////                } catch (IllegalTypeException e) {
////                    e.printStackTrace();
////                } catch (PropertyExistException e) {
////
////                    if (userDefinedProperties != null && s != null) {
////                        try {
////                            XPropertySet xPropertySet = QI.XPropertySet(userDefinedProperties);
////
////                            xPropertySet.setPropertyValue("DiagramModel", s);
////                        } catch (UnknownPropertyException e1) {
////                            e1.printStackTrace();
////                        } catch (PropertyVetoException e1) {
////                            e1.printStackTrace();
////                        } catch (IllegalArgumentException e1) {
////                            e1.printStackTrace();
////                        } catch (WrappedTargetException e1) {
////                            e1.printStackTrace();
////                        }
////                    }
////                }
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
                    NodeBase processNode = nodeFactory.create(Node.NodeType.StartMethodOfProcess);
                    node = processNode;
                }
                if (url.Path.compareTo(PROCEDURE_NODE) == 0) {
                    ProcedureNode procedureNode = (ProcedureNode) nodeFactory.create(Node.NodeType.MethodOfProcess);//createAndInsert(NodeBase.NodeType.StartMethodOfProcess, xDrawDoc, xShapes);
                    node = procedureNode;
                }
                if (url.Path.compareTo(CLIENT_NODE) == 0) {
                    ClientNode clientNode = (ClientNode) nodeFactory.create(Node.NodeType.ClientPort);
                    node = clientNode;
                }
                if (url.Path.compareTo(SERVER_NODE) == 0) {
                    ServerNode serverNode = (ServerNode) nodeFactory.create(Node.NodeType.ServerPort);
                    node = serverNode;
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
                            getDiagramController().setInputMode(new DiagramController.InputTwoNodesMode(getDiagramController(), link, diagramService));
                        }
//                        statusChangedDisable(url);
                        getDiagramModel().addDiagramElement(link);
                        diagramController.configureListeners(link);
                        layout.layout(new DiagramElementObj(link));
                    }
                    return;
                }
                if (anyNode(url)) {
                    if (node != null) {
//                        undoManager.enterUndoContext("Remove_" + node.getName());
                        DrawHelper.insertShapeOnCurrentPage(node.getShape(), getDiagramModel().getDrawDoc());
                        MiscHelper.setNodeType(node.getShape(), node.getType());
                        DrawHelper.setShapeSize(node.getShape(), 2000, 1500);
                        node.setProps();
                        MiscHelper.tagShapeAsNode(node.getShape());
                        if (Settings.getSettings().promptForNodeName()) {
                            CreateNodeDialog createNodeDialog = new CreateNodeDialog(node.getShape(), m_xContext);
                            XModel xModel = QI.XModel(xDrawDoc);
                            XDialog dialog = createDialog(CreateNodeDialog.CREATE_NODE_DIALOG_XDL, xModel, m_xFrame, createNodeDialog, false);
                            createNodeDialog.init(dialog);
                            dialog.execute();
                        }
                        // common for all nodes
                        final NodeBase finalNode = node;
                        getDiagramModel().addDiagramElement(node);
                        MiscHelper.setId(node.getShape(), node.getId());
                        MiscHelper.tagShapeAsNode(node.getShape());
                        layout.layout(new DiagramElementObj(node));
//                        undoManager.addUndoAction(new XUndoAction() {
//                            @Override
//                            public String getTitle() {
//                                return "Добавить узел " + finalNode.getName();
//                            }
//                            @Override
//                            public void undo() throws UndoFailedException {
//                                getDiagramModel().removeDiagramElement(finalNode);
//                                ShapeHelper.removeShape(finalNode.getShape(), DrawHelper.getCurrentDrawPage(getDiagramModel().getDrawDoc()));
//                            }
//                            @Override
//                            public void redo() throws UndoFailedException {
//                                getDiagramModel().addDiagramElement(finalNode);
//                                DrawHelper.insertShapeOnCurrentPage(finalNode.getShape(), getDiagramModel().getDrawDoc());
//                            }
//                        });
//                        undoManager.leaveUndoContext();
                    }
                    return;
                }
                if (url.Path.compareTo("GetCode") == 0) {
                    XModel xModel = QI.XModel(xDrawDoc);
                    try {
                        Object oClipboard =
                                xMCF.createInstanceWithContext(
                                        COM_SUN_STAR_DATATRANSFER_CLIPBOARD_SYSTEM_CLIPBOARD,
                                        m_xContext);
                        XShapes xShapes;
                        XDrawPage currentDrawPage = DrawHelper.getCurrentDrawPage(xDrawDoc);
                        xShapes = QI.XShapes(currentDrawPage);
                        Set<XShape> allShapes = Sets.newHashSet();
                        for (int i = 0; i < xShapes.getCount(); i++) {
                            allShapes.add(QI.XShape(xShapes.getByIndex(i)));
                        }
                        DiagramType diagramType = diagramTypeRecognition.recognise(allShapes);
                        diagramModel.setDiagramType(diagramType);
                        //TODO DI
                        diagramWalker.setDiagramType(diagramType);
                        Graph walk = diagramWalker.walk(allShapes);
                        CodeGenerator codeGenerator = codeGeneratorFactory.create(DrawHelper.getPageName(currentDrawPage), diagramType);
                        diagramModel.setGraph(walk);
                        Set<XShape> usedShapes = new HashSet<>();
                        for (Link link_ : walk.getLinks()) {
                            Node startNode = link_.getStartNode();
                            NodeBase nodeBaseStart = (NodeBase) startNode;
                            XShape shape = nodeBaseStart.getShape();
                            usedShapes.add(shape);
                            Node endNode = link_.getEndNode();
                            NodeBase nodeBaseEnd = (NodeBase) endNode;
                            shape = nodeBaseEnd.getShape();
                            usedShapes.add(shape);
                            if (link_ instanceof ShapesProvider) {
                                ShapesProvider shapesProvider = (ShapesProvider) link_;
                                if (shapesProvider != null) {
                                    usedShapes.addAll(Lists.newArrayList(shapesProvider.getShapes()));
                                }
                            }
                        }
                        if (Settings.getSettings().isValidationRequired()) {
                            Validator validator = new ValidatorImpl(diagramModel, allShapes, usedShapes);
                            ValidationResult validate = validator.validate(diagramModel);
                            if (validate.getItems().size() > 0) {
                                ValidationDialog validationDialog = new ValidationDialog();
                                XWindow dialog = createWindow(ValidationDialog.VALIDATION_DIALOG_XDL, xModel, m_xFrame, validationDialog.getDialogHandler(), false);
                                Object instance = xMCF.createInstanceWithContext("com.sun.star.awt.tree.MutableTreeDataModel", m_xContext);
                                XMutableTreeDataModel treeDataModel = QI.XMutableTreeDataModel(instance);
                                validationDialog.init(dialog, validate, treeDataModel, diagramService);
                                dialog.setVisible(true);
                                return;
                            }
                        }
                        ArrayList<ConnectedShapesComplex> connectedShapesComplexes = new ArrayList<>(diagramWalker.getConnectedShapesComplexes());
                        String code = codeGenerator.generateCode(new DiagramCodeSource(diagramModel, connectedShapesComplexes, diagramType));
                        DiagramCodeGenerator generator = (DiagramCodeGenerator) codeGenerator;
                        GetCodeDialog myDialog = new GetCodeDialog(code, oClipboard);
                        XDialog dialog = createDialog(GetCodeDialog.GET_CODE_DIALOG_XDL, xModel, m_xFrame, myDialog, false);
                        myDialog.init(dialog);
                        dialog.execute();
                    } catch (CantRecognizeType cantRecognizeType) {
                        diagramController.setSelectedShape(cantRecognizeType.getShape());
                        throw new java.lang.RuntimeException(cantRecognizeType);
                    } catch (Exception e) {
                        throw new java.lang.RuntimeException(e);
                    }
                }
                if (url.Path.compareTo("About") == 0) {
                    XDialog dialog = createDialog("vnd.sun.star.extension://ru.ssau.graphplus.oograph/dialogs/AboutDialog.xdl", QI.XModel(xDrawDoc), m_xFrame, false);
                    dialog.execute();
                }
                if (url.Path.compareTo("Colorize") == 0) {
                    class Colorizer implements Walker<Set<XShape>, Void> {

                        @Override
                        public Void walk(Set<XShape> toWalk) {
                            //# use golden ratio
                            double golden_ratio_conjugate = 0.618033988749895;
                            float h = (float) Math.random(); //# use random start value
                            Iterator<XShape> iterator = toWalk.iterator();
                            while (iterator.hasNext()) {
                                XShape next = iterator.next();
                                h += golden_ratio_conjugate;
                                h %= 1;
                                int color = hsv_to_rgb(h, 0.5f, 0.95f);
                                try {
                                    DrawHelper.setFillColor(next, color);
                                } catch (UnknownPropertyException | PropertyVetoException | IllegalArgumentException | WrappedTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                            return null;
                        }

                        private int hsv_to_rgb(float h, float s, float v) {
                            return Color.HSBtoRGB(h, s, v);
                        }
                    }
                    XShapes xShapes;// = QI.XShapes(xDrawDoc);
                    XDrawPage currentDrawPage = DrawHelper.getCurrentDrawPage(xDrawDoc);
                    xShapes = QI.XShapes(currentDrawPage);
                    Set<XShape> allShapes = Sets.newHashSet();
                    for (int i = 0; i < xShapes.getCount(); i++) {
                        allShapes.add(QI.XShape(xShapes.getByIndex(i)));
                    }
                    Colorizer colorizer = new Colorizer();
                    colorizer.walk(allShapes);
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
