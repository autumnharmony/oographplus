/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.validation;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 5/2/14
 * Time: 5:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class ResultOk implements RuleResult {

    @Override
    public Object getItem() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDescription() {
        return "Ok";
    }
}
