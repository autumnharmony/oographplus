/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl;

import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XShape;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import ru.ssau.graphplus.TestModule;
import ru.ssau.graphplus.api.DiagramType;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.codegen.impl.analizer.Graph;
import ru.ssau.graphplus.commons.*;

import java.util.ArrayList;
import java.util.List;

@Ignore
public class DiagramWalkerTest {


    private UnoRuntimeWrapper unoRuntimeWrapper;
    private ShapeHelperWrapper shapeHelperWrapper;
    private DiagramWalker diagramWalker;

    @Before
    public void setUp() throws Exception {
        shapeHelperWrapper = Mockito.mock(ShapeHelperWrapper.class, "shapeHelperWrapper");
        unoRuntimeWrapper = Mockito.mock(UnoRuntimeWrapper.class, "unoRuntimeWrapper");
        Injector injector = Guice.createInjector(new TestModule(shapeHelperWrapper, unoRuntimeWrapper));

        QI.setUnoRuntimeWrapper(unoRuntimeWrapper);
        diagramWalker = injector.getInstance(DiagramWalker.class);
    }


    /**
     * Переход от порта к методу,
     * если поступило данное сообщение
     */
    @Ignore //TODO
    @Test
    public void portToMethod() throws Exception {

        final XShape method = Mockito.mock(XShape.class, "method");
        Mockito.when(shapeHelperWrapper.getNodeType(method)).thenReturn(Node.NodeType.MethodOfProcess);


        final XShape serverPort = Mockito.mock(XShape.class, "serverPort");
        Mockito.when(shapeHelperWrapper.getNodeType(serverPort)).thenReturn(Node.NodeType.ServerPort);

        // link start
        XShape link1Shape = Mockito.mock(XShape.class, "link1Shape");
        final XConnectorShape link1Connector = Mockito.mock(XConnectorShape.class, "link1ConnectorShape");
        XPropertySet link1PropertySet = Mockito.mock(XPropertySet.class, "link1PropertySet");


        XShape link2Shape = Mockito.mock(XShape.class, "link2Shape");
        final XConnectorShape link2Connector = Mockito.mock(XConnectorShape.class, "link2ConnectorShape");
        XPropertySet link2PropertySet = Mockito.mock(XPropertySet.class, "link2PropertySet");

        XShape linkTextShape = Mockito.mock(XShape.class, "linkTextShape");
        XPropertySet linkTextPropertySet = Mockito.mock(XPropertySet.class);




        Mockito.when(shapeHelperWrapper.isConnectorShape(link1Shape)).thenReturn(true);
        Mockito.when(shapeHelperWrapper.isConnectorShape(link2Shape)).thenReturn(true);

        Mockito.when(link1PropertySet.getPropertyValue(ConnectedShapes.START_SHAPE)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return serverPort;
            }
        });

        Mockito.when(link1PropertySet.getPropertyValue(ConnectedShapes.START_SHAPE)).thenReturn(serverPort);

        Mockito.when(link1PropertySet.getPropertyValue(ConnectedShapes.END_SHAPE)).thenReturn(linkTextShape);

        Mockito.when(link2PropertySet.getPropertyValue(ConnectedShapes.START_SHAPE)).thenReturn(linkTextShape);

        Mockito.when(link2PropertySet.getPropertyValue(ConnectedShapes.END_SHAPE)).thenReturn(method);


        Mockito.when(unoRuntimeWrapper.queryInterface(XPropertySet.class, link1Connector)).thenReturn( link1PropertySet);

        Mockito.when(unoRuntimeWrapper.queryInterface(XPropertySet.class, link2Connector)).thenReturn(link2PropertySet);

        Mockito.when(unoRuntimeWrapper.queryInterface(XConnectorShape.class, link1Shape)).thenReturn(link1Connector);
        Mockito.when(unoRuntimeWrapper.queryInterface(XConnectorShape.class, link2Shape)).thenReturn(link2Connector);


        Mockito.when(unoRuntimeWrapper.queryInterface(XShape.class, serverPort)).thenReturn(serverPort);
        Mockito.when(unoRuntimeWrapper.queryInterface(XShape.class, method)).thenReturn(method);
        Mockito.when(unoRuntimeWrapper.queryInterface(XShape.class, linkTextShape)).thenReturn(linkTextShape);

        Mockito.when(unoRuntimeWrapper.queryInterface(XConnectorShape.class, link2Shape)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return link2Connector;
            }
        });

        Mockito.when(unoRuntimeWrapper.queryInterface(XConnectorShape.class, link1Shape)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return(link1Connector);
            }
        });

        Mockito.when(unoRuntimeWrapper.queryInterface(XConnectorShape.class, link1Connector)).thenReturn(link1Connector);
        Mockito.when(unoRuntimeWrapper.queryInterface(XConnectorShape.class, link2Connector)).thenReturn(link2Connector);

        Mockito.when(shapeHelperWrapper.isTextShape(linkTextShape)).thenReturn(true);
        Mockito.when(linkTextPropertySet.getPropertyValue("Text")).thenReturn("msg");

        Mockito.when(unoRuntimeWrapper.queryInterface(XPropertySet.class, linkTextShape)).thenReturn(linkTextPropertySet);

        // link end

        diagramWalker.setDiagramType(DiagramType.Process);

        Graph walk1 = diagramWalker.walk(Sets.newHashSet(method, link1Shape, link2Shape, linkTextShape, serverPort));
        List<ConnectedShapesComplex> walk = new ArrayList<>(diagramWalker.getConnectedShapesComplexes());

        Assert.assertEquals(diagramWalker.visited.size(), 5);
        Assert.assertEquals(walk.size(), 1);
        ConnectedShapesComplex connectedShapesComplex = walk.get(0);
        Assert.assertEquals(connectedShapesComplex.fromShape, serverPort);
        Assert.assertEquals(connectedShapesComplex.toShape, method);
        Assert.assertEquals(connectedShapesComplex.textShape, linkTextShape);




    }




    @Test
    public void portToMethod_simpleLink() throws Exception {

        final XShape method = Mockito.mock(XShape.class, "method");
        Mockito.when(shapeHelperWrapper.getNodeType(method)).thenReturn(Node.NodeType.MethodOfProcess);


        final XShape serverPort = Mockito.mock(XShape.class, "serverPort");
        Mockito.when(shapeHelperWrapper.getNodeType(serverPort)).thenReturn(Node.NodeType.ServerPort);

        XShape linkShape = Mockito.mock(XShape.class, "linkShape");
        XConnectorShape linkConnector = Mockito.mock(XConnectorShape.class, "linkConnectorShape");
        XPropertySet linkPropertySet = Mockito.mock(XPropertySet.class, "linkPropertySet");
        Mockito.when(linkPropertySet.getPropertyValue("Text")).thenReturn("msg");


        Mockito.when(shapeHelperWrapper.isConnectorShape(linkShape)).thenReturn(true);

        Mockito.when(linkPropertySet.getPropertyValue(ConnectedShapes.START_SHAPE)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return serverPort;
            }
        });

        Mockito.when(linkPropertySet.getPropertyValue(ConnectedShapes.START_SHAPE)).thenReturn(serverPort);

        Mockito.when(linkPropertySet.getPropertyValue(ConnectedShapes.END_SHAPE)).thenReturn(method);



        Mockito.when(unoRuntimeWrapper.queryInterface(XPropertySet.class, linkConnector)).thenReturn( linkPropertySet);



        Mockito.when(unoRuntimeWrapper.queryInterface(XConnectorShape.class, linkShape)).thenReturn(linkConnector);



        Mockito.when(unoRuntimeWrapper.queryInterface(XShape.class, serverPort)).thenReturn(serverPort);
        Mockito.when(unoRuntimeWrapper.queryInterface(XShape.class, method)).thenReturn(method);



        // link end

        diagramWalker.setDiagramType(DiagramType.Process);

        diagramWalker.walk(Sets.newHashSet(method, linkShape, serverPort));

        Assert.assertEquals(diagramWalker.visited.size(), 3);
    }



    @Test
    public void methodToMethodSuccessfullCase() throws Exception {

        final XShape method1 = Mockito.mock(XShape.class, "method1");
        Mockito.when(shapeHelperWrapper.getNodeType(method1)).thenReturn(Node.NodeType.MethodOfProcess);

        final XShape method2 = Mockito.mock(XShape.class, "method2");
        Mockito.when(shapeHelperWrapper.getNodeType(method2)).thenReturn(Node.NodeType.ServerPort);

        // link start
        XShape link1Shape = Mockito.mock(XShape.class, "link1Shape");
        XConnectorShape link1Connector = Mockito.mock(XConnectorShape.class, "link1ConnectorShape");
        XPropertySet link1PropertySet = Mockito.mock(XPropertySet.class, "link1PropertySet");


        XShape link2Shape = Mockito.mock(XShape.class, "link2Shape");
        XConnectorShape link2Connector = Mockito.mock(XConnectorShape.class, "link2ConnectorShape");
        XPropertySet link2PropertySet = Mockito.mock(XPropertySet.class, "link2PropertySet");

        XShape linkTextShape = Mockito.mock(XShape.class, "linkTextShape");
        XPropertySet linkTextPropertySet = Mockito.mock(XPropertySet.class);

        Mockito.when(shapeHelperWrapper.isConnectorShape(link1Shape)).thenReturn(true);
        Mockito.when(shapeHelperWrapper.isConnectorShape(link2Shape)).thenReturn(true);

        Mockito.when(link1PropertySet.getPropertyValue(ConnectedShapes.START_SHAPE)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return method1;
            }
        });

        Mockito.when(link1PropertySet.getPropertyValue(ConnectedShapes.END_SHAPE)).thenReturn(linkTextShape);

        Mockito.when(link2PropertySet.getPropertyValue(ConnectedShapes.START_SHAPE)).thenReturn(linkTextShape);

        Mockito.when(link2PropertySet.getPropertyValue(ConnectedShapes.END_SHAPE)).thenReturn(method2);

        Mockito.when(unoRuntimeWrapper.queryInterface(XPropertySet.class, link1Connector)).thenReturn(link1PropertySet);

        Mockito.when(unoRuntimeWrapper.queryInterface(XPropertySet.class, link2Connector)).thenReturn(link2PropertySet);

        Mockito.when(unoRuntimeWrapper.queryInterface(XConnectorShape.class, link1Shape)).thenReturn(link1Connector);
        Mockito.when(unoRuntimeWrapper.queryInterface(XConnectorShape.class, link2Shape)).thenReturn(link2Connector);


        Mockito.when(unoRuntimeWrapper.queryInterface(XShape.class, method2)).thenReturn(method2);
        Mockito.when(unoRuntimeWrapper.queryInterface(XShape.class, method1)).thenReturn(method1);
        Mockito.when(unoRuntimeWrapper.queryInterface(XShape.class, linkTextShape)).thenReturn(linkTextShape);


        Mockito.when(shapeHelperWrapper.isTextShape(linkTextShape)).thenReturn(true);
        Mockito.when(linkTextPropertySet.getPropertyValue("Text")).thenReturn("+");

        Mockito.when(unoRuntimeWrapper.queryInterface(XPropertySet.class, linkTextShape)).thenReturn(linkTextPropertySet);

        // link end


        diagramWalker.setDiagramType(DiagramType.Process);

        diagramWalker.walk(Sets.newHashSet(method1, link1Shape, link2Shape, linkTextShape, method2));

        Assert.assertEquals(diagramWalker.visited.size(), 5);

    }

    @Test
    public void methodToMethod() throws Exception {

        ShapeHelperWrapper shapeHelperWrapper = Mockito.mock(ShapeHelperWrapper.class);
        UnoRuntimeWrapper unoRuntimeWrapper = Mockito.mock(UnoRuntimeWrapper.class);


        XShape method1 = Mockito.mock(XShape.class);

        XShape method2 = Mockito.mock(XShape.class);
        Mockito.when(shapeHelperWrapper.getNodeType(method2)).thenReturn(Node.NodeType.MethodOfProcess);


        XConnectorShape link = Mockito.mock(XConnectorShape.class);
        XPropertySet linkPropertySet = Mockito.mock(XPropertySet.class);

        Mockito.when(shapeHelperWrapper.isConnectorShape(link)).thenReturn(true);

        Mockito.when(linkPropertySet.getPropertyValue(ConnectedShapes.START_SHAPE)).thenReturn(method2);
        Mockito.when(linkPropertySet.getPropertyValue(ConnectedShapes.END_SHAPE)).thenReturn(method1);

        Mockito.when(unoRuntimeWrapper.queryInterface(XPropertySet.class, link)).thenReturn(linkPropertySet);


        Injector injector = Guice.createInjector(new TestModule(shapeHelperWrapper, unoRuntimeWrapper));

        DiagramWalker diagramWalker = injector.getInstance(DiagramWalker.class);


//        diagramWalker.walk(Sets.newHashSet());
    }
}
