package ru.ssau.graphplus.commons;

import com.sun.star.awt.Point;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * Created by anton on 11.05.14.
 */
public class ShapeHelperWrapperImplTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testShift() throws Exception {
        ShapeHelperWrapperImpl shapeHelperWrapper = new ShapeHelperWrapperImpl(mock(MiscHelperWrapper.class));
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
        Assert.assertEquals(points[0],point3);
        Assert.assertEquals(points[1],point);
        Assert.assertEquals(points[2],point1);
        Assert.assertEquals(points[3],point2);

    }
}
