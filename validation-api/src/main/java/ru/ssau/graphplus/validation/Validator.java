/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.validation;

public interface Validator {
    ValidationResult validate(Validatable validatable);
}
