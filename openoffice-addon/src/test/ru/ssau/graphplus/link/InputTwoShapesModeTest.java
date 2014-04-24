/*
 * Copyright (c) 2013. Anton Borisov
 */

package ru.ssau.graphplus.link;

import com.sun.star.drawing.XShape;
import com.sun.star.lang.XComponent;
//import ooo.connector.BootstrapSocketConnector;
import org.junit.Ignore;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.api.Node;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

/**
 * User: anton
 * Date: 6/5/13
 * Time: 11:18 PM
 */
@Ignore
public class InputTwoShapesModeTest {

    private InputTwoShapesMode inputTwoShapesMode;

    private XShape selectedShape;
    private XComponent xDoc;
    private XShape first;
    private XShape second;
    private Link link;
    private Node firstNode;
    private Node secondNode;

//    @Before
//    public void setUp() throws Exception {
//
//        String oooExeFolder = "/opt/libreoffice4.0/program";
//        XComponentContext m_xContext = BootstrapSocketConnector.bootstrap(oooExeFolder);
//
//
//        XComponentLoader xLoader = UnoRuntime.queryInterface(
//                XComponentLoader.class,
//                m_xContext.getServiceManager().createInstanceWithContext(
//                        "com.sun.star.frame.Desktop", m_xContext));
//        xDoc = UnoRuntime.queryInterface(
//                XComponent.class,
//                xLoader.loadComponentFromURL(
//                        "private:factory/sdraw", "_default",
//                        FrameSearchFlag.ALL, new PropertyValue[]{}));
//
//        DiagramController controller = mock(DiagramController.class);
//
//        when(controller.getSelectedShape()).thenAnswer(new Answer<Object>() {
//            @Override
//            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
//                return selectedShape;
//            }
//        });
//
//
//
//        when(controller.getSelectedShapes()).thenAnswer(new Answer<Object>() {
//            @Override
//            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
//                return null;
//            }
//        });
//
//
//        when(controller.getLinker()).thenAnswer(new Answer<Linker>() {
//            @Override
//            public Linker answer(InvocationOnMock invocationOnMock) throws Throwable {
//                return new Linker() {
//                    @Override
//                    public void link(XShape sh1, XShape sh2) {
//
//                    }
//
//                    @Override
//                    public void adjustLink(XShape sh1, XShape sh2) {
//
//                    }
//
//                    @Override
//                    public XShape getTextShape() {
//                        return null;
//                    }
//                };
//            }
//        });
//        DiagramModel model = mock(DiagramModel.class);
//
//
//        first = DrawHelper.createShape(xDoc, DrawHelper.SHAPE_KIND_RECTANGLE);
//        second = DrawHelper.createShape(xDoc, DrawHelper.SHAPE_KIND_ELLIPSE);
//
//        NodeFactory nodeFactory = new NodeFactory(QI.XMultiServiceFactory(xDoc));
//        LinkFactory linkFactory = new LinkFactory(QI.XMultiServiceFactory(xDoc));
//
//        firstNode = nodeFactory.create(first, Node.NodeType.ClientPort);
//        secondNode = nodeFactory.create(second, Node.NodeType.ServerPort);
//
//        final Map<XShape, DiagramElement> map = new HashMap<>();
//        map.put(first, firstNode);
//        map.put(second, secondNode);
//
//
//        when(model.getDiagramElementByShape(any(XShape.class))).thenAnswer(new Answer<DiagramElement>() {
//            @Override
//            public DiagramElement answer(InvocationOnMock invocationOnMock) throws Throwable {
//                return map.get(invocationOnMock.getArguments()[0]);
//            }
//        });
//
//        when(controller.getDiagramModel()).thenReturn(model);
//
//        inputTwoShapesMode = new InputTwoShapesMode();
//        inputTwoShapesMode.setDiagramController(controller);
//        link = linkFactory.create(Link.LinkType.Link, xDoc);
//        inputTwoShapesMode.setLink(link);
//
//    }
//
//    @After
//    public void tearDown() throws Exception {
//
//    }
//
//    @Test
//    public void testOnInput() throws Exception {
//
//
//
//        selectedShape =  first;
//
//        inputTwoShapesMode.onInput(null);
//
//        Assert.assertEquals(inputTwoShapesMode.firstShape, first);
//
//        selectedShape = second;
//
//        inputTwoShapesMode.onInput(null);
//
//        Assert.assertEquals(inputTwoShapesMode.secondShape, second);
//
//        Assert.assertEquals(link.getStartNode(),firstNode);
//        Assert.assertEquals(link.getEndNode(), secondNode);
//
//    }
}
