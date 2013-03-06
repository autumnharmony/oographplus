/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus;

import com.sun.star.drawing.XShape;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.ssau.graphplus.link.Link;

/**
 *
 * @author Антон
 */
public class Model {
    List<DiagramElement> diagramElements;
    Map<XShape, DiagramElement> map; 
    public Model() {
        diagramElements = new ArrayList<DiagramElement>();
        map = new HashMap<XShape, DiagramElement>();
    }

    public Model(List<DiagramElement> diagramElements) {
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
