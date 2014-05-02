/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.validation;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 5/2/14
 * Time: 4:59 AM
 * To change this template use File | Settings | File Templates.
 */
public interface RuleResult<T> {
    <T> T getItem();
    String getDescription();
}
