/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl;


import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.api.*;
import ru.ssau.graphplus.codegen.CodeGenerator;
import ru.ssau.graphplus.codegen.CodeProvider;
import ru.ssau.graphplus.codegen.CodeSource;
import ru.ssau.graphplus.codegen.impl.analizer.matches.Match;
import ru.ssau.graphplus.codegen.impl.analizer.matches.MatchFactory;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.node.ClientNode;
import ru.ssau.graphplus.node.ServerNode;

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


    @Override
    public String generateCode(CodeSource codeSource) {

        if (!(codeSource instanceof DiagramCodeSource)) {
            throw new IllegalArgumentException("This implementation of CodeGenerator only takes ru.ssau.graphplus.codegen.impl.DiagramCodeSource");
        }

        DiagramCodeSource diagramCodeSource = (DiagramCodeSource) codeSource;

        codeProviderMap = matchFactory.getMatchToCodeProvider(diagramCodeSource.getDiagramType());

        List<ConnectedShapesComplex> connectedShapesComplexes = diagramCodeSource.getConnectedShapesComplexes();

        StringBuffer buffer = new StringBuffer();

        List<CodeProvider> codeProviders = new ArrayList();

        for (ConnectedShapesComplex connectedShapesComplex : connectedShapesComplexes) {

            for (Match match : codeProviderMap.keySet()) {
                if (match.matches(connectedShapesComplex)) {
                    System.out.print("MATCH: ");
                    System.out.println(match);
                    System.out.println(connectedShapesComplex);
                    CodeProvider codeProvider = codeProviderMap.get(match);

                    System.out.print("CODE PROVIDER: ");
                    System.out.println(codeProvider);
                    codeProvider = codeProviderFactory.create(codeProvider.getClass(), Collections.<String, Object>emptyMap());
                    if (codeProvider instanceof LinkCodeBase) {
                        LinkCodeBase linkCodeBase = (LinkCodeBase) codeProvider;
                        linkCodeBase.setConnectedShapesComplex(connectedShapesComplex);
                    }
                    codeProviders.add(codeProvider);

                }
            }
        }

        codeProviders = groupCodeProvidersFromSameNode(codeProviders);

        DiagramModel diagramModel = diagramCodeSource.getDiagramModel();
        Graph graph = diagramModel.getGraph();
        Table<Node, Node, List<Link>> table = graph.getTable();

        for (Node node : diagramModel.getNodes()) {

            Map<Node, List<Link>> outgoing = table.row(node);
            Map<Node, List<Link>> incoming = table.column(node);


            if (node instanceof ClientNode || node instanceof ServerNode &&  diagramModel.getDiagramType().equals(DiagramType.Process)) {
                codeProviders.add(
                        codeProviderFactory.create(
                                node.getClass()
                                        .getAnnotation(CodeProviderAnnotation.class)
                                        .codeProvider(),

                                ImmutableMap.<String, Object>builder().put("out", outgoing).put("in", incoming).put("node", node).build()));

            }


        }


        for (CodeProvider codeProvider : codeProviders) {
            System.out.println("CodeProvider " + codeProvider.getClass().getSimpleName());
            String code = codeProvider.getCode();
            System.out.println("bring us next code:");
            System.out.println("=====================");
            System.out.println(code);
            System.out.println("=====================");
            buffer.append(code).append("\n\n");
        }
        return "~" + name + "=\n" + buffer.toString() + ".";
    }


    private List<CodeProvider> groupCodeProvidersFromSameNode(List<CodeProvider> codeProviders) {

        List<CodeProvider> result = new ArrayList();

        Map<XShape, List<LinkCodeBase>> map = new HashMap();


        for (CodeProvider codeProvider : codeProviders) {

            if (codeProvider instanceof LinkCodeBase) {
                LinkCodeBase linkCodeBase = (LinkCodeBase) codeProvider;

                XShape from = linkCodeBase.from();

                if (!map.containsKey(from)) {
                    map.put(from, new ArrayList<LinkCodeBase>());
                }

                map.get(from).add(linkCodeBase);
            } else {
                result.add(codeProvider);
            }
        }

        if (diagramType.equals(DiagramType.Channel)) {
            groupForChannelDiagram(result, map);
        }
        if (diagramType.equals(DiagramType.Process)) {
            groupForProcessDiagram(result, map);
        }
        return result;
    }

    private void groupForChannelDiagram(List<CodeProvider> result, Map<XShape, List<LinkCodeBase>> map) {
        for (Map.Entry<XShape, List<LinkCodeBase>> entry : map.entrySet()) {
            Iterable<LinkCodeBase> filter = Iterables.filter(entry.getValue(), new Predicate<LinkCodeBase>() {
                @Override
                public boolean apply(LinkCodeBase linkCodeBase) {
                    return linkCodeBase instanceof StateTransitionCode;
                }
            });
            if (Iterables.size(filter) > 1 && entry.getValue().size() > 1) {
                StateTransitionGroup codeProvider = (StateTransitionGroup) codeProviderFactory.create(StateTransitionGroup.class, Collections.<String, Object>emptyMap());
                codeProvider.setGroup(Lists.<StateTransitionCode>newArrayList(Iterables.transform(entry.getValue(), new Function<LinkCodeBase, StateTransitionCode>() {
                    @Override
                    public StateTransitionCode apply(LinkCodeBase linkCodeBase) {
                        return (StateTransitionCode) linkCodeBase;
                    }
                })));

                result.add(codeProvider);
                result.removeAll(Lists.newArrayList(filter));
            } else {
                result.addAll(entry.getValue());
            }

        }
    }

    private void groupForProcessDiagram(List<CodeProvider> result, Map<XShape, List<LinkCodeBase>> map) {
        for (Map.Entry<XShape, List<LinkCodeBase>> entry : map.entrySet()) {
            Iterable<LinkCodeBase> filter = Iterables.filter(entry.getValue(), new Predicate<LinkCodeBase>() {
                @Override
                public boolean apply(LinkCodeBase linkCodeBase) {
                    return (linkCodeBase instanceof PortToMethodDefaultCode || linkCodeBase instanceof PortToMethodOnMessageCode);
                }
            });
            if (Iterables.size(filter) > 1 && entry.getValue().size() > 1) {
                PortToMethodGroup codeProvider = (PortToMethodGroup) codeProviderFactory.create(PortToMethodGroup.class, Collections.<String, Object>emptyMap());
                codeProvider.setGroup(Lists.<PortToMethodCode>newArrayList(Iterables.transform(entry.getValue(), new Function<LinkCodeBase, PortToMethodCode>() {
                    @Override
                    public PortToMethodCode apply(LinkCodeBase linkCodeBase) {
                        return (PortToMethodCode) linkCodeBase;
                    }
                })));

                result.add(codeProvider);
                result.removeAll(Lists.newArrayList(filter));

            } else {
//                result.addAll(entry.getValue());
            }
        }

        for (Map.Entry<XShape, List<LinkCodeBase>> entry : map.entrySet()) {

            Iterable<LinkCodeBase> filter = Iterables.filter(entry.getValue(), new Predicate<LinkCodeBase>() {
                @Override
                public boolean apply(LinkCodeBase linkCodeBase) {
                    return (linkCodeBase instanceof PortMessageCode);
                }
            });
            if (Iterables.size(filter) > 1 && entry.getValue().size() > 1 && Iterables.all(filter, new Predicate<LinkCodeBase>() {
                @Override
                public boolean apply(ru.ssau.graphplus.codegen.impl.LinkCodeBase input) {
                    PortMessageCode portMessageCode = (PortMessageCode) input;
                    return portMessageCode.type(portMessageCode.to()).equals(Node.NodeType.ServerPort) || portMessageCode.type(portMessageCode.to()).equals(Node.NodeType.ClientPort);
                }
            })) {
                PortMessageGroup codeProvider = (PortMessageGroup) codeProviderFactory.create(PortMessageGroup.class, Collections.<String, Object>emptyMap());
                codeProvider.setGroup(Lists.<PortMessageCode>newArrayList(Iterables.transform(entry.getValue(), new Function<LinkCodeBase, PortMessageCode>() {
                    @Override
                    public PortMessageCode apply(LinkCodeBase linkCodeBase) {
                        return (PortMessageCode) linkCodeBase;
                    }
                })));

                result.add(codeProvider);
                result.removeAll(Lists.newArrayList(filter));

            } else {
                result.addAll(entry.getValue());
            }

        }
    }


}
