
package ru.ssau.graphplus;

import com.sun.star.awt.*;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.document.XUndoManager;
import com.sun.star.drawing.*;
import com.sun.star.frame.*;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.Locale;
import com.sun.star.lang.*;
import com.sun.star.task.XStatusIndicator;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.URL;
import com.sun.star.util.XModifyListener;
import com.sun.star.view.XSelectionChangeListener;
import com.sun.star.view.XSelectionSupplier;
import ru.ssau.graphplus.api.DiagramElement;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.commons.ShapeHelper;
import ru.ssau.graphplus.events.*;
import ru.ssau.graphplus.gui.dialogs.ChooseNodeTypeDialog;
import ru.ssau.graphplus.link.*;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.node.NodeBase;
import ru.ssau.graphplus.node.NodeFactory;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * This controller responsible for
 */
public class DiagramController implements
        XModifyListener,
//        XDispatch,
        XSelectionChangeListener {

    //    private static ConnectorShapeListener connectorShapeListener;
    private static URL lastURL;
    private final XComponent m_xComponent;
    private final XComponent xDrawDoc;


    private static final InputMode DEFAULT_INPUT_MODE = new InputMode() {
        @Override
        public void onInput(EventObject eventObject) {
            // consume
        }

        @Override
        public void setDiagramController(DiagramController diagramController) {
            //nothing
        }
    };
    private final MyDispatch dispatch;
    private final XUndoManager undoManager;


    State state;

    Map<State, DiagramEventHandler> diagramEventHandlerMap = new HashMap<State, DiagramEventHandler>();
    DiagramEventHandler diagramEventHandler;
    XDrawPage xDP = null;


    private InputMode inputMode;
    private DiagramModel diagramModel;
    private NodeFactory nodeFactory;
    private LinkFactory linkFactory;
    private List<ShapeRemovedEvent> removedEvents = new ArrayList();
    private XStatusIndicator statusIndicator;

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


    private transient static List<WeakReference<DiagramController>> instances = new ArrayList<>();

    public static List<WeakReference<DiagramController>> getInstances() {
        return Collections.unmodifiableList(instances);
    }

    public DiagramController(XComponentContext xContext, XFrame xFrame, XMultiServiceFactory xMSF_, XMultiComponentFactory xMCF_, final DiagramModel diagramModel, XComponent xDoc, MyDispatch myDispatch, XUndoManager undoManager) {
//        OOGraph.LOGGER.info("DiagramController ctor");
        this.undoManager = undoManager;
        dispatch = myDispatch;

        inputMode = new InputMode() {
            @Override
            public void onInput(EventObject eventObject) {
                // nothing
            }

            @Override
            public void setDiagramController(DiagramController diagramController) {
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
        elements = new HashMap();
        whichElementContainsShape = new HashMap();
        positions = new HashMap();
        addSelectionListener();

//        OOGraph.LOGGER.info("adding shape event listeners");


    }

    public void setNodeFactory(NodeFactory nodeFactory) {
        this.nodeFactory = nodeFactory;
    }

    public void setLinkFactory(LinkFactory linkFactory) {
        this.linkFactory = linkFactory;
    }

    public DiagramModel getDiagramModel() {
        return diagramModel;
    }


    private String getPackageLocation() {
        Object valueByName = m_xContext.getValueByName("/singletons/com.sun.star.deployment.PackageInformationProvider");
        XPackageInformationProvider xPackageInformationProvider = UnoRuntime.queryInterface(XPackageInformationProvider.class, valueByName);
        return xPackageInformationProvider.getPackageLocation("ru.ssau.graphplus.oograph");
    }

    public void configureListeners(Link link) {
    }


    public void onShapeInserted(com.sun.star.document.EventObject arg0) {
        System.out.println("ShapeInserted");


    }

    private void fireDiagramEvent(DiagramEvent diagramEvent) {
        if (diagramEvent instanceof ElementAddEvent) {
            diagramEventHandler.elementAdded((ElementAddEvent) diagramEvent);
        }

    }

    public void onShapeModified(XShape shape) {

        DiagramElement diagramElementByShape = diagramModel.getDiagramElementByShape(shape);
        if (diagramElementByShape instanceof Node) {

            NodeBase node = (NodeBase) diagramElementByShape;
            node.setName(QI.XText(node.getShape()).getString());
            diagramModel.fireEvent(new NodeModifiedEvent(node));
        }

    }
//
//    public void onShapeInserted(XShape shape){
//        DiagramElement diagramElementByShape = diagramModel.getDiagramElementByShape(shape);
//        if (diagramElementByShape instanceof Node){
//            diagramModel.fireEvent(new NodeAddedEvent((Node) diagramElementByShape));
//        }
//
//    }

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

                        @Override
                        public void onEvent(Event event) {
                            // empty body TODO
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

//    public Linker getLinker() {
//        return linker;
//    }
//
//    public void setLinker(Linker linker) {
//        this.linker = linker;
//    }

    public XShapes getSelectedShapes() {
        try {
            return UnoRuntime.queryInterface(XShapes.class, m_xSelectionSupplier.getSelection());
        } catch (java.lang.Exception ex) {
            return null;
        }
    }

    public XShape getSelectedShape(int i) {
        try {
            XShapes xShapes = getSelectedShapes();
            if (xShapes != null) {
                return UnoRuntime.queryInterface(XShape.class, xShapes.getByIndex(i));
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

    // unused
    private void chooseNodeType(XShape xShape) {
        new ChooseNodeTypeDialog().chooseNodeType(xMCF, xShape, m_xContext);
    }

    //

    private void insertLinkShapes(Link link) {

        LinkTwoConnectorsAndTextBase link_ = (LinkTwoConnectorsAndTextBase) link;

        for (XShape shape : link_.getShapes()) {
            DrawHelper.insertShapeOnCurrentPage(shape, getDiagramModel().getDrawDoc());
        }
    }

    public void onShapeRemoved(XShape xShape) {

        DiagramElement diagramElementByShape = getDiagramModel().getDiagramElementByShape(xShape);
        if (diagramElementByShape != null) {
            if (diagramElementByShape instanceof Link) {


                LinkBase linkBase = (LinkTwoConnectorsAndTextBase) diagramElementByShape;

                diagramModel.removeDiagramElement(diagramElementByShape);

                for (XShape linkShape : linkBase.getShapes()) {
                    if (!linkShape.equals(xShape)) {
                        ShapeHelper.removeShape(linkShape, DrawHelper.getCurrentDrawPage(xDrawDoc));
                    }
                }
            }

            if (diagramElementByShape instanceof Node) {
                diagramModel.removeDiagramElement(diagramElementByShape);
            }

        }

    }

    public void insertLink(Link link) {
        diagramModel.addDiagramElement(link);
    }

    public void insertNode(Node node){
        diagramModel.addDiagramElement(node);
    }

    public interface NodeSelectionListener {
        void nodeSelected(Node node);
    }

    interface NodeSelectionController {
        void nodeSelected(Node node);

        void addNodeSelectionListener(NodeSelectionListener nodeSelectionListener);
    }

    public void select(DiagramElement diagramElement) {
        if (diagramElement instanceof NodeBase) {
            try {
                getXSelectionSupplier().select(((NodeBase) diagramElement).getShape());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        if (diagramElement instanceof LinkBase) {
            LinkBase linkBase = (LinkBase) diagramElement;

            ShapesProvider shapesProvider = linkBase;

            try {
                //            com.sun.star.drawing.ShapeCollection
                Object shapesCollection = null;

                shapesCollection = xMCF.createInstanceWithContext("com.sun.star.drawing.ShapeCollection", m_xContext);
                XShapes xShapes = QI.XShapes(shapesCollection);
                Iterable<XShape> shapes = shapesProvider.getShapes();
                for (XShape xShape : shapes) {
                    xShapes.add(xShape);
                }
                try {
                    getXSelectionSupplier().select(xShapes);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {


            try {
                ShapesProvider shapesProvider = (ShapesProvider) diagramElement;
                //            com.sun.star.drawing.ShapeCollection
                Object shapesCollection = null;

                shapesCollection = xMCF.createInstanceWithContext("com.sun.star.drawing.ShapeCollection", m_xContext);
                XShapes xShapes = QI.XShapes(shapesCollection);
                Iterable<XShape> shapes = shapesProvider.getShapes();
                for (XShape xShape : shapes) {
                    xShapes.add(xShape);
                }
                try {
                    getXSelectionSupplier().select(xShapes);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    class NodeSelectionControllerImpl implements NodeSelectionController {
        List<NodeSelectionListener> nodeSelectionListeners = new ArrayList<>();

        public void nodeSelected(Node node) {
            for (NodeSelectionListener nodeSelectionListener1 : nodeSelectionListeners) {
                nodeSelectionListener1.nodeSelected(node);
            }
        }

        public void addNodeSelectionListener(NodeSelectionListener nodeSelectionListener) {
            nodeSelectionListeners.add(nodeSelectionListener);
        }
    }

    NodeSelectionController nodeSelectionController = new NodeSelectionControllerImpl();

    public void addNodeSelectionListener(NodeSelectionListener nodeSelectionListener) {
        nodeSelectionController.addNodeSelectionListener(nodeSelectionListener);
    }

    @Override
    public void selectionChanged(EventObject eventObject) {
        //TODO implement


        Object shapeObj = getSelectedShape();
//        Misc.printInfo(shapeObj);
        XShapes selectedShapes = getSelectedShapes();

        if (selectedShapes != null) {
            if (selectedShapes.getCount() == 1) {
                try {
                    Object byIndex = selectedShapes.getByIndex(0);
                    XShape shape = QI.XShape(byIndex);
                    DiagramElement diagramElementByShape = getDiagramModel().getDiagramElementByShape(shape);
                    if (diagramElementByShape instanceof Node) {
                        Node nodeByShape = (Node) diagramElementByShape;

                        nodeSelectionController.nodeSelected(nodeByShape);


//                        LinkNodesDialog.map.get(dispatch).setNodeZ(nodeByShape);

                    }

                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (WrappedTargetException e) {
                    e.printStackTrace();
                }
            }
        }


        inputMode.onInput(eventObject);

        DiagramElement diagramElementByShape = getDiagramModel().getDiagramElementByShape(getSelectedShape());

//        OOGraph.aController.displayMessage(diagramElementByShape.toString());
    }

    public void setInputMode(InputMode inputMode) {
        inputMode.setDiagramController(this);
        this.inputMode = inputMode;
    }

    public void resetInputMode() {
        inputMode = DEFAULT_INPUT_MODE;
    }

    public void statusChangedEnable(URL lastURL) {
        dispatch.statusChangedEnable(lastURL);
    }

    public enum State {
        Nothing,
        InputTwoShapes,
        AddingLink
    }


}