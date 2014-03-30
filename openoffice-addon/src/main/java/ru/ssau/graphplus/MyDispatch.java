/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.sun.star.awt.*;
import com.sun.star.beans.*;
import com.sun.star.container.XNameContainer;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.document.DocumentEvent;
import com.sun.star.document.XDocumentProperties;
import com.sun.star.document.XDocumentPropertiesSupplier;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.drawing.XShapes;
import com.sun.star.frame.*;
import com.sun.star.lang.*;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.report.XImageControl;
import com.sun.star.task.XJobExecutor;
import com.sun.star.uno.*;
import com.sun.star.uno.Exception;
import com.sun.star.uno.RuntimeException;
import com.sun.star.util.URL;
import com.sun.star.view.XSelectionSupplier;
import ru.ssau.graphplus.analizer.DiagramWalker;
import ru.ssau.graphplus.api.DiagramElement;
import ru.ssau.graphplus.api.DiagramType;
import ru.ssau.graphplus.document.event.handler.DocumentEventHandler;
import ru.ssau.graphplus.document.event.handler.DocumentEventsHandler;
import ru.ssau.graphplus.document.event.handler.impl.DocumentEventsHandlerImpl;
import ru.ssau.graphplus.gui.GetCodeDialog;
import ru.ssau.graphplus.gui.Gui;
import ru.ssau.graphplus.gui.LinkNodesDialog;
import ru.ssau.graphplus.link.*;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.node.*;
import ru.ssau.graphplus.recognition.DiagramTypeRecognition;
import ru.ssau.graphplus.recognition.DiagramTypeRecognitionImpl;

import static ru.ssau.graphplus.Constants.*;

import java.io.*;
import java.lang.ClassNotFoundException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 2/8/14
 * Time: 1:04 AM
 * To change this template use File | Settings | File Templates.
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

    public NodeFactory getNodeFactory() {
        return nodeFactory;
    }

    public LinkFactory getLinkFactory() {
        return linkFactory;
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
        diagramController = new DiagramController(m_xContext, m_xFrame, xMSF, xMCF, diagramModel, xDrawDoc, this);

        setupDocumentEventsHandler();

        if (Boolean.TRUE.equals(Global.loaded)){
         onLoadHandler();
        }
    }

    private void onLoadHandler() {
        try {
            XComponent xComponent = this.m_xComponent;
            diagramModel.refreshModel();

            XDocumentPropertiesSupplier xDocumentPropertiesSupplier = UnoRuntime.queryInterface(XDocumentPropertiesSupplier.class, xComponent);
            XPropertyContainer userDefinedProperties = xDocumentPropertiesSupplier.getDocumentProperties().getUserDefinedProperties();//.rea;
            XPropertySet xPropertySet = QI.XPropertySet(userDefinedProperties);
            Object modelFromProperties = null;

            modelFromProperties = xPropertySet.getPropertyValue("DiagramModel");

            String diagramModelAsString = (String) modelFromProperties;

            Object o = null;
            try {
                o = StringSerializer.fromString(diagramModelAsString);
                DiagramModel modelFromString = (DiagramModel) o;
                boolean remap = modelFromString.remap(xComponent);
                Gui.showErrorMessageBox(null, "DiagramElements:", Joiner.on('\n').join(modelFromString.getDiagramElements().iterator()), xMCF, m_xContext);
                if (remap) {

                    // successfully remapped deserialized DiagramModel to document
                    setDiagramModel(modelFromString);
                    nodeFactory.setCount(getNodeCount(modelFromString));
                    linkFactory.setCount(getLinkCount(modelFromString));
                } else {

                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        }
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


            Object nodeObject;
            Object linkObject;

            LinkBase link = null;
            NodeBase node = null;


            if (url.Path.compareTo("Node") == 0) {

                try {
                    XDrawPage xDrawPage = DrawHelper.getDrawPageByIndex(getDiagramModel().getDrawDoc(), 0);
                    XShape xShape = ShapeHelper.createShape(m_xComponent, new Point(800, 600), new Size(1500, 1500), "com.sun.star.drawing.EllipseShape");// .createEllipseShape(m_xComponent, 800, 800, 1500, 1500);
                    node = nodeFactory.create(Node.NodeType.ClientPort, m_xComponent);
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


                    MiscHelper.tagShapeAsNode(xShape);
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


                    Object instanceWithContext = xMCF.createInstanceWithContext("com.sun.star.task.JobExecutor", m_xContext);
                    XJobExecutor jobExecutor = UnoRuntime.queryInterface(XJobExecutor.class, instanceWithContext);

                    jobExecutor.trigger("vnd.sun.star.job:service=ru.ssau.graphplus.OOGraph,alias=MonitorJob,event=BlaBla");

                    XDrawPage xPage = PageHelper.getDrawPageByIndex(getDiagramModel().getDrawDoc(), 0);
                    XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, xPage);


                    NodeBase processNode = nodeFactory.create(Node.NodeType.StartMethodOfProcess, m_xComponent);
                    node = processNode;


                    XPropertySet xPS = QI.XPropertySet(node.getShape());

                    XNameContainer xNC2 = QI.XNameContainer(xPS.getPropertyValue("UserDefinedAttributes"));


                    DrawHelper.insertShapeOnCurrentPage(node.getShape(), getDiagramModel().getDrawDoc());

                    MiscHelper.addUserDefinedAttributes(node.getShape(), xMSF);

                    MiscHelper.tagShapeAsNode(node.getShape());
                    MiscHelper.setNodeType(node.getShape(), Node.NodeType.StartMethodOfProcess);
                    DrawHelper.setShapePositionAndSize(node.getShape(), 100, 100, 1800, 1500);
                    Gui.createDialogForShape2(node.getShape(), m_xContext, new HashMap<String, XShape>());
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
                    ProcedureNode procedureNode = (ProcedureNode) nodeFactory.create(Node.NodeType.MethodOfProcess, m_xComponent);//createAndInsert(NodeBase.NodeType.StartMethodOfProcess, m_xComponent, xShapes);
                    node = procedureNode;

                    DrawHelper.insertShapeOnCurrentPage(procedureNode.getShape(), getDiagramModel().getDrawDoc());

                    MiscHelper.addUserDefinedAttributes(procedureNode.getShape(), xMSF);
                    MiscHelper.setNodeType(procedureNode.getShape(), Node.NodeType.MethodOfProcess);

                    MiscHelper.tagShapeAsNode(procedureNode.getShape());

                    DrawHelper.setShapePositionAndSize(procedureNode.getShape(), 100, 100, 2000, 1500);
                    Gui.createDialogForShape2(procedureNode.getShape(), m_xContext, new HashMap<String, XShape>());

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


                    ClientNode clientNode = (ClientNode) nodeFactory.create(Node.NodeType.ClientPort, m_xComponent);
                    node = clientNode;

                    DrawHelper.insertNodeOnCurrentPage(clientNode, getDiagramModel().getDrawDoc());

                    MiscHelper.addUserDefinedAttributes(clientNode.getShape(), xMSF);
                    MiscHelper.setNodeType(clientNode.getShape(), Node.NodeType.ClientPort);

                    MiscHelper.tagShapeAsNode(clientNode.getShape());

                    clientNode.setProps();
                    DrawHelper.setShapePositionAndSize(clientNode.getShape(), 100, 100, 1500, 1500);
                    Gui.createDialogForShape2(clientNode.getShape(), m_xContext, new HashMap<String, XShape>());

                    OOGraph.LOGGER.info("omg");
                    //return;
                } catch (java.lang.Exception ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }

            }

            if (url.Path.compareTo(SERVER_NODE) == 0) {
                try {
                    ServerNode serverNode = (ServerNode) nodeFactory.create(Node.NodeType.ServerPort, m_xComponent);
                    node = serverNode;

                    DrawHelper.insertShapeOnCurrentPage(serverNode.getShape(), getDiagramModel().getDrawDoc());

                    MiscHelper.addUserDefinedAttributes(serverNode.getShape(), xMSF);
                    MiscHelper.tagShapeAsNode(serverNode.getShape());
                    MiscHelper.setNodeType(serverNode.getShape(), Node.NodeType.ServerPort);


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

                LinkLink linkLink = (LinkLink) linkFactory.create(Link.LinkType.Link, getDiagramModel().getDrawDoc());
                link = linkLink;
                linkShapes = linkLink.getShapes();

            }

            if (url.Path.compareTo(MESSAGE_LINK) == 0) {

                MessageLink messageLink = (MessageLink) linkFactory.create(Link.LinkType.Message, getDiagramModel().getDrawDoc());
                link = messageLink;
                linkShapes = messageLink.getShapes();

            }

            if (url.Path.compareTo(CONTROL_LINK) == 0) {

                ControlLink controlLink = (ControlLink) linkFactory.create(Link.LinkType.Control, getDiagramModel().getDrawDoc());
                link = controlLink;
                linkShapes = controlLink.getShapes();

            }


            if (url.Path.compareTo(LINK_LINK) == 0 || url.Path.compareTo(CONTROL_LINK) == 0 || url.Path.compareTo(MESSAGE_LINK) == 0) {

                for (XShape shape : linkShapes) {
                    DrawHelper.insertShapeOnCurrentPage(shape, getDiagramModel().getDrawDoc());

//                    linkFactory.setId(shape, link);
                }

                link.setProps();

                // common for all links


                getDiagramController().setLinker(link);
                getDiagramController().setInputMode(new InputTwoShapesMode(getDiagramController(), link));

                //inputMode = new InputTwoShapesMode(getDiagramController());
                statusChangedDisable(url);

                //                    lastURL = url;

                if (link != null) {

                    getDiagramModel().addDiagramElement(link);
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
                OOGraph.LOGGER.info("Tag");
                if (Status.isTagAllNewShapes()) {
                    Status.setTagAllNewShapes(false);
                } else {
                    Status.setTagAllNewShapes(true);
                }
                return;
            }

            if (url.Path.compareTo("Assoc") == 0) {
                // add your own code here
                OOGraph.LOGGER.info("Assoc");

                return;
            }


            if (url.Path.compareTo("TagAsLink") == 0) {

                try {

                    XDrawPage xPage = PageHelper.getDrawPageByIndex(getDiagramModel().getDrawDoc(), 0);

                    XController xController = m_xFrame.getController();

                    //                            Object ddv = xMCF.createInstanceWithContext("com.sun.star.drawing.DrawingDocumentDrawView", m_xContext);
                    //XSelectionSupplier
                    XSelectionSupplier xSelectSup = QI.XSelectionSupplier(xController);
                    Object selectionObj = xSelectSup.getSelection();
                    XShapes xShapes = (XShapes) UnoRuntime.queryInterface(
                            XShapes.class, selectionObj);
                    try {
                        final XShape xShape = (XShape) QI.XShape(xShapes.getByIndex(0));
                        OOGraph.LOGGER.info(xShape.getShapeType());
                        //                        if (xShape.getShapeType().contains("Connector")) {
                        //                            Misc.tagShapeAsLink(xShape);
                        //                            chooseLinkType(xShape);
                        //
                        //                        }

                        String packageLocation = getPackageLocation();
                        OOGraph.LOGGER.info(packageLocation);
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

                                    XControlContainer xControlContainer = UnoRuntime.queryInterface(XControlContainer.class, xDialog);
                                    //                                    xControlContainer.getControl("")
                                    boolean handled = true;
                                    boolean end = false;


                                    if (s.equals("chooseType")) {

                                        MiscHelper.tagShapeAsNode(xShape);

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


                                        }


                                        end = true;
                                        handled = true;

                                    } else if (s.equals("itemStatusChanged")) {
                                        selected = ((ItemEvent) o).Selected;

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

                } catch (com.sun.star.lang.IndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (WrappedTargetException e) {
                    e.printStackTrace();
                }
            }


            if (url.Path.compareTo("TagAsNode") == 0) {
                try {

                    XDrawPage xPage = PageHelper.getDrawPageByIndex(getDiagramModel().getDrawDoc(), 0);
                    XController xController = m_xFrame.getController();

                    XSelectionSupplier xSelectSup = QI.XSelectionSupplier(xController);
                    Object selectionObj = xSelectSup.getSelection();
                    XShapes xShapes = (XShapes) UnoRuntime.queryInterface(
                            XShapes.class, selectionObj);
                    try {
                        final XShape xShape = (XShape) QI.XShape(xShapes.getByIndex(0));
                        OOGraph.LOGGER.info(xShape.getShapeType());


                        Object valueByName = m_xContext.getValueByName("/singletons/com.sun.star.deployment.PackageInformationProvider");
                        XPackageInformationProvider xPackageInformationProvider = UnoRuntime.queryInterface(XPackageInformationProvider.class, valueByName);
                        String packageLocation = xPackageInformationProvider.getPackageLocation("ru.ssau.graphplus.oograph");
                        OOGraph.LOGGER.info(packageLocation);
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

                                        MiscHelper.tagShapeAsNode(xShape);

                                        XControl comboBox1 = xControlContainer.getControl("ComboBox1");
                                        XComboBox xComboBox = UnoRuntime.queryInterface(XComboBox.class, comboBox1);

                                        String nodeType = xComboBox.getItem(selected.shortValue());
                                        NodeBase nodeReplace = null;
                                        final boolean finalConvertShape = convertShape;
                                        final NodeBase finalNodeReplace = nodeReplace;
                                        if (convertShape) {
                                            final NodeBase.NodeType type = Node.NodeType.valueOf(nodeType);
                                            nodeReplace = nodeFactory.create(type, m_xComponent);

                                            PostCreationAction postCreationAction = new NodeBase.DefaultPostCreationAction(convertShape) {
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
                                        } catch (com.sun.star.lang.IllegalArgumentException e) {
                                            e.printStackTrace();
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
                            Logger.getAnonymousLogger().warning(e.getMessage());
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


            if (anyNode(url)) {
                // common for all nodes
                if (node != null) {

                    getDiagramModel().addDiagramElement(node);
                    MiscHelper.setId(node.getShape(), node.getName());
                    //                        diagramModel.addDiagramElement(node);
                }

                return;
            }

            if (url.Path.compareTo("LinkNodesToolbar") == 0) {
                XModel xModel = QI.XModel(xDrawDoc);
                String dialog = createLinkNodesDialog("vnd.sun.star.extension://ru.ssau.graphplus.oograph/dialogs/LinkNodesDialog.xdl", xModel, m_xFrame);

            }

            if (url.Path.compareTo("GetCode") == 0){
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

                    for (int i = 0; i < xShapes.getCount(); i++){
                        shapes.add(QI.XShape(xShapes.getByIndex(i)));
                    }


                    DiagramTypeRecognition diagramTypeRecognition = new DiagramTypeRecognitionImpl();
                    DiagramType recognise = diagramTypeRecognition.recognise(shapes);

                    //TODO DI
                    DiagramWalker diagramWalker = new DiagramWalker(new ShapeHelperWrapperImpl(), new UnoRuntimeWrapperImpl());
                    diagramWalker.setDiagramType(recognise);
                    ru.ssau.graphplus.api.DiagramModel walk = diagramWalker.walk(shapes, null);

                    CodeGenerator codeGenerator = new DiagramCodeGenerator();

                    String code = codeGenerator.generateCode(new DiagramCodeSource(walk));


                    GetCodeDialog myDialog = new GetCodeDialog(code, oClipboard);

                    XDialog dialog = createDialog(GetCodeDialog.GET_CODE_DIALOG_XDL, xModel, m_xFrame, myDialog, false);
                    myDialog.init(dialog);
                    dialog.execute();

                } catch (Exception e) {
                    throw new RuntimeException("",e);
                }



            }

            if (url.Path.compareTo("About")==0){
                XModel xModel = QI.XModel(xDrawDoc);
                XDialog dialog = createDialog("vnd.sun.star.extension://ru.ssau.graphplus.oograph/dialogs/AboutDialog.xdl", xModel, m_xFrame);

            }


            // TODO add

//            if (url.Complete.endsWith(msShowCommand))
//                PanelOptionDialog.Show();




        }

    }

    final static String msShowCommand = "ShowOptionsDialog";


    private boolean anyNode(URL url) {
        return url.Path.equals(CLIENT_NODE) || url.Path.equals(PROCEDURE_NODE) || url.Path.equals(CLIENT_NODE) || url.Path.equals(SERVER_NODE);
    }

    public XDialog createDialog(String xdlUrl, XModel xModel, XFrame xFrame){
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


    public String createLinkNodesDialog(String DialogURL, XModel xModel, XFrame xFrame) {

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

//            "aNodeReset", "zNodeReset", "aNodeSet", "zNodeSet"
            LinkNodesDialog linkNodesDialog = new LinkNodesDialog(getDiagramModel(), null);

            XDialog xDialog = xDialogProvider.createDialogWithHandler(DialogURL,
                   linkNodesDialog.getDialogHandler());
            XControl xControl = (XControl) UnoRuntime.queryInterface(
                    XControl.class, xDialog);



            diagramController.addNodeSelectionListener(linkNodesDialog.getNodeSelectionListener());






            if (xDialog != null){
                QI.XPropertySet(xControl.getModel()).setPropertyValue("Visible", Boolean.TRUE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Created dialog \"" + DialogURL + "\"";
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
