/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;


import com.google.inject.Guice;
import com.google.inject.Injector;
import org.reflections.Reflections;
import ru.ssau.graphplus.analizer.Match;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DiagramCodeGenerator implements CodeGenerator {


    private final List<Match> matches;

    public DiagramCodeGenerator() {

        matches = new ArrayList<>();

        Set<Class<? extends Match>> subTypesOf = new Reflections("ru.ssau.graphplus.analizer.*").getSubTypesOf(Match.class);
        Injector injector = Guice.createInjector(new CodeGeneratorModule());
        for (Class<? extends Match> aClass : subTypesOf) {
            Match instance = injector.getInstance(aClass);
            matches.add(instance);
        }

    }


    @Override
    public String generateCode(CodeSource codeSource) {

        if (!(codeSource instanceof DiagramCodeSource)) {
            throw new IllegalArgumentException("This implementation of CodeGenerator only takes ru.ssau.graphplus.DiagramCodeSource");
        }

        DiagramCodeSource diagramCodeSource = (DiagramCodeSource) codeSource;
        List<ConnectedShapesComplex> connectedShapesComplexes = diagramCodeSource.getConnectedShapesComplexes();

        ru.ssau.graphplus.api.DiagramModel diagramModel = diagramCodeSource.getDiagramModel();

        StringBuffer buffer = new StringBuffer();

        for (ConnectedShapesComplex connectedShapesComplex : connectedShapesComplexes) {
            for (Match match : matches) {
                if (match.matches(connectedShapesComplex)){
                    buffer.append(match.getClass().getSimpleName());
                }
            }
        }


        // TODO
        return "Generated Code:"+buffer.toString();
    }

//
//    private
}
