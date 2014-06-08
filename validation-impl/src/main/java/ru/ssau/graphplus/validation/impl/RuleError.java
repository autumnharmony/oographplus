/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.validation.impl;

import ru.ssau.graphplus.validation.RuleResult;

public class RuleError<T> implements RuleResult {
    private final String desc;
    private final T item;
    private final Type type;

    public RuleError(String desc, T item, Type type) {
        this.desc = desc;
        this.item = item;
        this.type = type;
    }

    public  RuleError(String desc, T item) {
        this.desc = desc;
        this.item = item;
        type = Type.Error;
    }


    @Override
    public T getItem() {
        return item;
    }

    @Override
    public String getDescription() {
      return desc;
    }
    @Override
    public Type getType() {
        return type;
    }
}
