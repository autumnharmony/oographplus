/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.validation;

import com.google.common.collect.Sets;
import ru.ssau.graphplus.api.DiagramModel;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.api.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 5/2/14
 * Time: 1:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class ValidatorImpl implements Validator {

    Set<NodeRule> nodesRules = Sets.<NodeRule>newHashSet(new NodeNameRule());
    Set<LinkRule> linksRules = Sets.<LinkRule>newHashSet(new LinkNameRule());

    @Override
    public ValidationResult validate(Validatable validatable) {

        if (!(validatable instanceof DiagramModel)) {
            throw new IllegalArgumentException("This implementaion of Validator only works with DiagramModel");
        }

        List<RuleResult>  results = new ArrayList<>();

        DiagramModel diagramModel = (DiagramModel) validatable;
        for (NodeRule nodeRule : nodesRules) {
            for (Node node : diagramModel.getNodes()) {
                RuleResult<Node> check = nodeRule.check(node);
                if (!(check instanceof ResultOk)){
                    results.add(check);
                }
            }
        }

        for (LinkRule linkRule : linksRules) {
            for (Link link : diagramModel.getLinks()) {
                RuleResult<Link> check = linkRule.check(link);
                if (!(check instanceof ResultOk)){
                    results.add(check);
                }
            }
        }


        return new ValidationResultImpl(results);
    }
}
