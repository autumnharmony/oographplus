/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.validation.impl;

import com.google.common.collect.Sets;
import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.api.DiagramModel;
import ru.ssau.graphplus.api.DiagramType;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.validation.*;

import java.util.*;

public class ValidatorImpl implements Validator {

    private final Collection<XShape> unusedShapes;
    private final Collection<XShape> unconnectedShapes;
    Set<NodeRule> nodesRules;
    Set<LinkRule> linksRules = Sets.<LinkRule>newHashSet(
            //new LinkNameRule(),
            new LinkConnectedRule());

    @Override
    public ValidationResultImpl validate(Validatable validatable) {

        if (!(validatable instanceof DiagramModel)) {
            throw new IllegalArgumentException("This implementaion of Validator only works with DiagramModel");
        }

        List<RuleResult>  results = new ArrayList<>();

        System.out.println("Validation");

        DiagramModel diagramModel = (DiagramModel) validatable;


        for (NodeRule nodeRule : nodesRules) {
            for (Node node : diagramModel.getNodes()) {
                RuleResult<Node> check = nodeRule.check(node);

                System.out.println("Validated node: "+node.getName()+" Result: "+check.getDescription());
                if (!(check instanceof ResultOk)){
                    results.add(check);
                }
            }
        }

        for (LinkRule linkRule : linksRules) {
            for (Link link : diagramModel.getLinks()) {
                RuleResult<Link> check = linkRule.check(link);
                System.out.println("Validated link: "+link.getName()+" Result: "+check.getDescription());
                if (!(check instanceof ResultOk)){
                    results.add(check);
                }
            }
        }

//        for (XShape unusedShape : unusedShapes){
//            results.add(new RuleError("Unused shape", new UnknownSingleShapedDiagramElement(unusedShape)));
//        }

        System.out.println("Validation completed");

        return new ValidationResultImpl(results);
    }

    private DiagramModel diagramModel;

    public ValidatorImpl(DiagramModel diagramModel, Collection<XShape> all, Set<XShape> usedShapes) {
        this.diagramModel = diagramModel;

        unconnectedShapes = Sets.newHashSet();

        Set<XShape> unusedShapes = new HashSet<>(all);
        unusedShapes.removeAll(usedShapes);
        this.unusedShapes = unusedShapes;
        nodesRules = Sets.<NodeRule>newHashSet(new NodeNameRule(), new NodeConnectedRule(diagramModel));
        if (diagramModel.getDiagramType().equals(DiagramType.Process)) {
            nodesRules.add(new PortNameRule());
        }
    }
}
