/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus.node;

import ru.ssau.graphplus.DiagramElement;
import ru.ssau.graphplus.Misc;
import ru.ssau.graphplus.ShapeBuilder;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import ru.ssau.graphplus.link.Link;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author 1
 */
public abstract class Node implements ShapeBuilder, DiagramElement {

    public interface PostCreationAction {
        void postCreate(XShape shape);
    }

    public class DefaultPostCreationAction implements PostCreationAction {

        @Override
        public void postCreate(XShape shape) {
            Misc.tagShapeAsNode(xShape);
        }
    }

    Collection<Link>    linkCollection;

    protected PostCreationAction postCreationAction;

    protected Node(PostCreationAction postCreationAction) {
        this.postCreationAction = postCreationAction;
    }

    public XShape buildShape(XMultiServiceFactory xMSF, XDrawPage xDP) {
        throw new UnsupportedOperationException("Not overrided.");
    }

    public XShape buildShape(XMultiServiceFactory xMSF, XDrawPage xDP, XComponent xDrawDoc) {
        throw new UnsupportedOperationException("Not overrided yet.");
    }

    public XShape buildShape(XMultiServiceFactory xMSF) {
        throw new UnsupportedOperationException("Not overrided.");
    }

    public Collection<XShape> buildShapes(XMultiServiceFactory xMSF, XDrawPage xDP, XComponent xDrawDoc) {
        throw new UnsupportedOperationException("Not overrided.");
    }
    
    
    public enum NodeType {
        Client,
        Server,
        Process,
        Procedure
    }

    public Node() {
        
    }
    
    public Node(NodeType type){
        this.type = type;
    }
    
    
    
    protected XShape xShape;

    public XShape getShape() {
        return xShape;
    }

    public void setxShape(XShape xShape) {
        this.xShape = xShape;
    }
    
    public static Map<String,String> typeDescMap = new HashMap<String, String>(){
        {
            put("Client", "Client Node");
            put("Server", "Server Node");
            put("Process", "Process Node");
            put("Procedure","Procedure Node");
        }
    };
    
    NodeType type;

    public void runPostCreation(){
        if (postCreationAction != null) {
            postCreationAction.postCreate(getShape());
        }
    }
    
//    private static void testStub(){
//        System.out.println(typeDescMap.get(type.toString()));
//    }
//    
//    public static void main(String[] args){
//        testStub();
//    }
    
}
