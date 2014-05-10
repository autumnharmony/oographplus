/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl;

import com.google.inject.AbstractModule;
import ru.ssau.graphplus.codegen.impl.analizer.matches.MatchFactory;
import ru.ssau.graphplus.codegen.impl.analizer.matches.MatchFactoryImpl;
import ru.ssau.graphplus.commons.CommonsModule;
import ru.ssau.graphplus.codegen.impl.recognition.LinkTypeRecogniser;
import ru.ssau.graphplus.codegen.impl.recognition.LinkTypeRecogniserImpl;

public class CodeGeneratorModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new CommonsModule());

//        bind(UnoRuntimeWrapper.class).to(UnoRuntimeWrapperImpl.class);
        bind(MatchFactory.class).to(MatchFactoryImpl.class);
        bind(LinkTypeRecogniser.class).to(LinkTypeRecogniserImpl.class);
    }
}
