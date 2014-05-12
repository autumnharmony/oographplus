/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.di;

import com.google.inject.AbstractModule;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import ru.ssau.graphplus.codegen.impl.recognition.DiagramTypeRecognition;
import ru.ssau.graphplus.commons.CommonsModule;
import ru.ssau.graphplus.DiagramController;
import ru.ssau.graphplus.DiagramServiceImpl;
import ru.ssau.graphplus.api.DiagramModel;
import ru.ssau.graphplus.api.DiagramService;
import ru.ssau.graphplus.gui.FlowLayout;
import ru.ssau.graphplus.gui.Layout;
import ru.ssau.graphplus.gui.StageSheetImpl;
import ru.ssau.graphplus.link.LinkFactory;
import ru.ssau.graphplus.node.NodeFactory;


public class AddonModule extends AbstractModule {

    private final DiagramModel diagramModel;
    private final XMultiServiceFactory xMSF;
    private final XComponent xComponent;
    private final DiagramController diagramController;

    public AddonModule(DiagramModel diagramModel, XMultiServiceFactory xMSF, XComponent xComponent, DiagramController diagramController) {

        this.diagramModel = diagramModel;
        this.xMSF = xMSF;
        this.xComponent = xComponent;
        this.diagramController = diagramController;
    }

    @Override
    protected void configure() {
//        install(new CommonsModule());
        bind(DiagramModel.class).toInstance(diagramModel);
        bind(ru.ssau.graphplus.DiagramModel.class).toInstance((ru.ssau.graphplus.DiagramModel) diagramModel);
        bind(DiagramController.class).toInstance(diagramController);
        bind(XComponent.class).toInstance(xComponent);

        bind(Layout.Stage.class).to(StageSheetImpl.class);
        bind(Layout.class).to(FlowLayout.class);
        bind(XMultiServiceFactory.class).toInstance(xMSF);
        bind(NodeFactory.class);
        bind(LinkFactory.class);

        bind(DiagramService.class).to(DiagramServiceImpl.class);
    }
}
