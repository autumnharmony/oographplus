package ru.ssau.graphplus;

import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import ru.ssau.graphplus.link.Link;
import ru.ssau.graphplus.link.LinkFactory;
import ru.ssau.graphplus.link.Validatable;
import ru.ssau.graphplus.node.Node;
import ru.ssau.graphplus.node.NodeFactory;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

import static ru.ssau.graphplus.Misc.*;


public class DiagramModel implements Serializable, Validatable {

    private static final long serialVersionUID = 1L;
    Collection<DiagramElement> diagramElements;
    Map<String, String> info;
    Map<String, String> nameToIdMap;
    private transient Map<XShape, DiagramElement> shapeToDiagramElementMap;
    private transient Map<XConnectorShape, ConnectedShapes> connectedShapes;
    private transient Map<String, XShape> idToShape;
    private DiagramType diagramType;
    private String name;
    transient private Map<XShape, Pair<XShape, XShape>> connShapeToShape;
    private boolean restored;

    public DiagramModel() {
        diagramElements = new ArrayList<>();
        shapeToDiagramElementMap = new HashMap<>();
        connectedShapes = new HashMap<>();
        idToShape = new HashMap<>();
        nameToIdMap = new HashMap<>();

        info = new HashMap<>();

    }

    public DiagramType getDiagramType() {
        return diagramType;
    }

    public void setDiagramType(String diagramType) {
        Logger.getAnonymousLogger().info("setDiagramType");
        this.diagramType = DiagramType.valueOf(diagramType);
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

    public Collection<DiagramElement> getDiagramElements() {
        return diagramElements;
    }

    public boolean isRestored() {
        return restored;
    }

    public DiagramElement getDiagramELementByShape(XShape xS) {
        return shapeToDiagramElementMap.get(xS);
    }

    public void addDiagramElement(DiagramElement de) {
        diagramElements.add(de);

        if (de instanceof Link) {
            Link link = (Link) de;
            XShape connShape1 = link.getConnShape1();
            shapeToDiagramElementMap.put(connShape1, de);
            XShape connShape2 = link.getConnShape2();
            shapeToDiagramElementMap.put(connShape2, de);
            XShape textShape = link.getTextShape();
            shapeToDiagramElementMap.put(textShape, de);

            LinkFactory.setId(textShape, link);
            LinkFactory.setId(connShape1, link);
            LinkFactory.setId(connShape2, link);

            idToShape.put(Misc.getId(textShape), textShape);
            idToShape.put(Misc.getId(connShape1), connShape1);
            idToShape.put(Misc.getId(connShape2), connShape2);

        } else if (de instanceof Node) {
            Node node = (Node) de;
            shapeToDiagramElementMap.put(node.getShape(), de);
//            idToShape.put(Misc.getId(node.getShape()), node.getShape());
        }
    }

    public void removeDiagramElement(DiagramElement de) {
        if (de instanceof Link) {
            Link link = (Link) de;
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

    public Map<XShape, DiagramElement> getShapeToDiagramElementMap() {
        return shapeToDiagramElementMap;
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

    public boolean remap(XComponent xDrawDoc) {
        boolean mapped = false;
        if (idToShape == null) {
            idToShape = new HashMap<>();
        }
        XDrawPage xDrawPage = DrawHelper.getCurrentDrawPage(xDrawDoc);
        if (xDrawPage != null) {
            mapped = true;
            for (int i = 0; i < xDrawPage.getCount(); i++) {


                try {

                    Object byIndex = null;
                    byIndex = xDrawPage.getByIndex(i);
                    XShape xShape = QI.XShape(byIndex);

                    String id = Misc.getId(xShape);
                    if (id != null) {
                        idToShape.put(id, xShape);

                    }
                } catch (com.sun.star.lang.IndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (WrappedTargetException e) {
                    e.printStackTrace();
                }

            }

            for (DiagramElement diagramElement : diagramElements) {
                if (diagramElement instanceof Link) {
                    Link link = (Link) diagramElement;
                    XShape connShape1 = idToShape.get(link.getId() + "/conn1");
                    XShape connShape2 = idToShape.get(link.getId() + "/conn2");
                    XShape textShape = idToShape.get(link.getId() + "/text");
                    link.setProps(connShape1, connShape2, textShape);
                }
                if (diagramElement instanceof Node) {

                }
            }
        }
        return mapped;
    }

    void refreshLinksShapesId() {
        for (DiagramElement diagramElement : diagramElements) {
            if (diagramElement instanceof Link) {
                Link link = (Link) diagramElement;
                refreshLinkShapeId(link.getConnShape1(), link);
                refreshLinkShapeId(link.getConnShape2(), link);
                refreshLinkShapeId(link.getTextShape(), link);
            }
        }
    }

    void refreshLinkShapeId(XShape xShape, Link link) {
        String linkName = link.getName();

        String id = Misc.getId(xShape);
        Misc.setId(xShape, linkName + id.substring(id.indexOf('/')));
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
        List<DiagramElement> invalid = new ArrayList<>();
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

            diagramElement.refresh(this);
        }
    }

    public void refreshModel(XDrawPage xDrawPage) {
        List<DiagramElement> notActual = new ArrayList<>();
        for (DiagramElement diagramElement : diagramElements) {
            if (isActual(diagramElement, xDrawPage)) {
                diagramElement.refresh(this);
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
        Collection<Node> nodes = new ArrayList<>();
        for (XShape xShape : shapes) {

            DiagramElement diagramELementByShape = getDiagramELementByShape(xShape);
            if (diagramELementByShape instanceof Node) {
                nodes.add((Node) diagramELementByShape);
            }
        }
        return nodes;
    }

    public Collection<Link> getLinks(Collection<XShape> shapes) {
        Collection<Link> links = new ArrayList<>();
        for (XShape xShape : shapes) {

            DiagramElement diagramELementByShape = getDiagramELementByShape(xShape);
            if (diagramELementByShape instanceof Link) {
                links.add((Link) diagramELementByShape);
            }
        }
        return links;
    }

    public void restore(XDrawPage xDrawPage, XMultiServiceFactory xMultiServiceFactory, XComponent xComponent) {

        if (!restored) {
            Map<String, XShape> map = new HashMap<>();

            Map<String, Collection<XShape>> links = new HashMap<>();
            Map<String, Node> nodes = new HashMap<>();

            Map<XShape, Node> shapeToNode = new HashMap<>();

            NodeFactory nodeFactory = new NodeFactory(xMultiServiceFactory);
            LinkFactory linkFactory = new LinkFactory(xMultiServiceFactory);

            for (int i = 0; i < xDrawPage.getCount(); i++) {

                try {
                    Object byIndex = xDrawPage.getByIndex(i);
                    XShape xShape = QI.XShape(byIndex);

                    if (isDiagramElement(xShape)) {

                        String id = Misc.getId(xShape);


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

                            Misc.getLinkType(xShape);
                        }
                        map.put(id, xShape);
                    }

                } catch (com.sun.star.lang.IndexOutOfBoundsException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (WrappedTargetException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }


            for (Map.Entry<String, Collection<XShape>> entry : links.entrySet()) {
                String key = entry.getKey();

                Link prototype = linkFactory.createPrototype(Link.LinkType.valueOf(getLinkType((XShape) entry.getValue().toArray()[0])));
                XShape conn1 = null;
                XShape conn2 = null;
                XShape text = null;
                for (XShape xShape : entry.getValue()) {
                    String prefix = getPrefix(Misc.getId(xShape));
                    String suffix = getSuffix(Misc.getId(xShape));

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

                XShape startShapeStatic = Link.getStartShapeStatic(conn1);
                if (shapeToNode.containsKey(startShapeStatic)) {
                    Node node = shapeToNode.get(startShapeStatic);
                    {
                        prototype.setNode1(node);
                    }
                }

                XShape endShapeStatic = Link.getEndShapeStatic(conn2);
                if (shapeToNode.containsKey(startShapeStatic)) {
                    Node node = shapeToNode.get(endShapeStatic);
                    {
                        prototype.setNode2(node);
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


}
