/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl;


import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.sun.star.drawing.XShape;
import com.sun.star.drawing.XShapes;
import ru.ssau.graphplus.api.DiagramType;
import ru.ssau.graphplus.codegen.CodeGenerator;
import ru.ssau.graphplus.codegen.CodeProvider;
import ru.ssau.graphplus.codegen.CodeSource;
import ru.ssau.graphplus.codegen.impl.analizer.matches.Match;
import ru.ssau.graphplus.codegen.impl.analizer.matches.MatchFactory;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.QI;

import java.util.*;

public class DiagramCodeGenerator implements CodeGenerator {

    private final CodeProviderFactory codeProviderFactory;
    private final DiagramType diagramType;
    private String name;
    private Map<Match, CodeProvider> codeProviderMap;
    private final MatchFactory matchFactory;

    @Inject
    public DiagramCodeGenerator(MatchFactory matchFactory, CodeProviderFactory codeProviderFactory, @Assisted String name, @Assisted DiagramType diagramType) {
        this.matchFactory = matchFactory;
        this.name = name;
        this.codeProviderFactory = codeProviderFactory;
        this.diagramType = diagramType;
    }


    private Set<XShapes> usedShapes = new HashSet<>();

    @Override
    public String generateCode(CodeSource codeSource) {

        if (!(codeSource instanceof DiagramCodeSource)) {
            throw new IllegalArgumentException("This implementation of CodeGenerator only takes ru.ssau.graphplus.codegen.impl.DiagramCodeSource");
        }

        DiagramCodeSource diagramCodeSource = (DiagramCodeSource) codeSource;

        codeProviderMap = matchFactory.getMatchToCodeProvider(diagramCodeSource.getDiagramType());

        List<ConnectedShapesComplex> connectedShapesComplexes = diagramCodeSource.getConnectedShapesComplexes();

        StringBuffer buffer = new StringBuffer();

        List<CodeProvider> codeProviders = new ArrayList<>();

        for (ConnectedShapesComplex connectedShapesComplex : connectedShapesComplexes) {

            for (Match match : codeProviderMap.keySet()) {
                if (match.matches(connectedShapesComplex)) {
                    CodeProvider codeProvider = codeProviderMap.get(match);
                    codeProvider = codeProviderFactory.create(codeProvider.getClass());
                    if (codeProvider instanceof LinkCodeBase) {
                        LinkCodeBase linkCodeBase = (LinkCodeBase) codeProvider;
                        linkCodeBase.setConnectedShapesComplex(connectedShapesComplex);
                    }
                    codeProviders.add(codeProvider);

                }
            }
        }

        codeProviders = mergeCodeProvidersFromSameNode(codeProviders);

        for (CodeProvider codeProvider : codeProviders) {
            String code = codeProvider.getCode();
            buffer.append(code).append("\n");
        }
        return "~"+name + "=\n" + buffer.toString() + ".";
    }


    private List<CodeProvider> mergeCodeProvidersFromSameNode(List<CodeProvider> codeProviders) {

        List<CodeProvider> result = new ArrayList<>();

        Map<XShape, List<LinkCodeBase>> map = new HashMap<>();
        for (CodeProvider codeProvider : codeProviders) {
            if (codeProvider instanceof LinkCodeBase) {
                LinkCodeBase linkCodeBase = (LinkCodeBase) codeProvider;
                XShape from = linkCodeBase.from();
                String string = QI.XText(from).getString();
                System.out.println(string);
                if (!map.containsKey(from)) {
                    map.put(from, new ArrayList<LinkCodeBase>());
                }
                map.get(from).add(linkCodeBase);
            }
            else {
                result.add(codeProvider);
            }
        }

        if (diagramType.equals(DiagramType.Channel)) {
            forChannelDiagram(result, map);
        }
        if (diagramType.equals(DiagramType.Process)) {
            forProcessDiagram(result, map);
        }
        return result;
    }

    private void forChannelDiagram(List<CodeProvider> result, Map<XShape, List<LinkCodeBase>> map) {
        for (Map.Entry<XShape, List<LinkCodeBase>> entry : map.entrySet()) {
            if (Iterables.all(entry.getValue(), new Predicate<LinkCodeBase>() {
                @Override
                public boolean apply(LinkCodeBase linkCodeBase) {
                    return linkCodeBase instanceof StateTransitionCode;
                }
            }) && entry.getValue().size() > 1) {
                StateTransitionGroup codeProvider = (StateTransitionGroup) codeProviderFactory.create(StateTransitionGroup.class);
                codeProvider.setGroup(Lists.<StateTransitionCode>newArrayList(Iterables.transform(entry.getValue(), new Function<LinkCodeBase, StateTransitionCode>() {
                    @Override
                    public StateTransitionCode apply(LinkCodeBase linkCodeBase) {
                        return (StateTransitionCode) linkCodeBase;
                    }
                })));

                result.add(codeProvider);

            }
            else {
                result.addAll(entry.getValue());
            }

        }
    }

    private void forProcessDiagram(List<CodeProvider> result, Map<XShape, List<LinkCodeBase>> map) {
        for (Map.Entry<XShape, List<LinkCodeBase>> entry : map.entrySet()) {
            if (Iterables.all(entry.getValue(), new Predicate<LinkCodeBase>() {
                @Override
                public boolean apply(LinkCodeBase linkCodeBase) {
                    return (linkCodeBase instanceof PortToMethodDefaultCode || linkCodeBase instanceof PortToMethodOnMessageCode);
                }
            }) && entry.getValue().size() > 1) {
                PortToMethodGroup codeProvider = (PortToMethodGroup) codeProviderFactory.create(PortToMethodGroup.class);
                codeProvider.setGroup(Lists.<PortToMethodCode>newArrayList(Iterables.transform(entry.getValue(), new Function<LinkCodeBase, PortToMethodCode>() {
                    @Override
                    public PortToMethodCode apply(LinkCodeBase linkCodeBase) {
                        return (PortToMethodCode) linkCodeBase;
                    }
                })));

                result.add(codeProvider);

            }
            else {
                result.addAll(entry.getValue());
            }

        }
    }



}
