/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.swing.text.Position;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class FlowLayoutTest {

    private FlowLayout flowLayout;

    @Before
    public void setUp() throws Exception {
        flowLayout = new FlowLayout();
    }



    @After
    public void tearDown() throws Exception {

    }

    class ObjForTest implements Layout.Obj {

        Dimension dimension;
        Point position;

        ObjForTest(Dimension dimension, Point position) {
            this.dimension = dimension;
            this.position = position;
        }

        @Override
        public Dimension getDimension() {
            return dimension;
        }

        @Override
        public void setPosition(Point position) {
            this.position = position;
        }

        @Override
        public Point getPosition() {
            return position;
        }

        @Override
        public Rectangle getBound() {
            return new Rectangle(position.x, position.y, dimension.width, dimension.height);
        }
    }

    @Test
    public void testLayout() throws Exception {
        Layout.Stage stage = mock(Layout.Stage.class);



        List<Layout.Obj> objsOnStage = new ArrayList<>();
        Layout.Obj mock = createObj(0,0);
        objsOnStage.add(mock);

        Layout.Obj mock2 = createObj(40,0);
        objsOnStage.add(mock2);

        when(stage.getObjects()).thenReturn(objsOnStage);
        when(stage.getDimenstion()).thenReturn(new Dimension(300,300));


        Layout.Obj obj = new ObjForTest(new Dimension(30,30), new Point(0,0));
//        Layout.Obj spy = spy(obj);
//
        flowLayout.layout(stage, obj);
        Assert.assertEquals(obj.getPosition().getX(), 70.0);
        Assert.assertEquals(obj.getPosition().getY(), 0.0);

    }

    private Layout.Obj createObj(int x, int y) {
        Layout.Obj mock = mock(Layout.Obj.class);
        when(mock.getPosition()).thenReturn(new Point(x,y));
        when(mock.getBound()).thenReturn(new Rectangle(x,y, 30,30));
        when(mock.getDimension()).thenReturn(new Dimension(30,30));
        return mock;
    }
}
