/*
 * Copyright (c) 2013. Anton Borisov
 */

package ru.ssau.graphplus;

import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XDrawPages;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.*;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.uno.Type;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.link.LinkBase;
import ru.ssau.graphplus.node.NodeBase;

import static org.mockito.Mockito.*;

/**
 * User: anton
 * Date: 8/26/13
 * Time: 1:54 AM
 */
public class DiagramModelTest {
    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testSerializeToString() throws Exception {




    }

    @Test
    public void test_remap_only_nodes() throws Exception {

        final XShape shape = mock(XShape.class);
        final XShape shape2 = mock(XShape.class);

        DiagramModel diagramModel = new DiagramModel(mock(XComponent.class)){

            @Override
            XDrawPages getDrawPages(XComponent xDrawDoc) {
                XDrawPages drawPages = mock(XDrawPages.class);
                when(drawPages.getCount()).thenReturn(1);
                when(drawPages.getElementType()).thenReturn(new Type(XDrawPage.class));

                XDrawPage drawPage = mock(XDrawPage.class);
                when(drawPage.getCount()).thenReturn(2);


                try {

                    when(drawPage.getByIndex(0)).thenReturn(shape);
                    when(drawPage.getByIndex(1)).thenReturn(shape2);
                    when(drawPages.getByIndex(0)).thenReturn(drawPage);

                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (WrappedTargetException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }


                return drawPages;
            }

            @Override
            String getId(XShape xShape) {
                if (xShape.equals(shape)){
                    return "node0";
                }
                if (xShape.equals(shape2)){
                    return "node1";
                }
                throw new RuntimeException( new IllegalArgumentException());
            }
        };

        NodeBase node = node("node0", "whatever");
        NodeBase node2 = node("node1", "whatever1");


        diagramModel.addDiagramElement(node);

        diagramModel.addDiagramElement(node2);



        diagramModel.remap(null);
        verify(node).setProps(shape);
        verify(node2).setProps(shape2);


    }

    private NodeBase node(String id, String name) {
        NodeBase node = mock(NodeBase.class);
        when(node.getName()).thenReturn(name);
        when(node.getType()).thenReturn(Node.NodeType.ClientPort);
        when(node.getId()).thenReturn(id);
        XShape shape_ = mock(XShape.class);
        when(node.getShape()).thenReturn(shape_);
        return node;
    }




    @Test
    public void test_remap_nodes_and_links() throws Exception {

        final XShape shape = mock(XShape.class);
        final XShape shape2 = mock(XShape.class);


        final XShape link1_connShape1 = mock(XShape.class);
        final XShape link1_connShape2 = mock(XShape.class);
        final XShape link1_textShape = mock(XShape.class);



        DiagramModel diagramModel = new DiagramModel(mock(XComponent.class)){

            @Override
            XDrawPages getDrawPages(XComponent xDrawDoc) {
                XDrawPages drawPages = mock(XDrawPages.class);
                when(drawPages.getCount()).thenReturn(1);
                when(drawPages.getElementType()).thenReturn(new Type(XDrawPage.class));

                XDrawPage drawPage = mock(XDrawPage.class);
                when(drawPage.getCount()).thenReturn(5);


                try {

                    when(drawPage.getByIndex(0)).thenReturn(shape);
                    when(drawPage.getByIndex(1)).thenReturn(shape2);

                    when(drawPage.getByIndex(4)).thenReturn(link1_connShape2);
                    when(drawPage.getByIndex(2)).thenReturn(link1_connShape1);
                    when(drawPage.getByIndex(3)).thenReturn(link1_textShape);


                    when(drawPages.getByIndex(0)).thenReturn(drawPage);

                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (WrappedTargetException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }


                return drawPages;
            }

            @Override
            String getId(XShape xShape) {
                if (xShape.equals(shape)){
                    return "node0";
                }
                if (xShape.equals(shape2)){
                    return "node1";
                }

                if (xShape.equals(link1_connShape1)){
                    return "link1/conn1";
                }

                if (xShape.equals(link1_connShape2)){
                    return "link1/conn2";
                }

                if (xShape.equals(link1_textShape)){
                    return "link1/text";
                }

                throw new RuntimeException( new IllegalArgumentException());
            }

            @Override
            void setLinkId(Link link, XShape connShape1, XShape connShape2, XShape textShape) {

            }
        };

        NodeBase node = node("node0", "whatever");
        NodeBase node2 = node("node1", "whatever1");




        LinkBase link = link(node, node2, link1_connShape1, link1_connShape2, link1_textShape);


        diagramModel.addDiagramElement(node);
        diagramModel.addDiagramElement(node2);

        diagramModel.addDiagramElement(link);



        diagramModel.remap(null);
        verify(node).setProps(shape);
        verify(node2).setProps(shape2);

        verify(link).setProps(link1_connShape1, link1_connShape2, link1_textShape);


    }

    private LinkBase link(Node node, Node node2, XShape cS1, XShape cS2, XShape tS) {
        LinkBase link = mock(LinkBase.class);
        when(link.getId()).thenReturn("link1");
        when(link.getName()).thenReturn("LINK NAME");
        when(link.getConnShape1()).thenReturn(cS1);
        when(link.getConnShape2()).thenReturn(cS2);
        when(link.getTextShape()).thenReturn(tS);
        when(link.getStartNode()).thenReturn(node);
        when(link.getEndNode()).thenReturn(node2);
        return link;
    }


}
