/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.validation.impl;

import com.google.common.base.Strings;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.validation.LinkRule;
import ru.ssau.graphplus.validation.RuleResult;

public class LinkNameRule implements LinkRule {
    @Override
    public RuleResult<Link> check(Link link) {
        String name = link.getName();
        if (Strings.isNullOrEmpty(name.trim())){
            return new RuleError("Link without name", link);
        }
        else return new ResultOk();
    }
}
