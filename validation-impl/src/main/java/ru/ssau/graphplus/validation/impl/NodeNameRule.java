/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.validation.impl;

import com.google.common.base.Strings;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.validation.NodeRule;
import ru.ssau.graphplus.validation.RuleResult;

public class NodeNameRule extends RuleBase<Node> implements NodeRule {

    @Override
    public RuleResult<Node> check(Node node) {
        String name = node.getName();
        if (Strings.isNullOrEmpty(name)){
            return error(node);
        }
        else return new ResultOk();
    }
}
