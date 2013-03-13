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
import com.sun.star.container.*;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.frame.*;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.Locale;
import com.sun.star.uno.*;
import com.sun.star.uno.Exception;
import com.sun.star.util.URL;
import ru.ssau.graphplus.gui.Gui;
import ru.ssau.graphplus.link.Link;
import ru.ssau.graphplus.link.LinkFactory;
import ru.ssau.graphplus.link.Linker;
import com.sun.star.drawing.*;
import com.sun.star.lang.*;
import com.sun.star.util.XModifyListener;
import com.sun.star.view.XSelectionChangeListener;
import com.sun.star.view.XSelectionSupplier;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import ru.ssau.graphplus.link.LinkerImpl;
import ru.ssau.graphplus.node.Node;
import ru.ssau.graphplus.node.NodeFactory;

/**
 * @author anton
 */
public final class DiagramController implements XSelectionChangeListener, XModifyListener, XDispatch {


    private final XComponent m_xComponent;
    private DiagramModel diagramModel;
    private final XComponent xDrawDoc;
    private NodeFactory nodeFactory;
    private LinkFactory linkFactory;


    @Override
    public void dispatch(URL url, PropertyValue[] propertyValues) {
        if (url.Protocol.compareTo("ru.ssau.graphplus:") == 0) {

            Object nodeObject;
            Object linkObject;

            Link link = null;
            Node node = null;


            if (url.Path.compareTo("Omg") == 0) {


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
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (WrappedTargetException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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

                    //xDispatchProvider.queryDispatch(new URL())
                } catch (com.sun.star.uno.Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

            }


            if (url.Path.compareTo("Node") == 0) {

                try {
                    XDrawPage xDrawPage = DrawHelper.getDrawPageByIndex(xDrawDoc, 0);
                    XShape xShape = ShapeHelper.createShape(m_xComponent, new Point(800, 600), new Size(1500, 1500), "com.sun.star.drawing.EllipseShape");// .createEllipseShape(m_xComponent, 800, 800, 1500, 1500);
                    node = nodeFactory.create(Node.NodeType.Client, m_xComponent);
                    XPropertySet xPS = QI.XPropertySet(xShape);
//                    XEnumerableMap em = EnumerableMap.create(m_xContext, Type.STRING, Type.ANY);

                    try {
                        Object ncObj = xMSF.createInstance("com.sun.star.drawing.BitmapTable");
                        XNameContainer xNamedCont = (XNameContainer) QI.XNameContainer(ncObj);


                    } catch (ServiceNotRegisteredException ex) {
                    }

                    XNameContainer xNC = QI.XNameContainer(xPS.getPropertyValue("ShapeUserDefinedAttributes"));

                    XPropertySet xPropSet = QI.XPropertySet(xShape);
//                    xPropSet.setPropertyValue("SizeProtect", true);
                    //printInfo(xShape);

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


            if (url.Path.compareTo("ProcessNode") == 0) {
                try {

//                    xDrawDoc = (XComponent) UnoRuntime.queryInterface(
//                            XComponent.class, m_xComponent);

                    XDrawPage xPage = PageHelper.getDrawPageByIndex(xDrawDoc, 0);
                    XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, xPage);


                    Node processNode = nodeFactory.create(Node.NodeType.Process, m_xComponent);
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

            if (url.Path.compareTo("ProcedureNode") == 0) {
                try {

//                    m_xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, m_xFrame.getController().getModel());

//
//                    xDrawDoc = (XComponent) UnoRuntime.queryInterface(
//                            XComponent.class, m_xComponent);


                    XDrawPage xPage = PageHelper.getDrawPageByIndex(xDrawDoc, 0);
                    XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, xPage);
                    Node procedureNode = nodeFactory.create(Node.NodeType.Procedure, m_xComponent);//createAndInsert(Node.NodeType.Process, m_xComponent, xShapes);
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


            if (url.Path.compareTo("ClientNode") == 0) {
                try {

//                    m_xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, m_xFrame.getController().getModel());
//
//
//                    xDrawDoc = (XComponent) UnoRuntime.queryInterface(
//                            XComponent.class, m_xComponent);
                    Node processNode = nodeFactory.create(Node.NodeType.Client, m_xComponent);
                    node = processNode;


                    DrawHelper.insertNodeOnCurrentPage(processNode, xDrawDoc);

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

            if (url.Path.compareTo("ServerNode") == 0) {
                try {
                    Node serverNode = nodeFactory.create(Node.NodeType.Server, m_xComponent);
                    node = serverNode;

                    DrawHelper.insertShapeOnCurrentPage(serverNode.getShape(), xDrawDoc);

                    Misc.addUserDefinedAttributes(serverNode.getShape(), xMSF);
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


            if (url.Path.compareTo("LinkLink") == 0) {

                Link linkLink = linkFactory.create(Link.LinkType.Link, xDrawDoc, DrawHelper.getCurrentDrawPage(xDrawDoc));
                link = linkLink;
                for (XShape shape : linkLink.getShapes()) {
                    DrawHelper.insertShapeOnCurrentPage(shape, xDrawDoc);
                }


                setState(DiagramController.State.InputTwoShapes);
                setLinker(linkLink);

            }

            if (url.Path.compareTo("MessageLink") == 0) {
//                xDrawDoc = (XComponent) UnoRuntime.queryInterface(
//                        XComponent.class, m_xComponent);
                Link messageLink = linkFactory.create(Link.LinkType.Message, xDrawDoc, DrawHelper.getCurrentDrawPage(xDrawDoc));
                link = messageLink;

                for (XShape shape : messageLink.getShapes()) {
                    DrawHelper.insertShapeOnCurrentPage(shape, xDrawDoc);
                }

                setState(DiagramController.State.InputTwoShapes);
                setLinker(messageLink);

                //return;
            }

            if (url.Path.compareTo("ControlLink") == 0) {
//                xDrawDoc = (XComponent) UnoRuntime.queryInterface(
//                        XComponent.class, m_xComponent);
                Link controlLink = linkFactory.create(Link.LinkType.Control, xDrawDoc, DrawHelper.getCurrentDrawPage(xDrawDoc));
                link = controlLink;

                for (XShape shape : controlLink.getShapes()) {
                    DrawHelper.insertShapeOnCurrentPage(shape, xDrawDoc);
                }

                setState(DiagramController.State.InputTwoShapes);
                setLinker(controlLink);

                //return;
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
                                            linkReplace = linkFactory.create(Link.LinkType.valueOf(nodeType), m_xComponent, xDP);

                                            Node.PostCreationAction postCreationAction = new Node.PostCreationAction() {
                                                @Override
                                                public void postCreate(XShape shape) {
                                                    if (finalConvertShape) {
                                                        if (finalLinkReplace != null) {
                                                            Misc.tagShapeAsLink(finalLinkReplace.getConnShape1());
                                                            Misc.tagShapeAsLink(finalLinkReplace.getConnShape2());


                                                            xDP.remove(xShape);
                                                        }
                                                    } else {
                                                        Misc.tagShapeAsNode(xShape);
                                                    }
                                                }
                                            };

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
                         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     } catch (WrappedTargetException e) {
                         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
                                        Node nodeReplace = null;
                                        final boolean finalConvertShape = convertShape;
                                        final Node finalNodeReplace = nodeReplace;
                                        if (convertShape) {
                                            nodeReplace = nodeFactory.create(Node.NodeType.valueOf(nodeType), m_xComponent);

                                            Node.PostCreationAction postCreationAction = new Node.PostCreationAction() {
                                                @Override
                                                public void postCreate(XShape shape) {
                                                    if (finalConvertShape) {
                                                        if (finalNodeReplace != null) {
                                                            Misc.tagShapeAsNode(finalNodeReplace.getShape());

                                                            xDP.remove(xShape);
                                                        }
                                                    } else {
                                                        Misc.tagShapeAsNode(xShape);
                                                    }
                                                }
                                            };

                                            ShapeHelper.insertShape(nodeReplace.getShape(), xDP, postCreationAction);
                                            try {
                                                nodeReplace.getShape().setPosition(xShape.getPosition());
                                                nodeReplace.getShape().setSize(xShape.getSize());
                                            } catch (PropertyVetoException e) {
                                                e.printStackTrace();
                                            }
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
                        } catch (Exception e) {
                            e.printStackTrace();
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

            }

            if (url.Path.contains("Link")) {
                // common for all links
                state = State.AddingLink;
                if (link != null){
                    diagramModel.addDiagramElement(link);
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
    }

    @Override
    public void removeStatusListener(XStatusListener xStatusListener, URL url) {
    }



    Set<ShapeInsertedEvent> shapeInsertedEvents = new HashSet<ShapeInsertedEvent>();
    int connectorsAdded = 0;
    boolean isTextShapeAdded = false;

    public void onShapeInserted(com.sun.star.document.EventObject arg0) {
        System.out.println("ShapeInserted");


        if (shapeInsertedEvents.size() == 2){


            boolean matched = true;

            for (ShapeInsertedEvent shapeInsertedEvent : shapeInsertedEvents) {
                if (Misc.isTextShape(shapeInsertedEvent.xShape)){

                        if (isTextShapeAdded == true){
                            matched = false;
                            break;
                        }
                        else {
                            isTextShapeAdded = true;
                        }


                } else if (Misc.isConnectorShape(shapeInsertedEvent.xShape)) {
                    connectorsAdded++;
                }
            }
            matched = matched & (connectorsAdded == 2);

            if (matched) {
                ElementAddEvent elementAddEvent = new ElementAddEvent();
                fireDiagramEvent(elementAddEvent);
            }
            else {
                shapeInsertedEvents.clear();
                connectorsAdded = 0;
                isTextShapeAdded = false;
            }
            // todo


        }

        else {

            shapeInsertedEvents.add(new ShapeInsertedEvent(arg0, new Date()));


        }


//        XShape xShape = QI.XShape(arg0.Source);
//        if (xShape !=null) {
//            if (Misc.isConnectorShape(xShape)){
//                XPropertySet xPropertySet = QI.XPropertySet(xShape);
//                try {
//                    xPropertySet.getPropertyValue("StartShape");
//                } catch (UnknownPropertyException e) {
//                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                } catch (WrappedTargetException e) {
//                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                }
//                try {
//                    Object endShape = xPropertySet.getPropertyValue("EndShape");
//                    XShape xShape1 = QI.XShape(endShape);
//                    if (Misc.isTextShape(xShape1)){
//
//                    }
//                } catch (UnknownPropertyException e) {
//                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                } catch (WrappedTargetException e) {
//                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                }
//
//
//            }
//        }
    }

    private void fireDiagramEvent(DiagramEvent diagramEvent) {
       if (diagramEvent instanceof ElementAddEvent){
        diagramEventHandler.elementAdded(new ElementAddEvent());
       }

           }

    public void onShapeModified(com.sun.star.document.EventObject arg0) {
        System.out.println("ShapeModified");

    }

    public enum State {
        Nothing,
        InputTwoShapes,
        AddingLink
    }

    State state;

    public State getState() {
        return state;
    }

    Map<State, DiagramEventHandler>  diagramEventHandlerMap = new HashMap<State, DiagramEventHandler>();

    DiagramEventHandler diagramEventHandler;

    public void setState(State state) {
        this.state = state;
        if (diagramEventHandlerMap.containsKey(state)){
            diagramEventHandler =  diagramEventHandlerMap.get(state);
        }
        else {
            switch (state){
                case AddingLink:
                    AddingLink addingLink = new AddingLink();
                    diagramEventHandlerMap.put(state, addingLink);
                    break;
            }
        }
    }


    private XComponentContext m_xContext = null;
    private XFrame m_xFrame = null;
    private XController m_xController = null;
    private XSelectionSupplier m_xSelectionSupplier = null;
    private ArrayList<XShape> nodes = null;
    private ArrayList<XShape> links = null;
    private Map<String, XShape> elements = null;
    private XMultiServiceFactory xMSF = null;
    private XMultiComponentFactory xMCF = null;
    private Map<XShape, DiagramElement> whichElementContainsShape = null;

    private List<String> historyOfActions = new ArrayList<String>();


    public List<String> getHistoryOfActions() {
        return historyOfActions;
    }

    private Map<Object, Point> positions = null;

    XDrawPage xDP = null;
    int count = 0;

    DiagramController(XComponentContext xContext, XFrame xFrame, XMultiServiceFactory xMSF_, XMultiComponentFactory xMCF_, XDrawPage xDP_, DiagramModel diagramModel, XComponent m_xComponent, XComponent xComponent) {
        xDrawDoc = xComponent;
        this.diagramModel = diagramModel;
        this.m_xComponent = xComponent;
        m_xContext = xContext;
        m_xFrame = xFrame;
        m_xController = m_xFrame.getController();
        xMCF = xMCF_;
        xMSF = xMSF_;
        xDP = xDP_;
        nodeFactory = new NodeFactory(xMSF_);
        linkFactory = new LinkFactory(xMSF_);
        nodes = new ArrayList<XShape>();
        links = new ArrayList<XShape>();
        elements = new HashMap<String, XShape>();
        whichElementContainsShape = new HashMap<XShape, DiagramElement>();
        positions = new HashMap<Object, Point>();
        addSelectionListener();
        //historyOfActions = new ArrayList<String>();
    }

    public Map<String, XShape> getElements() {
        return elements;
    }

    public void addSelectionListener() {
        if (m_xSelectionSupplier == null) {
            m_xSelectionSupplier = (XSelectionSupplier) UnoRuntime.queryInterface(XSelectionSupplier.class, m_xController);
        }
        if (m_xSelectionSupplier != null) {
            m_xSelectionSupplier.addSelectionChangeListener(this);
        }
    }

    public void removeSelectionListener() {
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

    // XSelectionChangeListener

    public void disposing(EventObject arg0) {
    }

    //    boolean fistSelected = false;
//    boolean secondSelected = false;
    XShape firstShape;
    XShape secondShape;
    Linker linker;

    public Linker getLinker() {
        return linker;
    }

    public void setLinker(Linker linker) {
        this.linker = linker;
    }

    // XSelectionChangeListener
    @Override
    public void selectionChanged(EventObject event) {
        System.out.println("selectionChanged");

        Object shapeObj = getSelectedShape();
        if (shapeObj != null) {
            XNamed xNamed = (XNamed) UnoRuntime.queryInterface(XNamed.class, getSelectedShape());
            String selectedShapeName = xNamed.getName();
            // listen the diagrams
            Misc.printInfo(shapeObj);

            if (state.equals(State.InputTwoShapes)) {
                if (firstShape != null && secondShape != null) {
                    firstShape = null;
                    secondShape = null;
                }


                if (firstShape == null) {
                    firstShape = QI.XShape(shapeObj);
                } else {
                    if (secondShape == null) {
                        secondShape = QI.XShape(shapeObj);
                        // 2 shapes 
                        if (linker != null) {
                            linker.link(firstShape, secondShape);

                            setState(State.Nothing);
//                        LinkAdjuster.adjustLink((Link)linker);
                            setSelectedShape(linker.getTextShape());
                            // TODO remove next
                            Link linker1 = (Link) linker;
                            //QI.XPropertySet(linker1.getConnShape1()).addPropertyChangeListener();
                        }
                    } else {

                    }

                }
            } else {
            }
        }

    }

    public void setSelectedShape(Object obj) {
        try {
            m_xSelectionSupplier.select(obj);
        } catch (IllegalArgumentException ex) {
            System.err.println(ex.getLocalizedMessage());
        }
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
        return (XShapes) UnoRuntime.queryInterface(XShapes.class, m_xSelectionSupplier.getSelection());
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

    public void modified(EventObject arg0) {

        if (xDP.getCount() > count) {
            count++;
            System.out.println("added new shape");

            Object obj = null;
            try {
                obj = xDP.getByIndex(xDP.getCount() - 1);
            } catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
                Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
            } catch (WrappedTargetException ex) {
                Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
            }
            XShape xShape = (XShape) UnoRuntime.queryInterface(XShape.class, obj);
            positions.put(obj, xShape.getPosition());
            if (Misc.isNode(xShape) || Misc.isLink(xShape)) {

            } else {
                Misc.addUserDefinedAttributes(xShape, xMSF);
            }

            if (Status.isTagAllNewShapes() && xShape.getShapeType().equals("com.sun.star.drawing.ConnectorShape")) {


                links.add(xShape);


                System.out.println("ConnectorShape added");
                XConnectorShape xConnSh = (XConnectorShape) UnoRuntime.queryInterface(XConnectorShape.class, xShape);

                xMCF = m_xContext.getServiceManager();
                xMSF = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, xMCF);

                try {


                    XPropertySet xShapeProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xConnSh);
                    Object startShape = xShapeProps.getPropertyValue("StartShape");
                    Object endShape = xShapeProps.getPropertyValue("EndShape");
                    XShape xShStart = (XShape) UnoRuntime.queryInterface(XShape.class, startShape);
                    XShape xShEnd = (XShape) UnoRuntime.queryInterface(XShape.class, endShape);


                    XPropertySet xShStartProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShStart);


                    XPropertySet xShEndProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShEnd);

                    XNamed xNamedConnector = (XNamed) UnoRuntime.queryInterface(XNamed.class, xConnSh);
                    Misc.tagShapeAsLink(xShape);
                    Gui.createDialog(xNamedConnector, xConnSh, m_xContext, elements);
                    // TODO
                    elements.put(QI.XNamed(xShape).getName(), xShape);
                    XPropertySet xPS = QI.XPropertySet(xShape);
                    xPS.setPropertyValue("Text", QI.XNamed(xShape).getName());
                } catch (com.sun.star.uno.Exception ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                if (Status.isTagAllNewShapes() && xShape.getShapeType().equals("com.sun.star.drawing.EllipseShape")) {

                    // not connector shape

                    nodes.add(xShape);
                    XPropertySet xShapeProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);

                    String s;
                    XNamed xNamed = (XNamed) UnoRuntime.queryInterface(
                            XNamed.class, xShape);
                    Misc.tagShapeAsNode(xShape);
                    try {
                        Gui.createDialog(xNamed, xShape, m_xContext, elements);
                    } catch (com.sun.star.uno.Exception ex) {
                        Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // TODO
                    elements.put(QI.XNamed(xShape).getName(), xShape);


                }
            }

//            if (state.equals(State.AddingLink)){
//
//                if (addingListMode == null) {
//                    addingListMode = new AddingListMode();
//                }
//
//                addingListMode.oneMoreTime(xShape);
//
//                if (addingListMode.needToDetach()){
//                    addingListMode = null;
//                }
//            }


        }
    }

    private AddingListMode addingListMode = null;


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
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        } catch (WrappedTargetException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }

                        //To change body of implemented methods use File | Settings | File Templates.


                    } catch (com.sun.star.container.NoSuchElementException e) {
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


    public void addNode(String name) throws Exception {
    }

    public void addLink(String name) throws Exception {
    }
}