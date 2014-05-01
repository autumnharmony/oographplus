/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.validation;

import java.util.Collection;

public interface ValidationResult {

    Collection<Item> getItems();

    public interface Item {

        String getShortDescription();

        String getFullDescription();

    }
}
