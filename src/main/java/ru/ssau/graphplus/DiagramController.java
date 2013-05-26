/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus;

/**
 *
 * @author 1
 */

import com.sun.star.awt.*;
import com.sun.star.beans.*;
import com.sun.star.container.XIndexContainer;
import com.sun.star.container.XNameContainer;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.drawing.*;
import com.sun.star.frame.*;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.Locale;
import com.sun.star.lang.*;
import com.sun.star.report.XImageControl;
import com.sun.star.task.XJobExecutor;
import com.sun.star.task.XStatusIndicator;
import com.sun.star.text.XText;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.URL;
import com.sun.star.util.XModifyListener;
import com.sun.star.view.XSelectionChangeListener;
import com.sun.star.view.XSelectionSupplier;
import ru.ssau.graphplus.events.*;
import ru.ssau.graphplus.gui.Gui;
import ru.ssau.graphplus.gui.UnoAwtUtils;
import ru.ssau.graphplus.link.Link;
import ru.ssau.graphplus.link.LinkFactory;
import ru.ssau.graphplus.link.Linker;
import ru.ssau.graphplus.link.LinkerImpl;
import ru.ssau.graphplus.node.Node;
import ru.ssau.graphplus.node.NodeFactory;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DiagramController implements XModifyListener, XDispatch, XSelectionChangeListener {

    public static final String TEXT_FIELD_1 = "TextField1";
    public static final String LINK_LINK = "LinkLink";
    public static final String MESSAGE_LINK = "MessageLink";
    public static final String CONTROL_LINK = "ControlLink";
    public static final String SERVER_NODE = "ServerNode";
    public static final String CLIENT_NODE = "ClientNode";
    public static final String PROCESS_NODE = "ProcessNode";
    public static final String PROCEDURE_NODE = "ProcedureNode";
    private static ConnectorShapeListener connectorShapeListener;
    private static URL lastURL;
    private final XComponent m_xComponent;
    private final XComponent xDrawDoc;
    private static final InputMode DEFAULT_INPUT_MODE = new InputMode() {
        @Override
        public void onInput(EventObject eventObject, DiagramController diagramController) {

        }
    };
    Map<MyURL, Set<XStatusListener>> statusListeners = new HashMap<>();
    Set<ShapeInsertedEvent> shapeInsertedEvents = new HashSet<ShapeInsertedEvent>();
    int connectorsAdded = 0;
    boolean isTextShapeAdded = false;
    State state;
    Map<State, DiagramEventHandler> diagramEventHandlerMap = new HashMap<State, DiagramEventHandler>();
    DiagramEventHandler diagramEventHandler;
    XDrawPage xDP = null;
    int count = 0;
    XShape firstShape;
    XShape secondShape;
    Linker linker;
    InputMode inputMode;
    private DiagramModel diagramModel;
    private NodeFactory nodeFactory;
    private LinkFactory linkFactory;
    private List<ShapeRemovedEvent> removedEvents = new ArrayList<>();
    private XStatusIndicator statusIndicator;
    private List<String> urls = Arrays.asList(LINK_LINK, MESSAGE_LINK, CONTROL_LINK, SERVER_NODE, CLIENT_NODE, PROCESS_NODE, PROCEDURE_NODE);
    private XComponentContext m_xContext = null;
    private XFrame m_xFrame = null;
    private XController m_xController = null;
    private XSelectionSupplier m_xSelectionSupplier = null;
    private Map<String, XShape> elements = null;
    private XMultiServiceFactory xMSF = null;
    private XMultiComponentFactory xMCF = null;
    private Map<XShape, DiagramElement> whichElementContainsShape = null;
    private List<String> historyOfActions = new ArrayList<String>();
    private Map<Object, Point> positions = null;

    public XSelectionSupplier getXSelectionSupplier() {
        return m_xSelectionSupplier;
    }

    public static URL getLastURL() {
        return lastURL;
    }

    boolean flag;

    XShape toRemove1;
    XShape toRemove2;

    public DiagramController(XComponentContext xContext, XFrame xFrame, XMultiServiceFactory xMSF_, XMultiComponentFactory xMCF_, final DiagramModel diagramModel, XComponent xDoc) {
        Logger.getAnonymousLogger().info("DiagramController ctor");
        inputMode = new InputMode() {
            @Override
            public void onInput(EventObject eventObject, DiagramController diagramController) {
                // nothing
            }
        };
        xDrawDoc = xDoc;
        this.diagramModel = diagramModel;
        this.m_xComponent = xDoc;
        m_xContext = xContext;
        m_xFrame = xFrame;
        m_xController = m_xFrame.getController();
        xMCF = xMCF_;
        xMSF = xMSF_;
        xDP = DrawHelper.getCurrentDrawPage(xDrawDoc);
        nodeFactory = new NodeFactory(xMSF_);
        linkFactory = new LinkFactory(xMSF_);
        elements = new HashMap<>();
        whichElementContainsShape = new HashMap<>();
        positions = new HashMap<>();
        addSelectionListener();

        Logger.getLogger("DiagramController").info("adding shape event listeners");



        connectorShapeListener = new ConnectorShapeListener(xContext, xDoc, diagramModel.getConnectedShapes(), this);
        connectorShapeListener.addShapeEventListener(new ConnectedShapesChangeListener() {
            @Override
            void onConnectedShapesChange(ConnectedShapesChanged connectedShapesChanged) {
                ConnectedShapes connectedrShapes = connectedShapesChanged.getConnectedrShapes();
                XWindowPeer xWindowPeer = UnoRuntime.queryInterface(XWindowPeer.class, m_xFrame.getContainerWindow());
                if (xWindowPeer == null) {
                    xWindowPeer = UnoRuntime.queryInterface(XWindowPeer.class, m_xFrame.getContainerWindow());
                }
                System.out.println("DiagramController" + " onConnectedShapesChange");
                short i = UnoAwtUtils.showYesNoWarningMessageBox(xWindowPeer, "Are you sure", "Want to change connected shape?");
                if (i == 3) {
                    //ConnectorShapeListener.DocumentListener.ConnectedShapes connectedrShapes1 = connectedShapesChanged.getConnectedrShapes();
                    XPropertySet connector = connectedrShapes.getConnector();
                    try {
                        connector.setPropertyValue("StartShape", connectedrShapes.getStart());
                        connector.setPropertyValue("EndShape", connectedrShapes.getEnd());
                    } catch (UnknownPropertyException e) {
                        e.printStackTrace();
                    } catch (PropertyVetoException e) {
                        e.printStackTrace();
                    } catch (WrappedTargetException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                } else {
                    connectedShapesChanged.getConnectedrShapes().update();
                }
            }
        });

//        connectorShapeListener.addShapeEventListener(new ShapeInsertedListener() {
//
//            @Override
//            public void onShapeInserted(ShapeInsertedEvent shapeInsertedEvent) {
//
//                if (state.equals(State.AddingLink)) {
//                    if (shapeInsertedEvents.size() == 2) {
//
//                        boolean matched = true;
//                        setState(State.Nothing);
//
//                        for (ShapeInsertedEvent event : shapeInsertedEvents) {
//                            if (Misc.isTextShape(event.xShape)) {
//
//                                if (isTextShapeAdded == true) {
//                                    matched = false;
//                                    break;
//                                } else {
//                                    isTextShapeAdded = true;
//                                }
//
//
//                            } else if (Misc.isConnectorShape(event.xShape)) {
//                                connectorsAdded++;
//                            }
//                        }
//                        matched = matched & (connectorsAdded == 2);
//
//                        if (matched) {
//                            ElementAddEvent elementAddEvent = new ElementAddEvent();
//                            fireDiagramEvent(elementAddEvent);
//                        } else {
//                            shapeInsertedEvents.clear();
//                            connectorsAdded = 0;
//                            isTextShapeAdded = false;
//                        }
//                        // todo
//
//
//                    } else {
//                        shapeInsertedEvents.add(new ShapeInsertedEvent(shapeInsertedEvent, new Date()));
//                    }
//                }
//
//
//                if (xDP.getCount() > count) {
//                    count++;
//                    System.out.println("added new shape");
//
//                    Object obj = null;
//                    try {
//                        obj = xDP.getByIndex(xDP.getCount() - 1);
//                    } catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
//                        Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
//                    } catch (WrappedTargetException ex) {
//                        Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                    XShape xShape = (XShape) UnoRuntime.queryInterface(XShape.class, obj);
//                    positions.put(obj, xShape.getPosition());
//
//                    if (Misc.isNode(xShape) || Misc.isLink(xShape)) {
//
//                    } else {
//                        Misc.addUserDefinedAttributes(xShape, xMSF);
//                    }
//
//                }
//            }
//        });


        ShapeRemovedListener shapeEventListener = new ShapeRemovedListener() {
            @Override
            public void onShapeRemoved(ShapeRemovedEvent shapeRemovedEvent1) {

                XShape shape = shapeRemovedEvent1.getShape();
                DiagramElement diagramELementByShape = getDiagramModel().getDiagramELementByShape(shape);


//                connectorShapeListener.setMuteModify(true);
                connectorShapeListener.setMuteRemove(true);

                if (diagramELementByShape instanceof Link) {
                    Link link = (Link) diagramELementByShape;
                    if (!Misc.isTextShape(shape)) {
                        xDP.remove(link.getConnShape1());
                        xDP.remove(link.getConnShape2());
                        xDP.remove(link.getTextShape());

                    } else {
                        toRemove1 = link.getConnShape1();
                        toRemove2 = link.getConnShape2();
                        flag = true;

                        try {
                            Object instanceWithContext = xMCF.createInstanceWithContext("com.sun.star.task.JobExecutor", m_xContext);
                            XJobExecutor jobExecutor = UnoRuntime.queryInterface(XJobExecutor.class, instanceWithContext);
//                            XJob
                            jobExecutor.trigger("vnd.sun.star.job:service=ru.ssau.graphplus.OOGraph,alias=MonitorJob,event=OnNew");
                        } catch (Exception e) {

                            e.printStackTrace();
                        }

                    }


                } else {

                }
                getDiagramModel().removeDiagramElement(diagramELementByShape);
                connectorShapeListener.setMuteRemove(false);
//                connectorShapeListener.setMuteModify(false);

            }
        };
        connectorShapeListener.addShapeEventListener(shapeEventListener);

        connectorShapeListener.addShapeEventListener(new ShapeModifiedListener(){
            @Override
            public void onShapeModified(ShapeModifiedEvent shapeEvent) {
                System.out.println(shapeEvent.getShape());
            }
        });


    }

    public DiagramModel getDiagramModel() {
        return diagramModel;
    }

    @Override
    public void dispatch(URL url, PropertyValue[] propertyValues) {
        if (url.Protocol.compareTo("ru.ssau.graphplus:") == 0) {

            if (url.Complete.equals("ru.ssau.graphplus:DropdownCmd")) {

                System.out.println(url.Complete);
                Logger.getGlobal().log(Level.INFO, "PropertyValue[]", propertyValues);
                Object value = propertyValues[1].Value;
                String string = (String) propertyValues[1].Value;
                return;

            }


            Object nodeObject;
            Object linkObject;

            Link link = null;
            Node node = null;


            if (url.Path.compareTo("Omg") == 0) {


                statusIndicator.setText("OMG");

                XDispatchProvider xDispatchProvider = QI.XDispatchProvider(m_xFrame);

                XShapes xShapes = QI.XShapes(xDP);
                for (int i = 0; i < xShapes.getCount(); i++) {

                    Object byIndex = null;
                    try {
                        byIndex = xShapes.getByIndex(i);
                        //OOGraph.printInfo(byIndex);
                        XGluePointsSupplier xGluePointsSupplier = UnoRuntime.queryInterface(XGluePointsSupplier.class, byIndex);
                        XIndexContainer gluePoints = xGluePointsSupplier.getGluePoints();
                        for (int j = 0; j < gluePoints.getCount(); j++) {
                            Object byIndex1 = gluePoints.getByIndex(j);
                            GluePoint2 gluePoint2 = (GluePoint2) byIndex1;
                            System.out.println(gluePoint2.Escape);
                            System.out.println(gluePoint2.IsRelative);
                            System.out.println(gluePoint2.IsUserDefined);
                            System.out.println(gluePoint2.Position);
                            System.out.println(gluePoint2.Position.X);
                            System.out.println(gluePoint2.Position.Y);
                            System.out.println(gluePoint2.PositionAlignment);
                            System.out.println(gluePoint2.PositionAlignment.toString());
                        }

                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    } catch (WrappedTargetException e) {
                        e.printStackTrace();
                    }


                }


//                UnoRuntime.queryInterface(XUserInputInterception.class,m_xFrame.getController()).addMouseClickHandler(new XMouseClickHandler() {
//
//                    @Override
//                    public boolean mousePressed(MouseEvent mouseEvent) {
//                        System.out.println("mousePressed");
//                        return false;
//                    }
//
//                    @Override
//                    public boolean mouseReleased(MouseEvent mouseEvent) {
//                        System.out.println("mouseReleased");
//                        return false;
//                    }
//
//                    @Override
//                    public void disposing(EventObject eventObject) {
//                        //To change body of implemented methods use File | Settings | File Templates.
//                    }
//                });


                Object createInstance = null;
                try {


                    XMultiServiceFactory xMultiServiceFactory = QI.XMultiServiceFactory(m_xContext.getServiceManager());
                    createInstance = xMultiServiceFactory.createInstance("com.sun.star.frame.DispatchHelper");
                    XDispatchHelper dispatchHelper = QI.XDispatchHelper(createInstance);
                    XDispatchProvider xDispatchProvider1 = QI.XDispatchProvider(m_xFrame);

                    dispatchHelper.executeDispatch(xDispatchProvider1, ".uno:Shape", "", 0, new PropertyValue[]{});

                } catch (com.sun.star.uno.Exception e) {
                    e.printStackTrace();
                }

            }


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


                    Node processNode = nodeFactory.create(Node.NodeType.Process, m_xComponent);
                    node = processNode;


                    XPropertySet xPS = QI.XPropertySet(node.getShape());

                    XNameContainer xNC2 = QI.XNameContainer(xPS.getPropertyValue("UserDefinedAttributes"));


                    DrawHelper.insertShapeOnCurrentPage(node.getShape(), xDrawDoc);

//                    Misc.addUserDefinedAttributes(node.getShape(), xMSF);

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
                    XShapes xShapes = UnoRuntime.queryInterface(XShapes.class, xPage);
                    Node procedureNode = nodeFactory.create(Node.NodeType.Procedure, m_xComponent);//createAndInsert(Node.NodeType.Process, m_xComponent, xShapes);
                    node = procedureNode;


                    DrawHelper.insertShapeOnCurrentPage(procedureNode.getShape(), xDrawDoc);

//                    Misc.addUserDefinedAttributes(procedureNode.getShape(), xMSF);
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
                    Node serverNode = nodeFactory.create(Node.NodeType.Server, m_xComponent);
                    node = serverNode;

                    DrawHelper.insertShapeOnCurrentPage(serverNode.getShape(), xDrawDoc);

//                    Misc.addUserDefinedAttributes(serverNode.getShape(), xMSF);
                    Misc.tagShapeAsNode(serverNode.getShape());
                    Misc.setNodeType(serverNode.getShape(), Node.NodeType.Server);


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

                Link linkLink = linkFactory.create(Link.LinkType.Link, xDrawDoc, DrawHelper.getCurrentDrawPage(xDrawDoc));
                link = linkLink;
                linkShapes = linkLink.getShapes();


            }

            if (url.Path.compareTo(MESSAGE_LINK) == 0) {

                Link messageLink = linkFactory.create(Link.LinkType.Message, xDrawDoc, DrawHelper.getCurrentDrawPage(xDrawDoc));
                link = messageLink;
                linkShapes = messageLink.getShapes();

            }

            if (url.Path.compareTo(CONTROL_LINK) == 0) {

                Link controlLink = linkFactory.create(Link.LinkType.Control, xDrawDoc, DrawHelper.getCurrentDrawPage(xDrawDoc));
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

                inputMode = new InputTwoShapesMode(this);
                setLinker(link);

                statusChangedDisable(url);

                lastURL = url;

                if (link != null) {
                    diagramModel.addDiagramElement(link);
                    configureListeners(link);
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
                                            linkReplace = linkFactory.create(Link.LinkType.valueOf(nodeType), m_xComponent, xDP);

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
                                        Node nodeReplace = null;
                                        final boolean finalConvertShape = convertShape;
                                        final Node finalNodeReplace = nodeReplace;
                                        if (convertShape) {
                                            final Node.NodeType type = Node.NodeType.valueOf(nodeType);
                                            nodeReplace = nodeFactory.create(type, m_xComponent);

                                            Node.PostCreationAction postCreationAction = new Node.DefaultPostCreationAction(convertShape) {
                                                @Override
                                                public void postCreate(XShape shape) {
                                                    super.postCreate(shape);    //To change body of overridden methods use File | Settings | File Templates.

                                                }
                                            };

                                            if (finalConvertShape) {
                                                ShapeHelper.insertShape(nodeReplace.getShape(), xDP, postCreationAction);
                                                try {
                                                    nodeReplace.getShape().setPosition(xShape.getPosition());
                                                    nodeReplace.getShape().setSize(xShape.getSize());
                                                    DiagramElement diagramElement = diagramModel.getShapeToDiagramElementMap().get(xShape);


                                                    xDP.remove(xShape);
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

            if (url.Path.equals("EditLink")) {


                final XShape xShape = getSelectedShape();
                DiagramElement diagramElement = diagramModel.getShapeToDiagramElementMap().get(xShape);
                if (!(diagramElement instanceof Link)) {
                    return;
                } else {
                    final Link l = (Link) diagramElement;

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


                        XDialog xDialog = xDialogProvider.createDialogWithHandler("vnd.sun.star.extension://ru.ssau.graphplus.oograph/dialogs/EditLinkDialog.xdl", new XDialogEventHandler() {


                            @Override
                            public boolean callHandlerMethod(XDialog xDialog, Object o, String s) throws WrappedTargetException {


                                XControlContainer xControlContainer = UnoRuntime.queryInterface(XControlContainer.class, xDialog);

                                boolean handled = true;
                                boolean end = false;


                                if (s.equals("onSave")) {

                                    XControl control = xControlContainer.getControl(TEXT_FIELD_1);
                                    QI.XText(l.getTextShape()).setString(
                                            UnoRuntime.queryInterface(XTextComponent.class, control).getText());
                                    handled = true;
                                    end = true;
                                } else if (s.equals("onCancel")) {
                                    handled = true;
                                    end = true;

                                } else if (s.equals("textModified")) {
                                    handled = true;
                                    end = false;
                                }

                                if (end) {
                                    xDialog.endExecute();
                                }

                                return handled;
                            }

                            @Override
                            public String[] getSupportedMethodNames() {
                                return new String[]{"onSave", "onCancel", "textModified"};
                            }
                        });

                        XControlContainer xControlContainer = UnoRuntime.queryInterface(XControlContainer.class, xDialog);
                        XControl control = xControlContainer.getControl(TEXT_FIELD_1);
                        XTextComponent xTextComponent = QI.XTextComponent(control);
                        XText xText = QI.XText(l.getTextShape());
                        String string = xText.getString();
                        xTextComponent.setText(string);


                        xDialog.execute();
//                            if (xDialog != null)
//                                xDialog.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (java.lang.RuntimeException e) {
                        Logger.getGlobal().warning(e.getMessage());
                    }

                }

                return;
            }


            if (url.Path.contains("Node")) {
                // common for all links
                if (node != null)
                    diagramModel.addDiagramElement(node);

                return;
            }


        }

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
                    new NamedValue("List", new String[]{"Processes", "Composition", "Protocol"})
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

    private String getPackageLocation() {
        Object valueByName = m_xContext.getValueByName("/singletons/com.sun.star.deployment.PackageInformationProvider");
        XPackageInformationProvider xPackageInformationProvider = UnoRuntime.queryInterface(XPackageInformationProvider.class, valueByName);
        return xPackageInformationProvider.getPackageLocation("ru.ssau.graphplus.oograph");
    }

    public void configureListeners(Link link) {
        XPropertySet xPropertySet = QI.XPropertySet(link.getConnShape1());
//        try {
//
//        } catch (UnknownPropertyException e) {
//            e.printStackTrace();
//        } catch (WrappedTargetException e) {
//            e.printStackTrace();
//        }

    }

    void statusChanged(URL url, FeatureStateEvent featureStateEvent) {
        MyURL myURL = new MyURL(url);
        Set<XStatusListener> xStatusListeners = statusListeners.get(myURL);
        for (XStatusListener xStatusListener : xStatusListeners) {
            xStatusListener.statusChanged(featureStateEvent);
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

    void statusChangedEnable(URL url) {

        FeatureStateEvent featureStateEvent = new FeatureStateEvent();
        featureStateEvent.Source = this;
        featureStateEvent.IsEnabled = true;
        featureStateEvent.FeatureDescriptor = "QWE";
        featureStateEvent.FeatureURL = url;

        statusChanged(url, featureStateEvent);
    }

    public void onShapeInserted(com.sun.star.document.EventObject arg0) {
        System.out.println("ShapeInserted");


    }

    private void fireDiagramEvent(DiagramEvent diagramEvent) {
        if (diagramEvent instanceof ElementAddEvent) {
            diagramEventHandler.elementAdded((ElementAddEvent) diagramEvent);
        }

    }

    public void onShapeModified(com.sun.star.document.EventObject arg0) {
        System.out.println("ShapeModified");
        XShape xShape = QI.XShape(arg0.Source);
        if (isStartShapeChanged(xShape)) {
            System.out.println("StartShapeChanged");
        }
        if (isEndShapeChanged(xShape)) {
            System.out.println("EndShapeChanged");
        }

    }

    boolean isStartEndShapeChanged(XShape xShape, DiagramModel.StartEnd startEnd) {
        try {


            if (diagramModel.getConnShapeToShapeLink(xShape, startEnd).equals(QI.XShape(OOoUtils.getProperty(xShape, startEnd.toString()))))
                return true;
            return false;


        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    boolean isStartShapeChanged(XShape xShape) {
        return isStartEndShapeChanged(xShape, DiagramModel.StartEnd.StartShape);

    }

    boolean isEndShapeChanged(XShape xShape) {
        return isStartEndShapeChanged(xShape, DiagramModel.StartEnd.EndShape);
    }

    public void setStatusIndicator(XStatusIndicator statusIndicator) {
        this.statusIndicator = statusIndicator;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;


        if (diagramEventHandlerMap.containsKey(state)) {
            diagramEventHandler = diagramEventHandlerMap.get(state);
        } else {
            switch (state) {
                case AddingLink:
                    AddingLink addingLink = new AddingLink();
                    diagramEventHandlerMap.put(state, addingLink);
                    break;
                default:
                    diagramEventHandlerMap.put(state, new DiagramEventHandler() {
                        @Override
                        public void elementAdded(ElementAddEvent event) {
                            System.out.println("elementAdded");
                            // ignore
                        }

                        @Override
                        public void elementModified(ElementModifyEvent event) {
                            System.out.println("elementModified");
                            // ignore
                        }
                    });
                    break;
            }
        }
    }

    public List<String> getHistoryOfActions() {
        return historyOfActions;
    }

    public Map<String, XShape> getElements() {
        return elements;
    }

    private void addSelectionListener() {
        if (m_xSelectionSupplier == null) {
            m_xSelectionSupplier = (XSelectionSupplier) UnoRuntime.queryInterface(XSelectionSupplier.class, m_xController);
        }
        if (m_xSelectionSupplier != null) {
            m_xSelectionSupplier.addSelectionChangeListener(this);
        }
    }

    private void removeSelectionListener() {
        if (m_xSelectionSupplier != null) {
            m_xSelectionSupplier.removeSelectionChangeListener(this);
        }
    }

    public XDrawPage getCurrentPage() {
        XDrawView xDrawView = (XDrawView) UnoRuntime.queryInterface(XDrawView.class, m_xController);
        return xDrawView.getCurrentPage();
    }

    public Locale getLocation() {
        Locale locale = null;
        try {
            XMultiComponentFactory xMCF = m_xContext.getServiceManager();
            Object oConfigurationProvider = xMCF.createInstanceWithContext("com.sun.star.configuration.ConfigurationProvider", m_xContext);
            XLocalizable xLocalizable = (XLocalizable) UnoRuntime.queryInterface(XLocalizable.class, oConfigurationProvider);
            locale = xLocalizable.getLocale();
        } catch (Exception ex) {
            System.err.println(ex.getLocalizedMessage());
        }
        return locale;
    }

    public String getNumberStrOfShape(String name) {
        String s = "";
        char[] charName = name.toCharArray();
        int i = 0;
        while (i < name.length() && charName[i] != '-') {
            i++;
        }
        while (i < name.length() && (charName[i] < 48 || charName[i] > 57)) {
            i++;
        }
        while (i < name.length()) {
            s += charName[i++];
        }
        return s;
    }

    // XSelectionChangeListener

    public int getNumberOfShape(String name) {
        return parseInt(getNumberStrOfShape(name));
    }

    public int parseInt(String s) {
        int n = -1;
        try {
            n = Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            System.err.println(ex.getLocalizedMessage());
        }
        return n;
    }

    public void disposing(EventObject arg0) {
    }

    public Linker getLinker() {
        return linker;
    }

    public void setLinker(Linker linker) {
        this.linker = linker;
    }

    public boolean isOnlySimpleItemIsSelected() {
        if (getSelectedShapes().getCount() == 1) {
//            XNamed xNamed = (XNamed) UnoRuntime.queryInterface(XNamed.class, getSelectedShape());
//            String selectedShapeName = xNamed.getName();
//            if ((selectedShapeName.startsWith("OrganizationDiagram") || selectedShapeName.startsWith("SimpleOrganizationDiagram") || selectedShapeName.startsWith("HorizontalOrganizationDiagram") || selectedShapeName.startsWith("TableHierarchyDiagram")) && selectedShapeName.contains("RectangleShape") && !selectedShapeName.endsWith("RectangleShape0")) {
            return true;
//            }
        }
        return false;
    }

    public XShapes getSelectedShapes() {
        try {
            return (XShapes) UnoRuntime.queryInterface(XShapes.class, m_xSelectionSupplier.getSelection());
        } catch (java.lang.Exception ex) {
            return null;
        }

    }

    public XShape getSelectedShape(int i) {
        try {
            XShapes xShapes = getSelectedShapes();
            if (xShapes != null) {
                return (XShape) UnoRuntime.queryInterface(XShape.class, xShapes.getByIndex(i));
            }
        } catch (IndexOutOfBoundsException ex) {
            System.err.println(ex.getLocalizedMessage());
        } catch (WrappedTargetException ex) {
            System.err.println(ex.getLocalizedMessage());
        }
        return null;
    }

    public XShape getSelectedShape() {
        return getSelectedShape(0);
    }

    public void setSelectedShape(Object obj) {
        try {
            m_xSelectionSupplier.select(obj);
        } catch (IllegalArgumentException ex) {
            System.err.println(ex.getLocalizedMessage());
        }
    }

    public void modified(EventObject arg0) {


    }

    private Collection<Object> checkPositions() {


        for (int i = 0; i < xDP.getCount(); i++) {
            try {
                Object byIndex = xDP.getByIndex(i);
                XShape xShape = QI.XShape(byIndex);
                if (!positions.get(byIndex).equals(xShape.getPosition())) {
                    DiagramElement diagramElement = whichElementContainsShape.get(xShape);
                    if (diagramElement instanceof Node) {
                        Node node = (Node) diagramElement;
                        //  TODO adjust all related links
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                break;
            } catch (WrappedTargetException e) {
                e.printStackTrace();
            }
        }

        return Collections.EMPTY_LIST;

    }

    private void chooseNodeType(XShape xShape) {
        chooseNodeTypeDialog(xMCF, xShape);
    }

    private short chooseNodeTypeDialog(XMultiComponentFactory _xMCF, final XShape xShape) {
        try {
            Object oDialogModel = _xMCF.createInstanceWithContext("com.sun.star.awt.UnoControlDialogModel", m_xContext);

            // The XMultiServiceFactory of the dialogmodel is needed to instantiate the controls...
            XMultiServiceFactory m_xMSFDialogModel = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, oDialogModel);

            // The named container is used to insert the created controls into...
            final XNameContainer m_xDlgModelNameContainer = (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class, oDialogModel);

            // create the dialog...
            Object oUnoDialog = _xMCF.createInstanceWithContext("com.sun.star.awt.UnoControlDialog", m_xContext);
            XControl m_xDialogControl = (XControl) UnoRuntime.queryInterface(XControl.class, oUnoDialog);

            // The scope of the dialogControl container is public...
            final XControlContainer m_xDlgContainer = (XControlContainer) UnoRuntime.queryInterface(XControlContainer.class, oUnoDialog);

            XTopWindow m_xTopWindow = (XTopWindow) UnoRuntime.queryInterface(XTopWindow.class, m_xDlgContainer);

            // link the dialog and its model...
            XControlModel xControlModel = (XControlModel) UnoRuntime.queryInterface(XControlModel.class, oDialogModel);
            m_xDialogControl.setModel(xControlModel);


            XPropertySet xPSetDialog = (XPropertySet) UnoRuntime.queryInterface(
                    XPropertySet.class, oDialogModel);
            xPSetDialog.setPropertyValue(
                    "PositionX", new Integer(10));
            xPSetDialog.setPropertyValue(
                    "PositionY", new Integer(500));
            xPSetDialog.setPropertyValue(
                    "Width", new Integer(200));
            xPSetDialog.setPropertyValue(
                    "Height", new Integer(70));


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
            xMPS.setPropertyValues(new String[]{"Dropdown", "Height", "Name", "StringItemList"}, new Object[]{Boolean.TRUE, new Integer(12), new String("nodeType"), new String[]{"Server", "Client", "Process", "Procedure"}});
            m_xDlgModelNameContainer.insertByName("nodeTypeListBox", xMPS);

            controlModel = xMCF.createInstanceWithContext("com.sun.star.awt.UnoControlButtonModel", m_xContext);
            xMPS = (XMultiPropertySet) UnoRuntime.queryInterface(XMultiPropertySet.class, controlModel);
            xMPS.setPropertyValues(new String[]{"Height", "Label", "Name", "PositionX", "PositionY", "Width"}, new Object[]{new Integer(14), "Button", "chooseButton", new Integer(10), new Integer("1000"), new Integer(30)});
            m_xDlgModelNameContainer.insertByName("chooseNodeTypeButton", xMPS);
            XButton xButton = UnoRuntime.queryInterface(XButton.class, m_xDlgContainer.getControl("chooseNodeTypeButton"));
            xButton.addActionListener(new MyXActionListener(xShape, m_xDialogControl) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    try {
                        Object nodeTypeListBox = m_xDlgModelNameContainer.getByName("nodeTypeListBox");
                        XControl nodeTypeListBox1 = m_xDlgContainer.getControl("nodeTypeListBox");
                        XListBox xListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class, nodeTypeListBox1);
                        String selectedItem = xListBox.getSelectedItem();
                        System.out.println(selectedItem);


                        XPropertySet xShapeProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);


//                        } catch (UnknownPropertyException e) {
//                            Logger.getLogger(DiagramController.class.getName()).log(Level.SEVERE, null, e);
//                        } catch (PropertyVetoException e) {
//                            Logger.getLogger(DiagramController.class.getName()).log(Level.SEVERE, null, e);
//                        }
//                        catch (IllegalArgumentException ex) {
//                            Logger.getLogger(DiagramController.class.getName()).log(Level.SEVERE, null, ex);
//                        }

                    } catch (com.sun.star.container.NoSuchElementException e) {
                        e.printStackTrace();
                    } catch (WrappedTargetException e) {
                        e.printStackTrace();
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

    public void chooseLinkType(XShape xShape) {
        chooseLinkTypeDialog(xMCF, xShape);
    }

    public short chooseLinkTypeDialog(XMultiComponentFactory _xMCF, final XShape xShape) {
        try {
            Object oDialogModel = _xMCF.createInstanceWithContext("com.sun.star.awt.UnoControlDialogModel", m_xContext);

            // The XMultiServiceFactory of the dialogmodel is needed to instantiate the controls...
            XMultiServiceFactory m_xMSFDialogModel = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, oDialogModel);

            // The named container is used to insert the created controls into...
            final XNameContainer m_xDlgModelNameContainer = (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class, oDialogModel);

            // create the dialog...
            Object oUnoDialog = _xMCF.createInstanceWithContext("com.sun.star.awt.UnoControlDialog", m_xContext);
            XControl m_xDialogControl = (XControl) UnoRuntime.queryInterface(XControl.class, oUnoDialog);

            // The scope of the dialogControl container is public...
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
                    "Width", new Integer(200));
            xPSetDialog.setPropertyValue(
                    "Height", new Integer(70));


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
            xButton.addActionListener(new MyXActionListener(xShape, m_xDialogControl) {
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
                            Link linkReplace = linkFactory.create(Link.LinkType.valueOf(selectedItem), m_xComponent, DrawHelper.getCurrentDrawPage(m_xComponent), xShStart, xShEnd, false);
                            Linker linker = new LinkerImpl(linkReplace, xConnectorShape);
                            linker.link(xShStart, xShEnd);
                            dialogControl.dispose();

                        } catch (UnknownPropertyException e) {
                            e.printStackTrace();
                        } catch (WrappedTargetException e) {
                            e.printStackTrace();
                        }

                        //To change body of implemented methods use File | Settings | File Templates.


                    } catch (com.sun.star.container.NoSuchElementException e) {
                        e.printStackTrace();
                    } catch (WrappedTargetException e) {
                        e.printStackTrace();
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

    public void addNode(String name) throws Exception {
    }

    public void addLink(String name) throws Exception {
    }

    @Override
    public void selectionChanged(EventObject eventObject) {
        //TODO implement
        inputMode.onInput(eventObject, this);

    }

    public void setInputMode(InputTwoShapesMode inputMode) {
        inputMode.setDiagramController(this);
        this.inputMode = inputMode;
    }

    public void resetInputMode() {
        inputMode = DEFAULT_INPUT_MODE;
    }

    public enum State {
        Nothing,
        InputTwoShapes,
        AddingLink
    }

    private class Pair<F, S> {
        F f;
        S s;

        private Pair(F f, S s) {
            this.f = f;
            this.s = s;
        }
    }

    private class MyXActionListener implements XActionListener {
        protected XControl dialogControl;
        protected XShape xShape;

        private MyXActionListener(XShape xShape, XControl m_xDialogControl) {
            this.xShape = xShape;
            this.dialogControl = m_xDialogControl;
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