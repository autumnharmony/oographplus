/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.validation;

import com.google.common.base.Strings;
import ru.ssau.graphplus.api.Node;

public class NodeNameRule implements NodeRule {

    @Override
    public RuleResult<Node> check(Node node) {
        String name = node.getName();
        if (Strings.isNullOrEmpty(name)){
            return new RuleError("Node without name", node);
        }
        else return new ResultOk();
    }
}
