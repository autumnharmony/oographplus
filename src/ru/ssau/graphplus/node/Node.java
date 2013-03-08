/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus.node;

import ru.ssau.graphplus.DiagramElement;
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

    Collection<Link>    linkCollection;



    public XShape buildShape(XMultiServiceFactory xMSF, XDrawPage xDP) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public XShape buildShape(XMultiServiceFactory xMSF, XDrawPage xDP, XComponent xDrawDoc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public XShape buildShape(XMultiServiceFactory xMSF) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<XShape> buildShapes(XMultiServiceFactory xMSF, XDrawPage xDP, XComponent xDrawDoc) {
        throw new UnsupportedOperationException("Not supported yet.");
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
    
//    private static void testStub(){
//        System.out.println(typeDescMap.get(type.toString()));
//    }
//    
//    public static void main(String[] args){
//        testStub();
//    }
    
}
