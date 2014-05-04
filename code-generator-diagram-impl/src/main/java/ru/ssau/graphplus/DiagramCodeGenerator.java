/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;


import com.sun.star.drawing.XShapes;
import ru.ssau.graphplus.analizer.matches.Match;
import ru.ssau.graphplus.analizer.matches.MatchFactoryImpl;
import ru.ssau.graphplus.api.DiagramType;
import ru.ssau.graphplus.codegen.CodeGenerator;
import ru.ssau.graphplus.codegen.CodeProvider;
import ru.ssau.graphplus.codegen.CodeSource;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.MiscHelperWrapperImpl;
import ru.ssau.graphplus.commons.ShapeHelperWrapperImpl;
import ru.ssau.graphplus.recognition.LinkTypeRecogniserImpl;

import java.util.*;

public class DiagramCodeGenerator implements CodeGenerator {

    private Map<Match, CodeProvider> codeProviderMap;
    private final MatchFactoryImpl matchFactory;

    public DiagramCodeGenerator() {

        // TODO DI
        matchFactory = new MatchFactoryImpl(new ShapeHelperWrapperImpl(new MiscHelperWrapperImpl()), new LinkTypeRecogniserImpl());
    }


    private Set<XShapes> usedShapes = new HashSet<>();

    @Override
    public String generateCode(CodeSource codeSource) {

        if (!(codeSource instanceof DiagramCodeSource)) {
            throw new IllegalArgumentException("This implementation of CodeGenerator only takes ru.ssau.graphplus.DiagramCodeSource");
        }

        DiagramCodeSource diagramCodeSource = (DiagramCodeSource) codeSource;

        codeProviderMap = matchFactory.getMatchToCodeProvider(diagramCodeSource.getDiagramType());

        List<ConnectedShapesComplex> connectedShapesComplexes = diagramCodeSource.getConnectedShapesComplexes();

        StringBuffer buffer = new StringBuffer();



        for (ConnectedShapesComplex connectedShapesComplex : connectedShapesComplexes) {

            for (Match match : codeProviderMap.keySet()) {
                if (match.matches(connectedShapesComplex)) {

                    CodeProvider codeP = codeProviderMap.get(match);
                    if (codeP instanceof LinkCodeBase){
                        LinkCodeBase linkCodeBase = (LinkCodeBase) codeP;
                        linkCodeBase.setConnectedShapesComplex(connectedShapesComplex);
                    }
                    String code = codeP.getCode();
                    buffer.append(code).append("\n");
                }
            }
        }
        return buffer.toString();
    }

}
