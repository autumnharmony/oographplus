/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus;

import com.sun.star.drawing.XShape;

import java.util.*;

import ru.ssau.graphplus.link.Link;


public class DiagramModel {


    Collection<DiagramElement> diagramElements;
    Map<XShape, DiagramElement> map;


    public DiagramModel() {
        diagramElements = new ArrayList<DiagramElement>();
        map = new HashMap<XShape, DiagramElement>();
    }

    public DiagramModel(List<DiagramElement> diagramElements) {
        this.diagramElements = diagramElements;
    }
    
    public void addDiagramElement(DiagramElement de){
        diagramElements.add(de);
        
        if (de instanceof Link){
            Link link = (Link) de;
            map.put(link.getConnShape1(), de);
            map.put(link.getConnShape2(), de);
            map.put(link.getTextShape(), de);
        }
    }
    
    
}
