/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl.analizer;

import java.util.Set;

public interface Walker<Item,Result> {
    public Result walk(Set<Item> allItems, Item start);
}
