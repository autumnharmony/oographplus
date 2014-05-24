package ru.ssau.graphplus.node;


import com.google.inject.Inject;
import com.sun.star.container.XNamed;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import ru.ssau.graphplus.AbstractDiagramElementFactory;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.commons.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.ssau.graphplus.api.Node.NodeType;

/**
 * @author Borisov Anton
 */
public class NodeFactory extends AbstractDiagramElementFactory {

    private static final String NODE_PREFIX = "node";

    private ShapeHelperWrapper shapeHelper;

    @Inject
    public NodeFactory(XMultiServiceFactory xmsf, ShapeHelperWrapper shapeHelperWrapper) {
        super(xmsf);
        shapeHelper = shapeHelperWrapper;
    }


    public NodeBase create(NodeType type) {
        try {

            NodeBase node = null;
            switch (type) {
                case ClientPort:
                    node = new ClientNode(NODE_PREFIX + getCount());
                    break;
                case ServerPort:
                    node = new ServerNode(NODE_PREFIX + getCount());
                    break;
                case MethodOfProcess:
                    node = new ProcedureNode(NODE_PREFIX + getCount());
                    break;
                case StartMethodOfProcess:
                    node = new ProcessNode(NODE_PREFIX + getCount());
                    break;
                default:

            }
            XShape xShape = node.buildShape(xmsf);


            node.setShape(xShape);

            return node;
        } catch (Exception ex) {
            Logger.getLogger(NodeFactory.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
        }
        return null;
    }


    public NodeBase create(NodeType type, XShape shape) {
        try {

            NodeBase node = null;
            switch (type) {
                case ClientPort:
                    node = new ClientNode(NODE_PREFIX + getCount());
                    break;
                case ServerPort:
                    node = new ServerNode(NODE_PREFIX + getCount());
                    break;
                case MethodOfProcess:
                    node = new ProcedureNode(NODE_PREFIX + getCount());
                    break;
                case StartMethodOfProcess:
                    node = new ProcessNode(NODE_PREFIX + getCount());
                    break;
                default:

            }


            node.setShape(shape);

            return node;
        } catch (Exception ex) {
            Logger.getLogger(NodeFactory.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
        }
        return null;
    }

    private Map<XShape, Node> shapeNodeMap = new HashMap<XShape,Node>();


    private Node create(XShape shape) {
        if (shapeNodeMap.containsKey(shape)) {
            return shapeNodeMap.get(shape);
        } else {
            NodeType nodeType = shapeHelper.getNodeType(shape);
            NodeBase nodeBase = create(nodeType, shape);
            nodeBase.setName(ShapeHelper.getText(QI.XShape(shape)));
            return nodeBase;
        }

    }

    public Collection<Node> create(ConnectedShapesComplex connectedShapesComplex) {
        Node node1 = create(connectedShapesComplex.fromShape);
        Node node2 = create(connectedShapesComplex.toShape);
        return Arrays.asList(node1, node2);
    }


    public NodeBase create(XShape xShape, NodeType type) {


        NodeBase node = null;

        switch (type) {
            case ClientPort:
                node = new ClientNode(NODE_PREFIX + getCount());
                break;
            case ServerPort:
                node = new ServerNode(NODE_PREFIX + getCount());
                break;
            case MethodOfProcess:
                node = new ProcedureNode(NODE_PREFIX + getCount());
                break;
            case StartMethodOfProcess:
                node = new ProcessNode(NODE_PREFIX + getCount());
                break;
            default:

        }

        if (node != null)
            node.setShape(xShape);
        return node;
    }

//    public NodeBase process(){
//        return crea
//    }
//
//    public NodeBase client(){
//
//    }
//
//    public NodeBase procedure(){
//
//    }
//
//    public NodeBase server(){
//
//    }


//    public static NodeBase createAndInsert(NodeBase.NodeType type, XComponent xComponent, XShapes xShapes) {
//        try {
//            XShape xShape = null;
////            ShapeHelper.createShape(xComponent, new Point(0,0), new Size(300, 300), null);
//            xShape = ShapeHelper.createAndInsertShapeReturnXShape(xComponent, xShapes, new Point(0, 0), new Size(500, 500), node2shapeMap.get(type));
//            Misc.setNodeType(xShape, type);
//            Misc.tagShapeAsNode(xShape);
//            return new NodeBase(xShape, type);
//        } catch (Exception ex) {
//            Logger.getLogger(NodeFactory.class.getName()).log(Level.SEVERE, null, ex);
//
//        } finally {
//        }
//        return null;
//    }
}
