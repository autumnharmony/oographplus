/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.di;

import com.google.inject.AbstractModule;
import ru.ssau.graphplus.api.DiagramModel;
import ru.ssau.graphplus.gui.FlowLayout;
import ru.ssau.graphplus.gui.Layout;


public class AddonModule extends AbstractModule {

    private final Layout.Stage stage;
    private final DiagramModel diagramModel;

    public AddonModule(Layout.Stage stage, DiagramModel diagramModel) {
        this.stage = stage;
        this.diagramModel = diagramModel;
    }

    @Override
    protected void configure() {
        bind(Layout.Stage.class).toInstance(stage);
        bind(Layout.class).to(FlowLayout.class);
        bind(DiagramModel.class).toInstance(diagramModel);
    }
}
