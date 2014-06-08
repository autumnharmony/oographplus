package ru.ssau.graphplus.commons;

import com.sun.star.awt.Point;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.WrappedTargetException;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.collection.IsArrayContaining;
import org.hamcrest.collection.IsArrayContainingInOrder;
import org.junit.Assert;
import org.junit.Assert.*;
import org.junit.matchers.JUnitMatchers.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import ru.ssau.graphplus.api.Node;

import java.awt.geom.Line2D;

import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.ssau.graphplus.commons.ShapeHelperWrapperImplTest.PointMatcher.point;

/**
 * Created by anton on 11.05.14.
 */
//@Ignore
public class ShapeHelperWrapperImplTest {

    private ShapeHelperWrapperImpl shapeHelperWrapper;
    private UnoRuntimeWrapper unoRuntimeWrapper;

    @Before
    public void setUp() throws Exception {
        shapeHelperWrapper = new ShapeHelperWrapperImpl(mock(MiscHelperWrapper.class));
        unoRuntimeWrapper = mock(UnoRuntimeWrapper.class);
        QI.setUnoRuntimeWrapper(unoRuntimeWrapper);

    }

    @Test
    public void testShift() throws Exception {

        Point[] points = new Point[4];
        Point point = new Point(0, 0);
        points[0] = point;
        Point point1 = new Point(1, 1);
        points[1] = point1;
        Point point2 = new Point(2, 2);
        points[2] = point2;
        Point point3 = new Point(3, 3);
        points[3] = point3;
        shapeHelperWrapper.shift(points);
        Assert.assertEquals(points[0], point3);
        Assert.assertEquals(points[1],point);
        Assert.assertEquals(points[2],point1);
        Assert.assertEquals(points[3],point2);

    }

    private Point p(int x, int y){
        return new Point(x,y);
    }

    @Test
    public void getType_should_return_Server() throws Exception {
        XShape shape = createShape(new Point[]{p(0,0), p(200,0), p(100, 100), p(200,200), p(0,200), p(0,0)});
        Node.NodeType nodeType = shapeHelperWrapper.getNodeType(shape);
        Assert.assertEquals(nodeType, Node.NodeType.ServerPort);
    }

    @Test
    public void getType_not_normalized_should_return_Server() throws Exception {
        XShape shape = createShape(new Point[]{p(200,0), p(100, 100), p(200,200), p(0,200), p(0,0), p(200,0)});
        Node.NodeType nodeType = shapeHelperWrapper.getNodeType(shape);
        Assert.assertEquals(nodeType, Node.NodeType.ServerPort);
    }

    @Test
    public void getType_should_return_Client() throws Exception {
        XShape shape = createShape(new Point[]{p(0,0), p(200,0), p(300, 100), p(200,200), p(0,200), p(0,0)});
        Node.NodeType nodeType = shapeHelperWrapper.getNodeType(shape);
        Assert.assertEquals(nodeType, Node.NodeType.ClientPort);
    }

    private XShape createShape(Point[] points) throws WrappedTargetException, UnknownPropertyException {
        XShape shape = mock(XShape.class);
        XPropertySet propertySet = mock(XPropertySet.class);
        when(propertySet.getPropertyValue("PolyPolygon")).thenReturn(new Point[][]{points});
        when(shape.getShapeType()).thenReturn("PolyPolygonShape");
        when(unoRuntimeWrapper.queryInterface(XPropertySet.class, shape)).thenReturn(propertySet);
        return shape;

    }

    static class PointMatcher extends TypeSafeMatcher<Point> {

        @Override
        protected boolean matchesSafely(Point point) {
            return point.X == x && point.Y == y;
        }
        @Override
        public void describeTo(Description description) {
            description.appendValue(x);
            description.appendValue(y);
        }

        int x;
        int y;

        private PointMatcher(int x, int y) {
            this.x = x;
            this.y = y;
        }
        static PointMatcher point(int x, int y){
            return new PointMatcher(x,y);
        }
    }

    @Test
    public void testRemoveExtra() throws Exception {
        Line2D.Double line = new Line2D.Double(0,0,0,300);
        boolean contains = line.contains(0, 100);
        Point[] points = shapeHelperWrapper.removeExtra(new Point[]{new Point(0, 0), new Point(0, 100), new Point(0, 200), new Point(30, 300), new Point(0, 0)});

        Assert.assertEquals(points.length, 4);
        Assert.assertThat(points, IsArrayContainingInOrder.arrayContaining(point(0,0), point(0,200), point(30,300), point(0,0)));
    }
}
