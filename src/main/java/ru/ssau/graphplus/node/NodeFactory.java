/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus.node;


import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import ru.ssau.graphplus.QI;
import ru.ssau.graphplus.node.Node.NodeType;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Borisov Anton
 */
public class NodeFactory {

    private XMultiServiceFactory xmsf;



    public NodeFactory(XMultiServiceFactory xmsf) {
        this.xmsf = xmsf;
    }


    static Map<NodeType, String> node2shapeMap = new EnumMap<Node.NodeType, String>(Node.NodeType.class) {
        {
            put(NodeType.Client, "com.sun.star.drawing.CustomShape");
            put(NodeType.Process, "com.sun.star.drawing.RectangleShape");
        }
    };

    public Node create(NodeType type, XComponent xComponent) {
        try {

            Node node = null;
            switch (type) {
                case Client:
                    node = new ClientNode();
                    break;
                case Server:
                    node = new ServerNode();
                    break;
                case Procedure:
                    node = new ProcedureNode();
                    break;
                case Process:
                    node = new ProcessNode();
                    break;
                default:

            }
            XShape xShape = node.buildShape(xmsf);


            XPropertySet xPropertySet = QI.XPropertySet(xShape);

            node.setShape(xShape);

            return node;
        } catch (Exception ex) {
            Logger.getLogger(NodeFactory.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
        }
        return null;
    }


    public Node create(NodeType type, XComponent xComponent, Node.PostCreationAction postCreationAction) {
        try {

            Node node = null;
            switch (type) {
                case Client:
                    node = new ClientNode(postCreationAction);
                    break;
                case Server:
                    node = new ServerNode();
                    break;
                case Procedure:
                    node = new ProcedureNode();
                    break;
                case Process:
                    node = new ProcessNode();
                    break;
                default:

            }
            XShape xShape = node.buildShape(xmsf);


            XPropertySet xPropertySet = QI.XPropertySet(xShape);

            node.setShape(xShape);

            return node;
        } catch (Exception ex) {
            Logger.getLogger(NodeFactory.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
        }
        return null;
    }


    public Node create(XShape xShape, NodeType type){


            Node node = null;
            switch (type) {
                case Client:
                    node = new ClientNode();
                    break;
                case Server:
                    node = new ServerNode();
                    break;
                case Procedure:
                    node = new ProcedureNode();
                    break;
                case Process:
                    node = new ProcessNode();
                    break;
                default:

            }

        if (node!=null)
        node.setShape(xShape);
            return node;
    }


//    public static Node createAndInsert(Node.NodeType type, XComponent xComponent, XShapes xShapes) {
//        try {
//            XShape xShape = null;
////            ShapeHelper.createShape(xComponent, new Point(0,0), new Size(300, 300), null);
//            xShape = ShapeHelper.createAndInsertShapeReturnXShape(xComponent, xShapes, new Point(0, 0), new Size(500, 500), node2shapeMap.get(type));
//            Misc.setNodeType(xShape, type);
//            Misc.tagShapeAsNode(xShape);
//            return new Node(xShape, type);
//        } catch (Exception ex) {
//            Logger.getLogger(NodeFactory.class.getName()).log(Level.SEVERE, null, ex);
//
//        } finally {
//        }
//        return null;
//    }
}
