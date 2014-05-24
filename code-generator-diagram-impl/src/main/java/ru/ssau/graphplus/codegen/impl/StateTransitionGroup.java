package ru.ssau.graphplus.codegen.impl;

import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.codegen.impl.recognition.LinkTypeRecogniser;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;

import java.util.List;


public class StateTransitionGroup extends LinkCodeBase implements GroupingCode<StateTransitionCode>{

    List<StateTransitionCode> stateTransitionCode;

    public void setGroup(List<StateTransitionCode> stateTransitionCode) {
        this.stateTransitionCode = stateTransitionCode;
    }

    public StateTransitionGroup(ConnectedShapesComplex connectedShapesComplex, LinkTypeRecogniser linkTypeRecogniser, ShapeHelperWrapper shapeHelper) {
        super(connectedShapesComplex, linkTypeRecogniser, shapeHelper);
//        throw new UnsupportedOperationException();
    }

    @Override
    public String getCode() {
        StringBuffer buffer = new StringBuffer();

        XShape from = stateTransitionCode.get(0).from();

        buffer.append(type(from).equals(Node.NodeType.StartMethodOfProcess) ? "+" : "");
        buffer.append(text(from));
        buffer.append(type(from).equals(Node.NodeType.ClientPort) ? "!" : "");
        buffer.append(type(from).equals(Node.NodeType.ServerPort) ? "?" : "");


//        buffer.append(type(from).equals(Node.NodeType.StartMethodOfProcess) ? "!" : "");




        int i = 0;
        for (StateTransitionCode stateTransitionCode1 : stateTransitionCode){
            if (i!=0) buffer.append(" | ");
            XShape to = stateTransitionCode1.to();
            buffer.append(type(to).equals(Node.NodeType.ServerPort) ? "?" : "!");

            buffer.append(stateTransitionCode1.linkText());
            buffer.append(" -> ");
            buffer.append(text(stateTransitionCode1.to()));
            i++;
        }
//        buffer.append(text(textShape()));
//        buffer.append(" -> ");
//        buffer.append(text(to()));
//        buffer.append(type(from));
        buffer.append(";");
        return buffer.toString();
    }
}
