package ru.ssau.graphplus.node;

import com.google.inject.Inject;
import com.sun.star.container.XNamed;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;
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
            throw new RuntimeException(ex);
        } finally {
        }

    }

    public NodeBase create(NodeType type, XShape shape) {
        ShapeWrapper wrap = ShapeWrapper.wrap(shape);
        if (shapeNodeMap.containsKey(wrap) && shapeNodeMap.get(wrap).getType().equals(type)){
            return shapeNodeMap.get(wrap);
        }
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
            node.setName(node.getId());
            return node;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
        }
    }

    private Map<ShapeWrapper, NodeBase> shapeNodeMap = new HashMap<ShapeWrapper, NodeBase>();


    static class ShapeWrapper {

        private ShapeWrapper(XShape shape) {
            this.shape = shape;
        }

        public XShape shape;


        static ShapeWrapper wrap(XShape shape){
            return new ShapeWrapper(shape);
        }

        @Override
        public boolean equals(Object obj) {
            XShape shape1 = UnoRuntime.queryInterface(XShape.class, obj);
            if (shape1 == null){
                return false;
            }
            return UnoRuntime.areSame(this.shape, obj);
        }

        @Override
        public int hashCode() {
            return shape.hashCode();
        }
    }


    public Node create(XShape shape) {
        ShapeWrapper wrap = ShapeWrapper.wrap(shape);
        if (shapeNodeMap.containsKey(wrap)) {
            return shapeNodeMap.get(wrap);
        } else {
            NodeType nodeType = shapeHelper.getNodeType(shape);
            NodeBase nodeBase = create(nodeType, shape);
            nodeBase.setName(ShapeHelper.getText(QI.XShape(shape)));
            shapeNodeMap.put(ShapeWrapper.wrap(shape), nodeBase);
            return nodeBase;
        }
    }

    public Collection<Node> create(ConnectedShapesComplex connectedShapesComplex) {
        Node node1 = create(connectedShapesComplex.fromShape);
        Node node2 = create(connectedShapesComplex.toShape);
        return Arrays.asList(node1, node2);
    }
}
