/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.validation;

import ru.ssau.graphplus.api.Link;

public class LinkConnectedRule implements LinkRule {
    @Override
    public RuleResult<Link> check(Link link) {
        if (link.getStartNode()!=null && link.getEndNode()!=null){
            return new ResultOk();
        }
        else {
            return new RuleError<>("Unconnected link", link);
        }
    }
}
