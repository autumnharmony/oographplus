/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;


import ru.ssau.graphplus.analizer.DiagramWalker;
import ru.ssau.graphplus.api.DiagramElement;
import ru.ssau.graphplus.api.DiagramModel;
import ru.ssau.graphplus.api.DiagramType;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.recognition.DiagramTypeRecognition;
import ru.ssau.graphplus.recognition.DiagramTypeRecognitionImpl;

public class DiagramCodeGenerator implements CodeGenerator {

    @Override
    public String generateCode(CodeSource codeSource) {

        if (!(codeSource instanceof DiagramCodeSource)){
            throw new IllegalArgumentException("This implementation of CodeGenerator only takes ru.ssau.graphplus.DiagramCodeSource");
        }

        DiagramCodeSource diagramCodeSource = (DiagramCodeSource) codeSource;

        ru.ssau.graphplus.api.DiagramModel diagramModel = diagramCodeSource.getDiagramModel();

//        if (recognise.equals(DiagramType.StartMethodOfProcess)){
//
//            for (Node node : diagramModel.getNodes()){
//                node.getType();
//            }
//        }





        // TODO
         return "Generated Code";
    }

//
//    private
}
