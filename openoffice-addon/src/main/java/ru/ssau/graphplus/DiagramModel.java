package ru.ssau.graphplus;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XDrawPages;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.*;
import com.sun.star.uno.UnoRuntime;
import ru.ssau.graphplus.api.DiagramElement;
import ru.ssau.graphplus.api.DiagramType;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.link.LinkBase;
import ru.ssau.graphplus.link.LinkFactory;
import ru.ssau.graphplus.link.LinkHelper;
import ru.ssau.graphplus.link.Validatable;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.node.NodeBase;
import ru.ssau.graphplus.node.NodeFactory;
import ru.ssau.graphplus.QI;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.*;

import static ru.ssau.graphplus.MiscHelper.*;


public class DiagramModel implements ru.ssau.graphplus.api.DiagramModel, Serializable, Validatable {

    private static final long serialVersionUID = 1L;
    private Set<DiagramElement> diagramElements;
    private Map<String, String> info;
    private Map<String, String> nameToIdMap;
    private transient BiMap<XShape,DiagramElement> shapeDiagramElementBiMap;
    private transient Map<XShape, DiagramElement> shapeToDiagramElementMap;
    private transient Map<XConnectorShape, ConnectedShapes> connectedShapes;
    private transient Map<String, XShape> idToShape;
    private DiagramType diagramType;
    private String name;

    private BiMap<String,String> idNameBiMap;

    transient private Map<XShape, Pair<XShape, XShape>> connShapeToShape;
    private boolean restored;
    private transient XComponent xDrawDoc;

    private transient static List<WeakReference<DiagramModel>> instances = new ArrayList<>();

    public static List<WeakReference<DiagramModel>> getInstances() {
        return Collections.unmodifiableList(instances);
    }

    public XComponent getDrawDoc() {
        return xDrawDoc;
    }

    public DiagramModel(XComponent xDrawDoc) {
        instances.add(new WeakReference<DiagramModel>(this));
        diagramElements = new HashSet<>();
        shapeToDiagramElementMap = new HashMap();
        connectedShapes = new HashMap();
        idToShape = new HashMap();
        nameToIdMap = new HashMap();

        info = new HashMap();
        this.xDrawDoc = xDrawDoc;

        shapeDiagramElementBiMap =  HashBiMap.create();
        idNameBiMap = HashBiMap.create();
    }

    public DiagramType getDiagramType() {
        return diagramType;
    }

    public void setDiagramType(String diagramType) {
        OOGraph.LOGGER.info("setDiagramType");
        this.diagramType = DiagramType.valueOf(diagramType);
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

    public boolean isRestored() {
        return restored;
    }

    public DiagramElement getDiagramElementByShape(XShape xS) {
        return shapeToDiagramElementMap.get(xS);
    }

    public DiagramModel addDiagramElement(DiagramElement de) {

        diagramElements.add(de);

        if (de instanceof LinkBase) {
            LinkBase link = (LinkBase) de;
            XShape connShape1 = link.getConnShape1();
            shapeToDiagramElementMap.put(connShape1, de);
            XShape connShape2 = link.getConnShape2();
            shapeToDiagramElementMap.put(connShape2, de);
            XShape textShape = link.getTextShape();
            shapeToDiagramElementMap.put(textShape, de);

            setLinkId(link, connShape1, connShape2, textShape);

            idToShape.put(getId(textShape), textShape);
            idToShape.put(getId(connShape1), connShape1);
            idToShape.put(getId(connShape2), connShape2);

        } else if (de instanceof NodeBase) {
            NodeBase node = (NodeBase) de;
            shapeToDiagramElementMap.put(node.getShape(), de);
            idToShape.put(node.getId(), node.getShape());

            idNameBiMap.put(node.getId(),node.getName());
        }
        return this;
    }

    void setLinkId(Link link, XShape connShape1, XShape connShape2, XShape textShape) {
        LinkFactory.setId(link, connShape1, connShape2, textShape);
    }


    public void removeDiagramElement(DiagramElement de) {
        if (de instanceof LinkBase) {
            LinkBase link = (LinkBase) de;
            shapeToDiagramElementMap.remove(link.getConnShape1());
            shapeToDiagramElementMap.remove(link.getConnShape2());
            shapeToDiagramElementMap.remove(link.getTextShape());

            connectedShapes.remove(QI.XConnectorShape(link.getConnShape1()));
            connectedShapes.remove(QI.XConnectorShape(link.getConnShape2()));
        }
        if (de instanceof Node) {
            Node node = (Node) de;

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

    public void setConnShapeToShapeLink(XShape connShape, StartEnd startEnd, XShape shape) {
        if (connShapeToShape.containsKey(connShape)) {
            Pair<XShape, XShape> stringXShapePair = connShapeToShape.get(connShape);

            if (startEnd.equals(StartEnd.StartShape)) {
                stringXShapePair.first = shape;
            }
            if (startEnd.equals(StartEnd.EndShape)) {
                stringXShapePair.second = shape;
            }

        } else {
            if (startEnd.equals(StartEnd.StartShape)) {
                connShapeToShape.put(connShape, new Pair<XShape, XShape>(shape, null));
            }
            if (startEnd.equals(StartEnd.EndShape)) {
                connShapeToShape.put(connShape, new Pair<XShape, XShape>(null, shape));
            }

        }
    }

    public XShape getConnShapeToShapeLink(XShape connShape, StartEnd startEnd) {
        if (connShapeToShape.containsKey(connShape)) {
            Pair<XShape, XShape> stringXShapePair = connShapeToShape.get(connShape);
            if (startEnd.equals(StartEnd.StartShape)) {
                return stringXShapePair.first;
            } else if (startEnd.equals(StartEnd.EndShape)) {
                return stringXShapePair.second;
            }
        } else {
            return null;
            //connShapeToShape.put(connShape, new Pair<String, XShape>(startOrEnd, shape));
        }

        return null;
    }


    /**
     * remapping of this DiagramModel to new document
     *
     * @param xDrawDoc draw document
     * @return
     */
    public boolean remap(XComponent xDrawDoc) {
        this.xDrawDoc = xDrawDoc;
        boolean mapped = false;
        if (idToShape == null) {
            idToShape = new HashMap();
        }

        XDrawPages xDrawPages = getDrawPages(xDrawDoc);


        for (int j = 0; j < xDrawPages.getCount(); j++) {
            try {

                Object byIndex1 = xDrawPages.getByIndex(j);
                XDrawPage xDrawPage = UnoRuntime.queryInterface(XDrawPage.class, byIndex1);

                if (xDrawPage != null) {
                    mapped = true;
                    for (int i = 0; i < xDrawPage.getCount(); i++) {


                        try {

                            Object byIndex = null;
                            byIndex = xDrawPage.getByIndex(i);
                            XShape xShape = QI.XShape(byIndex);

                            // TODO there null !!!
                            String id = getId(xShape);
                            if (id != null) {
                                idToShape.put(id, xShape);
                            }
                        } catch (com.sun.star.lang.IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        } catch (WrappedTargetException e) {
                            e.printStackTrace();
                        }

                    }

                    if (shapeToDiagramElementMap == null) {
                        shapeToDiagramElementMap = new HashMap<>();
                    }

                    for (DiagramElement diagramElement : diagramElements) {
                        if (diagramElement instanceof LinkBase) {
                            LinkBase link = (LinkBase) diagramElement;
                            XShape connShape1 = idToShape.get(link.getId() + "/conn1");
                            XShape connShape2 = idToShape.get(link.getId() + "/conn2");
                            XShape textShape = idToShape.get(link.getId() + "/text");
                            link.setProps(connShape1, connShape2, textShape);
                        }
                        if (diagramElement instanceof NodeBase) {
                            NodeBase node = (NodeBase) diagramElement;
                            XShape xShape = idToShape.get(node.getId());
                            node.setProps(xShape);
                            shapeToDiagramElementMap.put(xShape, node);
                        }
                    }
                }
            } catch (com.sun.star.lang.IndexOutOfBoundsException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (WrappedTargetException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return mapped;
    }


    // override this in test
    String getId(XShape xShape) {
        return MiscHelper.getId(xShape);
    }


    // override this in test
    XDrawPages getDrawPages(XComponent xDrawDoc) {
        return DrawHelper.drawDoc_getXDrawPages(xDrawDoc);
    }

    void refreshLinksShapesId() {
        for (DiagramElement diagramElement : diagramElements) {
            if (diagramElement instanceof LinkBase) {
                LinkBase link = (LinkBase) diagramElement;
                refreshLinkShapeId(link.getConnShape1(), link);
                refreshLinkShapeId(link.getConnShape2(), link);
                refreshLinkShapeId(link.getTextShape(), link);
            }
        }
    }

    void refreshLinkShapeId(XShape xShape, Link link) {
        String linkName = link.getName();

        String id = getId(xShape);
        MiscHelper.setId(xShape, linkName + id.substring(id.indexOf('/')));
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

    public List<DiagramElement> getInvalid() {
        List<DiagramElement> invalid = new ArrayList();
        for (DiagramElement diagramElement : diagramElements) {
            if (diagramElement instanceof Validatable) {
                Validatable validatable = (Validatable) diagramElement;
                if (!validatable.isValid()) invalid.add(diagramElement);
            }
        }
        return invalid;
    }

    public void refreshNodesShapeId() {

    }

    public void refreshModel() {
        for (DiagramElement diagramElement : diagramElements) {
            if (diagramElement instanceof Refreshable)
                ((Refreshable)diagramElement).refresh(this);
        }
    }

    public void refreshModel(XDrawPage xDrawPage) {
        List<DiagramElement> notActual = new ArrayList();
        for (DiagramElement diagramElement : diagramElements) {
            if (!isActual(diagramElement, xDrawPage)) {
                if (diagramElement instanceof Refreshable){
                    Refreshable refreshable = (Refreshable) diagramElement;
                    refreshable.refresh(this);
                }
            } else {
                notActual.add(diagramElement);
            }
        }

        for (DiagramElement diagramElement : notActual) {
            removeDiagramElement(diagramElement);
        }
    }

    public boolean isActual(DiagramElement diagramElement, XDrawPage xDrawPage) {


        return true;
    }

    public Collection<Node> getNodes(Collection<XShape> shapes) {
        Collection<Node> nodes = new ArrayList();
        for (XShape xShape : shapes) {

            DiagramElement diagramELementByShape = getDiagramElementByShape(xShape);
            if (diagramELementByShape instanceof Node) {
                nodes.add((Node) diagramELementByShape);
            }
        }
        return nodes;
    }

    @Override
    public Collection<Node> getNodes() {


        return Lists.newArrayList(
                Iterables.<DiagramElement, Node>transform(Iterables.filter(diagramElements, new Predicate<DiagramElement>() {
                    @Override
                    public boolean apply(DiagramElement input) {
                        boolean b = input instanceof Link;
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
    public Collection<Link> getLinks(){
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

    public Collection<Link> getLinks(Collection<XShape> shapes) {
        Collection<Link> links = new ArrayList();
        for (XShape xShape : shapes) {

            DiagramElement diagramELementByShape = getDiagramElementByShape(xShape);
            if (diagramELementByShape instanceof Link) {
                links.add((Link) diagramELementByShape);
            }
        }
        return links;
    }

    public void restore(XDrawPage xDrawPage, XMultiServiceFactory xMultiServiceFactory, XComponent xComponent) {

        if (!restored) {
            Map<String, XShape> map = new HashMap();

            Map<String, Collection<XShape>> links = new HashMap();
            Map<String, Node> nodes = new HashMap();

            Map<XShape, Node> shapeToNode = new HashMap();

            NodeFactory nodeFactory = new NodeFactory(xMultiServiceFactory);
            LinkFactory linkFactory = new LinkFactory(xMultiServiceFactory);

            for (int i = 0; i < xDrawPage.getCount(); i++) {

                try {
                    Object byIndex = xDrawPage.getByIndex(i);
                    XShape xShape = QI.XShape(byIndex);

                    if (isDiagramElement(xShape)) {

                        String id = getId(xShape);


                        if (isNode(xShape)) {
                            String nodeTypeString = getNodeType(xShape);
                            Node.NodeType nodeType = Node.NodeType.valueOf(nodeTypeString);
                            Node node = nodeFactory.create(xShape, nodeType);
                            addDiagramElement(node);
                            nodes.put(id, node);
                            shapeToNode.put(xShape, node);
                        } else if (isLink(xShape)) {
                            String prefix;
                            if (id.contains("/")) {
                                prefix = getPrefix(id);
                                if (!links.containsKey(prefix)) {
                                    links.put(prefix, new ArrayList<XShape>());
                                }
                                links.get(prefix).add(xShape);
                            }

                            MiscHelper.getLinkType(xShape);
                        }
                        map.put(id, xShape);
                    }

                } catch (com.sun.star.lang.IndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (WrappedTargetException e) {
                    e.printStackTrace();
                }
            }


            for (Map.Entry<String, Collection<XShape>> entry : links.entrySet()) {
                String key = entry.getKey();

                LinkBase prototype = (LinkBase) linkFactory.createPrototype(Link.LinkType.valueOf(getLinkType((XShape) entry.getValue().toArray()[0])));
                XShape conn1 = null;
                XShape conn2 = null;
                XShape text = null;
                for (XShape xShape : entry.getValue()) {
                    String prefix = getPrefix(getId(xShape));
                    String suffix = getSuffix(getId(xShape));

                    switch (suffix) {
                        case "/conn1":
                            conn1 = xShape;
                            break;

                        case "/conn2":
                            conn2 = xShape;
                            break;
                        case "/text":
                            text = xShape;
                            break;
                    }

                }
                prototype.setProps(conn1, conn2, text);
                addDiagramElement(prototype);

                XShape startShapeStatic = LinkHelper.getStartShapeStatic(conn1);
                if (shapeToNode.containsKey(startShapeStatic)) {
                    Node node = shapeToNode.get(startShapeStatic);
                    {
                        prototype.setStartNode(node);
                    }
                }

                XShape endShapeStatic = LinkHelper.getEndShapeStatic(conn2);
                if (shapeToNode.containsKey(startShapeStatic)) {
                    Node node = shapeToNode.get(endShapeStatic);
                    {
                        prototype.setEndNode(node);
                    }
                }
            }

            restored = true;
        }

    }

    private String getPrefix(String id) {
        String prefix;
        prefix = id.substring(0, id.indexOf('/'));
        return prefix;
    }

    private String getSuffix(String id) {
        String prefix;
        prefix = id.substring(id.indexOf('/'));
        return prefix;
    }

    enum StartEnd {
        StartShape,
        EndShape
    }

    class Pair<F, S> {
        F first;
        S second;

        Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }
    }

    public void buildModel(){

    }


}
