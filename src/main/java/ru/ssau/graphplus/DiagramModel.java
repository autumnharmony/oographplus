/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus;

import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.link.Link;

import java.util.*;


public class DiagramModel {


    Collection<DiagramElement> diagramElements;
    Map<XShape, DiagramElement> shapeToDiagramElementMap;

    Map<XConnectorShape, ConnectedShapes> connectedShapes;

    public Map<XConnectorShape, ConnectedShapes> getConnectedShapes() {
        return connectedShapes;
    }

    public DiagramModel() {
        diagramElements = new ArrayList<DiagramElement>();
        shapeToDiagramElementMap = new HashMap<XShape, DiagramElement>();
        connectedShapes = new HashMap<>();
    }

    public DiagramModel(List<DiagramElement> diagramElements) {
        this.diagramElements = diagramElements;
    }
    
    public void addDiagramElement(DiagramElement de){
        diagramElements.add(de);
        
        if (de instanceof Link){
            Link link = (Link) de;
            shapeToDiagramElementMap.put(link.getConnShape1(), de);
            shapeToDiagramElementMap.put(link.getConnShape2(), de);
            shapeToDiagramElementMap.put(link.getTextShape(), de);
        }
    }

    public Map<XShape, DiagramElement> getShapeToDiagramElementMap() {
        return shapeToDiagramElementMap;
    }

    class Pair<F,S> {
       F first;
       S second;

        Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }
    }
    private Map<XShape,Pair<XShape,XShape>> connShapeToShape;
    enum StartEnd {
        StartShape,
        EndShape
    }

    public void setConnShapeToShapeLink(XShape connShape, StartEnd startEnd, XShape shape) {
        if (connShapeToShape.containsKey(connShape)){
            Pair<XShape, XShape> stringXShapePair = connShapeToShape.get(connShape);

            if (startEnd.equals(StartEnd.StartShape)){
                stringXShapePair.first = shape;
            }
            if (startEnd.equals(StartEnd.EndShape)){
                stringXShapePair.second = shape;
            }

        } else {
            if (startEnd.equals(StartEnd.StartShape)){
                connShapeToShape.put(connShape, new Pair<XShape, XShape>(shape, null));
            }
            if (startEnd.equals(StartEnd.EndShape)){
                connShapeToShape.put(connShape, new Pair<XShape, XShape>(null, shape));
            }

        }
    }

    public XShape getConnShapeToShapeLink(XShape connShape, StartEnd startEnd ) {
        if (connShapeToShape.containsKey(connShape)){
            Pair<XShape, XShape> stringXShapePair = connShapeToShape.get(connShape);
            if (startEnd.equals(StartEnd.StartShape)){
                return stringXShapePair.first;
            }
            else if (startEnd.equals(StartEnd.EndShape)) {
                return stringXShapePair.second;
            }
        } else {
            return  null;
            //connShapeToShape.put(connShape, new Pair<String, XShape>(startOrEnd, shape));
        }

        return null;
    }


    
    
}
