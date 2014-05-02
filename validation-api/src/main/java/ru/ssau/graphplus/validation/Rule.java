/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.validation;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 5/2/14
 * Time: 2:19 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Rule<T> {
    RuleResult<T> check(T t);
}
