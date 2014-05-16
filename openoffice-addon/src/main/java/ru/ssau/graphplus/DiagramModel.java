package ru.ssau.graphplus;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.star.drawing.*;
import com.sun.star.lang.*;
import com.sun.star.lang.IndexOutOfBoundsException;
import ru.ssau.graphplus.codegen.impl.DiagramWalker;
import ru.ssau.graphplus.api.DiagramElement;
import ru.ssau.graphplus.api.DiagramType;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.codegen.impl.analizer.Graph;
import ru.ssau.graphplus.commons.*;
import ru.ssau.graphplus.events.*;
import ru.ssau.graphplus.events.EventListener;
import ru.ssau.graphplus.link.LinkBase;
import ru.ssau.graphplus.link.LinkOneConnectorBase;
import ru.ssau.graphplus.link.LinkTwoConnectorsAndTextBase;
import ru.ssau.graphplus.link.LinkFactory;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.node.NodeBase;
import ru.ssau.graphplus.node.NodeFactory;
import ru.ssau.graphplus.codegen.impl.recognition.DiagramTypeRecognitionImpl;
import ru.ssau.graphplus.validation.Validatable;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.*;


public class DiagramModel implements ru.ssau.graphplus.api.DiagramModel, Serializable, Validatable {

    private static final long serialVersionUID = 1L;
    private transient static List<WeakReference<DiagramModel>> instances = new ArrayList<>();
    private Set<DiagramElement> diagramElements;
    private transient Map<XShape, DiagramElement> shapeToDiagramElementMap;
    private transient Map<XConnectorShape, ConnectedShapes> connectedShapes;
    private transient Map<String, XShape> idToShape;
    private DiagramType diagramType;

    private String name;

    private transient XComponent xDrawDoc;
    private Map<String, DiagramElement> idToDiagramElement;
    private Graph graph;
    private Map<Class<Event>, List<EventListener>> eventListeners = new WeakHashMap<>();

    public DiagramModel(XComponent xDrawDoc) {
        instances.add(new WeakReference<DiagramModel>(this));
        diagramElements = new HashSet<>();
        shapeToDiagramElementMap = new HashMap();
        connectedShapes = new HashMap();
        idToShape = new HashMap();
        idToDiagramElement = new HashMap<>();


        this.xDrawDoc = xDrawDoc;
    }

    public void init(NodeFactory nodeFactory, LinkFactory linkFactory) {

        try {

            XShapes shapes = QI.XShapes(DrawHelper.getCurrentDrawPage(xDrawDoc));

            Set<XShape> set = Sets.newHashSet();

            // fill set
            for (int i = 0; i < shapes.getCount(); i++) {
                Object byIndex = shapes.getByIndex(i);
                XShape shape = QI.XShape(byIndex);
                set.add(shape);
            }

            DiagramWalker diagramWalker = new DiagramWalker(new ShapeHelperWrapperImpl(new MiscHelperWrapperImpl()), new UnoRuntimeWrapperImpl(), linkFactory, nodeFactory);
            DiagramType recognise = new DiagramTypeRecognitionImpl().recognise(set);
            diagramWalker.setDiagramType(recognise);
            Graph graph1 = diagramWalker.walk(set);

            for (Node node : graph1.getNodes()){
                addDiagramElement(node);
            }

            for (Link link : graph1.getLinks()){
                addDiagramElement(link);
            }

        } catch (IndexOutOfBoundsException | WrappedTargetException  e) {
            e.printStackTrace();
        }

    }

    public static List<WeakReference<DiagramModel>> getInstances() {
        return Collections.unmodifiableList(instances);
    }

    public XComponent getDrawDoc() {
        return xDrawDoc;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public DiagramType getDiagramType() {
        return diagramType;
    }

    public void setDiagramType(DiagramType diagramType) {
        this.diagramType = diagramType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<XConnectorShape, ConnectedShapes> getConnectedShapes() {
        return connectedShapes;
    }

    @Override
    public Collection<DiagramElement> getDiagramElements() {
        return diagramElements;
    }

    public DiagramElement getDiagramElementByShape(XShape xS) {

        DiagramElement diagramElement = shapeToDiagramElementMap.get(xS);
        if (diagramElement == null) {
            String id = MiscHelper.getId(xS);
            if (id != null) {
                DiagramElement diagramElement1 = idToDiagramElement.get(id);
                if (diagramElement1 != null) return diagramElement1;
            } else {

            }
        }
        return diagramElement;
    }

    public void addEventListener(Class<? extends Event> event, EventListener eventListener) {
        if (!eventListeners.containsKey(event)) {
            eventListeners.put((Class<Event>) event, new ArrayList<EventListener>());
        }
        eventListeners.get(event).add(eventListener);
    }

    void fireEvent(Event event) {

        List<EventListener> eventListeners1 = eventListeners.get(event.getClass());
        if (eventListeners1 == null) {
            return;
        }
        for (EventListener eventListener : eventListeners1) {
            eventListener.onEvent(event);
        }
    }

    public DiagramModel addDiagramElement(DiagramElement de) {

        diagramElements.add(de);

        if (de instanceof LinkBase) {

            if (de instanceof LinkTwoConnectorsAndTextBase) {
                LinkTwoConnectorsAndTextBase link = (LinkTwoConnectorsAndTextBase) de;
                XShape connShape1 = link.getConnShape1();
                shapeToDiagramElementMap.put(connShape1, de);
                XShape connShape2 = link.getConnShape2();
                shapeToDiagramElementMap.put(connShape2, de);
                XShape textShape = link.getTextShape();
                shapeToDiagramElementMap.put(textShape, de);

            } else if (de instanceof LinkOneConnectorBase) {
                LinkOneConnectorBase link = (LinkOneConnectorBase) de;
                XShape connShape1 = link.getConnShape();
                shapeToDiagramElementMap.put(connShape1, de);
            }

        } else if (de instanceof NodeBase) {
            NodeBase node = (NodeBase) de;
            shapeToDiagramElementMap.put(node.getShape(), de);
            String id = node.getId();
            idToShape.put(id, node.getShape());
            idToDiagramElement.put(id, node);
            fireEvent(new NodeAddedEvent(node));
        }
        return this;
    }

    public void removeDiagramElement(DiagramElement de) {
        if (de instanceof LinkTwoConnectorsAndTextBase) {
            LinkTwoConnectorsAndTextBase link = (LinkTwoConnectorsAndTextBase) de;
            shapeToDiagramElementMap.remove(link.getConnShape1());
            shapeToDiagramElementMap.remove(link.getConnShape2());
            shapeToDiagramElementMap.remove(link.getTextShape());

            connectedShapes.remove(QI.XConnectorShape(link.getConnShape1()));
            connectedShapes.remove(QI.XConnectorShape(link.getConnShape2()));
            fireEvent(new LinkRemovedEvent(link));
        }
        if (de instanceof Node) {
            Node node = (Node) de;
            fireEvent(new NodeRemovedEvent(node));
        }

        diagramElements.remove(de);
    }

    /**
     * @deprecated replaced by {@link #getDiagramElementByShape}
     */
    @Deprecated
    public Map<XShape, DiagramElement> getShapeToDiagramElementMap() {
        return shapeToDiagramElementMap;
    }

    public boolean diagramElementWithShapeExists(XShape xShape) {
        return getDiagramElementByShape(xShape) != null;
    }


    @Override
    public boolean isValid() {
        for (DiagramElement diagramElement : diagramElements) {
            if (diagramElement instanceof Validatable) {
                Validatable validatable = (Validatable) diagramElement;
                if (!validatable.isValid()) return false;
            }
        }

        return true;
    }

    public void refreshModel() {
        for (DiagramElement diagramElement : diagramElements) {
            if (diagramElement instanceof Refreshable)
                ((Refreshable) diagramElement).refresh(this);
        }
    }

    @Override
    public Collection<Node> getNodes() {


        return Lists.newArrayList(
                Iterables.<DiagramElement, Node>transform(Iterables.filter(diagramElements, new Predicate<DiagramElement>() {
                    @Override
                    public boolean apply(DiagramElement input) {
                        boolean b = input instanceof Node;
                        return b;
                    }
                }), new Function<DiagramElement, Node>() {
                    @Override
                    public Node apply(DiagramElement input) {
                        return (Node) input;
                    }
                })
        );
    }

    @Override
    public Collection<Link> getLinks() {
        return Lists.newArrayList(
                Iterables.<DiagramElement, Link>transform(Iterables.filter(diagramElements, new Predicate<DiagramElement>() {
                    @Override
                    public boolean apply(DiagramElement input) {
                        boolean b = input instanceof Link;
                        return b;
                    }
                }), new Function<DiagramElement, Link>() {
                    @Override
                    public Link apply(DiagramElement input) {
                        return (Link) input;
                    }
                })
        );
    }


}
