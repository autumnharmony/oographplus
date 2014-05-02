/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.validation;

import com.google.common.base.Strings;
import ru.ssau.graphplus.api.Link;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 5/2/14
 * Time: 12:51 PM
 * To change this template use File | Settings | File Templates.
 */
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
