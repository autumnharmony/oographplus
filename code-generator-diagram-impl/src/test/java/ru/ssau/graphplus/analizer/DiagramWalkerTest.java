/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.analizer;

import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XShape;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import ru.ssau.graphplus.*;
import ru.ssau.graphplus.api.DiagramType;
import ru.ssau.graphplus.api.Node;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class DiagramWalkerTest {


    private UnoRuntimeWrapper unoRuntimeWrapper;
    private ShapeHelperWrapper shapeHelperWrapper;
    private DiagramWalker diagramWalker;

    @Before
    public void setUp() throws Exception {
        shapeHelperWrapper = mock(ShapeHelperWrapper.class, "shapeHelperWrapper");
        unoRuntimeWrapper = mock(UnoRuntimeWrapper.class, "unoRuntimeWrapper");
        Injector injector = Guice.createInjector(new TestModule(shapeHelperWrapper, unoRuntimeWrapper));

        QI.setUnoRuntimeWrapper(unoRuntimeWrapper);
        diagramWalker = injector.getInstance(DiagramWalker.class);
    }


    /**
     * Переход от порта к методу,
     * если поступило данное сообщение
     */
    @Test
    public void portToMethod() throws Exception {

        final XShape method = mock(XShape.class, "method");
        when(shapeHelperWrapper.getNodeType(method)).thenReturn(Node.NodeType.MethodOfProcess);


        final XShape serverPort = mock(XShape.class, "serverPort");
        when(shapeHelperWrapper.getNodeType(serverPort)).thenReturn(Node.NodeType.ServerPort);

        // link start
        XShape link1Shape = mock(XShape.class, "link1Shape");
        XConnectorShape link1Connector = mock(XConnectorShape.class,"link1ConnectorShape");
        XPropertySet link1PropertySet = mock(XPropertySet.class, "link1PropertySet");


        XShape link2Shape = mock(XShape.class, "link2Shape");
        XConnectorShape link2Connector = mock(XConnectorShape.class, "link2ConnectorShape");
        XPropertySet link2PropertySet = mock(XPropertySet.class, "link2PropertySet");

        XShape linkTextShape = mock(XShape.class, "linkTextShape");
        XPropertySet linkTextPropertySet = mock(XPropertySet.class);




        when(shapeHelperWrapper.isConnectorShape(link1Shape)).thenReturn(true);
        when(shapeHelperWrapper.isConnectorShape(link2Shape)).thenReturn(true);

        when(link1PropertySet.getPropertyValue(ConnectedShapes.START_SHAPE)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return serverPort;
            }
        });

        when(link1PropertySet.getPropertyValue(ConnectedShapes.START_SHAPE)).thenReturn(serverPort);

        when(link1PropertySet.getPropertyValue(ConnectedShapes.END_SHAPE)).thenReturn(linkTextShape);

        when(link2PropertySet.getPropertyValue(ConnectedShapes.START_SHAPE)).thenReturn(linkTextShape);

        when(link2PropertySet.getPropertyValue(ConnectedShapes.END_SHAPE)).thenReturn(method);


        when(unoRuntimeWrapper.queryInterface(XPropertySet.class, link1Connector)).thenReturn( link1PropertySet);

        when(unoRuntimeWrapper.queryInterface(XPropertySet.class, link2Connector)).thenReturn(link2PropertySet);

        when(unoRuntimeWrapper.queryInterface(XConnectorShape.class, link1Shape)).thenReturn(link1Connector);
        when(unoRuntimeWrapper.queryInterface(XConnectorShape.class, link2Shape)).thenReturn(link2Connector);


        when(unoRuntimeWrapper.queryInterface(XShape.class, serverPort)).thenReturn(serverPort);
        when(unoRuntimeWrapper.queryInterface(XShape.class, method)).thenReturn(method);
        when(unoRuntimeWrapper.queryInterface(XShape.class, linkTextShape)).thenReturn(linkTextShape);


        when(shapeHelperWrapper.isTextShape(linkTextShape)).thenReturn(true);
        when(linkTextPropertySet.getPropertyValue("Text")).thenReturn("msg");

        when(unoRuntimeWrapper.queryInterface(XPropertySet.class, linkTextShape)).thenReturn(linkTextPropertySet);

        // link end

        diagramWalker.setDiagramType(DiagramType.Process);

        diagramWalker.walk(Sets.newHashSet(method, link1Shape, link2Shape, linkTextShape, serverPort), null);

        Assert.assertEquals(diagramWalker.visited.size(), 5);
    }




    @Test
    public void portToMethod_simpleLink() throws Exception {

        final XShape method = mock(XShape.class, "method");
        when(shapeHelperWrapper.getNodeType(method)).thenReturn(Node.NodeType.MethodOfProcess);


        final XShape serverPort = mock(XShape.class, "serverPort");
        when(shapeHelperWrapper.getNodeType(serverPort)).thenReturn(Node.NodeType.ServerPort);

        XShape linkShape = mock(XShape.class, "linkShape");
        XConnectorShape linkConnector = mock(XConnectorShape.class,"linkConnectorShape");
        XPropertySet linkPropertySet = mock(XPropertySet.class, "linkPropertySet");
        when(linkPropertySet.getPropertyValue("Text")).thenReturn("msg");


        when(shapeHelperWrapper.isConnectorShape(linkShape)).thenReturn(true);

        when(linkPropertySet.getPropertyValue(ConnectedShapes.START_SHAPE)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return serverPort;
            }
        });

        when(linkPropertySet.getPropertyValue(ConnectedShapes.START_SHAPE)).thenReturn(serverPort);

        when(linkPropertySet.getPropertyValue(ConnectedShapes.END_SHAPE)).thenReturn(method);



        when(unoRuntimeWrapper.queryInterface(XPropertySet.class, linkConnector)).thenReturn( linkPropertySet);



        when(unoRuntimeWrapper.queryInterface(XConnectorShape.class, linkShape)).thenReturn(linkConnector);



        when(unoRuntimeWrapper.queryInterface(XShape.class, serverPort)).thenReturn(serverPort);
        when(unoRuntimeWrapper.queryInterface(XShape.class, method)).thenReturn(method);



        // link end

        diagramWalker.setDiagramType(DiagramType.Process);

        diagramWalker.walk(Sets.newHashSet(method, linkShape, serverPort), null);

        Assert.assertEquals(diagramWalker.visited.size(), 3);
    }



    @Test
    public void methodToMethodSuccessfullCase() throws Exception {

        final XShape method1 = mock(XShape.class, "method1");
        when(shapeHelperWrapper.getNodeType(method1)).thenReturn(Node.NodeType.MethodOfProcess);

        final XShape method2 = mock(XShape.class, "method2");
        when(shapeHelperWrapper.getNodeType(method2)).thenReturn(Node.NodeType.ServerPort);

        // link start
        XShape link1Shape = mock(XShape.class, "link1Shape");
        XConnectorShape link1Connector = mock(XConnectorShape.class,"link1ConnectorShape");
        XPropertySet link1PropertySet = mock(XPropertySet.class, "link1PropertySet");


        XShape link2Shape = mock(XShape.class, "link2Shape");
        XConnectorShape link2Connector = mock(XConnectorShape.class, "link2ConnectorShape");
        XPropertySet link2PropertySet = mock(XPropertySet.class, "link2PropertySet");

        XShape linkTextShape = mock(XShape.class, "linkTextShape");
        XPropertySet linkTextPropertySet = mock(XPropertySet.class);

        when(shapeHelperWrapper.isConnectorShape(link1Shape)).thenReturn(true);
        when(shapeHelperWrapper.isConnectorShape(link2Shape)).thenReturn(true);

        when(link1PropertySet.getPropertyValue(ConnectedShapes.START_SHAPE)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return method1;
            }
        });

        when(link1PropertySet.getPropertyValue(ConnectedShapes.END_SHAPE)).thenReturn(linkTextShape);

        when(link2PropertySet.getPropertyValue(ConnectedShapes.START_SHAPE)).thenReturn(linkTextShape);

        when(link2PropertySet.getPropertyValue(ConnectedShapes.END_SHAPE)).thenReturn(method2);

        when(unoRuntimeWrapper.queryInterface(XPropertySet.class, link1Connector)).thenReturn(link1PropertySet);

        when(unoRuntimeWrapper.queryInterface(XPropertySet.class, link2Connector)).thenReturn(link2PropertySet);

        when(unoRuntimeWrapper.queryInterface(XConnectorShape.class, link1Shape)).thenReturn(link1Connector);
        when(unoRuntimeWrapper.queryInterface(XConnectorShape.class, link2Shape)).thenReturn(link2Connector);


        when(unoRuntimeWrapper.queryInterface(XShape.class, method2)).thenReturn(method2);
        when(unoRuntimeWrapper.queryInterface(XShape.class, method1)).thenReturn(method1);
        when(unoRuntimeWrapper.queryInterface(XShape.class, linkTextShape)).thenReturn(linkTextShape);


        when(shapeHelperWrapper.isTextShape(linkTextShape)).thenReturn(true);
        when(linkTextPropertySet.getPropertyValue("Text")).thenReturn("+");

        when(unoRuntimeWrapper.queryInterface(XPropertySet.class, linkTextShape)).thenReturn(linkTextPropertySet);

        // link end


        diagramWalker.setDiagramType(DiagramType.Process);

        diagramWalker.walk(Sets.newHashSet(method1, link1Shape, link2Shape, linkTextShape, method2), null);

        Assert.assertEquals(diagramWalker.visited.size(), 5);

    }

    @Test
    public void methodToMethod() throws Exception {

        ShapeHelperWrapper shapeHelperWrapper = mock(ShapeHelperWrapper.class);
        UnoRuntimeWrapper unoRuntimeWrapper = mock(UnoRuntimeWrapper.class);


        XShape method1 = mock(XShape.class);

        XShape method2 = mock(XShape.class);
        when(shapeHelperWrapper.getNodeType(method2)).thenReturn(Node.NodeType.MethodOfProcess);


        XConnectorShape link = mock(XConnectorShape.class);
        XPropertySet linkPropertySet = mock(XPropertySet.class);

        when(shapeHelperWrapper.isConnectorShape(link)).thenReturn(true);

        when(linkPropertySet.getPropertyValue(ConnectedShapes.START_SHAPE)).thenReturn(method2);
        when(linkPropertySet.getPropertyValue(ConnectedShapes.END_SHAPE)).thenReturn(method1);

        when(unoRuntimeWrapper.queryInterface(XPropertySet.class, link )).thenReturn(linkPropertySet);


        Injector injector = Guice.createInjector(new TestModule(shapeHelperWrapper, unoRuntimeWrapper));

        DiagramWalker diagramWalker = injector.getInstance(DiagramWalker.class);


//        diagramWalker.walk(Sets.newHashSet());
    }
}
