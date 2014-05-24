package ru.ssau.graphplus.codegen.impl;

import ru.ssau.graphplus.codegen.CodeProvider;

import java.util.Map;

/**
 * Created by anton on 12.05.14.
 */
public interface CodeProviderFactory{
    CodeProvider create(Class<? extends CodeProvider> codeProviderClass, Map<String, Object> params);
}
