
package ru.ssau.graphplus.node;


import com.sun.star.drawing.XShape;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import ru.ssau.graphplus.DiagramElementFactory;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.ssau.graphplus.api.Node.NodeType;

/**
 * @author Borisov Anton
 */
public class NodeFactory extends DiagramElementFactory {


    private static Set<NodeFactory> nodeFactorySet = Collections.newSetFromMap(new WeakHashMap<NodeFactory, Boolean>());

    public static NodeFactory getFactory(Object o){
        return null;
    }

    private static final String NODE_PREFIX = "node";


    public NodeFactory(XMultiServiceFactory xmsf) {
        super(xmsf);
        nodeFactorySet.add(this);
    }


    public NodeBase create(NodeType type, XComponent xComponent) {
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
