/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import ru.ssau.graphplus.codegen.CodeProvider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CodeProviderAnnotation {
    Class<? extends CodeProvider> codeProvider();
}
