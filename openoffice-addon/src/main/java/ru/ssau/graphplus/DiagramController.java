
package ru.ssau.graphplus;

import com.google.common.collect.Sets;
import com.sun.star.awt.*;
import com.sun.star.deployment.XPackageInformationProvider;
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
import ru.ssau.graphplus.api.DiagramService;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.commons.ShapeHelper;
import ru.ssau.graphplus.events.*;
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
        XSelectionChangeListener {

    private final XComponent xDrawDoc;

    private static final InputMode DEFAULT_INPUT_MODE = new InputMode() {
        @Override
        public void onInput(Event eventObject) {
            // consume
        }
    };
    private final MyDispatch dispatch;

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

    private XMultiServiceFactory xMSF = null;
    private XMultiComponentFactory xMCF = null;
    private List<String> historyOfActions = new ArrayList<String>();
    private Map<Object, Point> positions = null;

    public XSelectionSupplier getXSelectionSupplier() {
        return m_xSelectionSupplier;
    }

    private transient static List<WeakReference<DiagramController>> instances = new ArrayList<>();

    public static List<WeakReference<DiagramController>> getInstances() {
        return Collections.unmodifiableList(instances);
    }

    public DiagramController(XComponentContext xContext, XFrame xFrame, XMultiServiceFactory xMSF_, XMultiComponentFactory xMCF_, final DiagramModel diagramModel, XComponent xDoc, MyDispatch myDispatch) {


        dispatch = myDispatch;
        inputMode = new InputMode() {

            public void onInput(Event eventObject) {

            }
        };
        xDrawDoc = xDoc;
        this.diagramModel = diagramModel;

        m_xContext = xContext;
        m_xFrame = xFrame;
        m_xController = m_xFrame.getController();
        xMCF = xMCF_;
        xMSF = xMSF_;
        xDP = DrawHelper.getCurrentDrawPage(xDrawDoc);


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
        if (diagramElementByShape == null) {
            return;
        }
        if (diagramElementByShape instanceof Node) {
            NodeBase node = (NodeBase) diagramElementByShape;
            node.setName(QI.XText(node.getShape()).getString());
            diagramModel.fireEvent(new NodeModifiedEvent(node));
        }
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
    @Override
    public void modified(EventObject eventObject) {
    }

    /**
     * This class is for removing complex links
     */
    private class LinkRemoveGuard {
        private HashSet<XShape> shapes;

        boolean isFree() {
            return shapes == null || shapes.isEmpty();
        }

        boolean workOut(XShape shape) {
            if (shapes != null) {
                return shapes.remove(shape);
            } else {
                throw new IllegalStateException();
            }
        }

        void reset() {
            shapes.clear();
        }

        void fill(LinkTwoConnectorsAndTextBase linkTwoConnectorsAndTextBase) {
            if (!isFree()) {
                throw new IllegalStateException("not free yet");
            }
            Iterable<XShape> shapes = linkTwoConnectorsAndTextBase.getShapes();
            this.shapes = Sets.newHashSet(shapes);
        }
    }
    private LinkRemoveGuard linkRemoveGuard = new LinkRemoveGuard();

    public void onShapeRemoved(XShape xShape) {
        DiagramElement diagramElementByShape = getDiagramModel().getDiagramElementByShape(xShape);
        if (diagramElementByShape != null) {
            if (diagramElementByShape instanceof Link) {
                if (diagramElementByShape instanceof LinkTwoConnectorsAndTextBase) {
                    LinkTwoConnectorsAndTextBase linkBase = (LinkTwoConnectorsAndTextBase) diagramElementByShape;
//                    if (linkRemoveGuard.isFree()){
//                        linkRemoveGuard.fill(linkBase);
//                    }
//                    else {
//                        if (linkRemoveGuard.workOut(xShape)){
//                            return;
//                        }
//
//                    }
                    diagramModel.removeDiagramElement(diagramElementByShape);
                    int i = 0;
                    for (XShape linkShape : linkBase.getShapes()) {
//                        if (!UnoRuntime.areSame(linkShape,xShape)) {
                            XDrawPage currentDrawPage = DrawHelper.getCurrentDrawPage(xDrawDoc);
                            if (ShapeHelper.removeShape(linkShape, currentDrawPage)) {
//                                i++;
                            } else {
//                                i = ShapeHelper.removeShape(linkBase.getName(), currentDrawPage);
                            }
//                        }
                    }
//                    if (i == 2) {
//                        linkRemoveGuard.reset();
//                    }
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

    public void insertNode(Node node) {
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

    @Override
    public void selectionChanged(EventObject eventObject) {
        Object shapeObj = getSelectedShape();
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
                    }
                    inputMode.onInput(DiagramElementSelected.create(diagramElementByShape));
                } catch (IndexOutOfBoundsException | WrappedTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        DiagramElement diagramElementByShape = getDiagramModel().getDiagramElementByShape(getSelectedShape());
//        OOGraph.aController.displayMessage(diagramElementByShape.toString());
    }

    public void setInputMode(InputMode inputMode) {
        this.inputMode = inputMode;
    }

    public void resetInputMode() {
        inputMode = DEFAULT_INPUT_MODE;
    }

    public void statusChangedEnable(URL lastURL) {
        dispatch.statusChangedEnable(lastURL);
    }

    /**
     * User: anton
     * Date: 5/18/13
     * Time: 6:09 PM
     */
    public static class InputTwoNodesMode implements InputMode {

        private Link link;
        private DiagramService diagramService;
        private Node first;
        private Node second;
        private DiagramController diagramController;

        public InputTwoNodesMode(DiagramController diagramController, Link link, DiagramService diagramService) {
            this();
            this.link = link;
            this.diagramService = diagramService;
            this.diagramController = diagramController;
        }

        public InputTwoNodesMode() {

        }

        enum State {
            Nothing,
            First,
            Second,
        }

        State state = State.Nothing;

        @Override
        public void onInput(Event eventObject) {

            if (eventObject instanceof NodeSelectedEvent){
                NodeSelectedEvent nodeSelectedEvent = (NodeSelectedEvent) eventObject;
                if (state.equals(State.Nothing)){
                    state = State.First;
                    first = nodeSelectedEvent.getNode();
                }
                else if (state.equals(State.First)){
                    second = nodeSelectedEvent.getNode();
                    diagramService.linkNodes(first, second, link);
                    diagramService.layoutLink(first, second, link);
                    state = State.Nothing;
                    diagramController.setInputMode(DEFAULT_INPUT_MODE);
                }
            }

        }


    }
}