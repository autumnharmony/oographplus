package ru.ssau.graphplus.commons;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class GeomUtils
{
    public static boolean findRectangleLineIntersection(Rectangle2D.Double rectangle, Point2D.Double inside, Point2D.Double outside, Point2D.Double targetPoint)
    {
        if (rectangle == null || inside == null || outside == null || targetPoint == null)
        {
            throw new IllegalArgumentException("Any argument shouldn't be null");
        }

        Point2D.Double lt = new Point2D.Double(rectangle.x, rectangle.y);
        Point2D.Double rt = new Point2D.Double(rectangle.x + rectangle.width, rectangle.y);

        Point2D.Double rb = new Point2D.Double(rectangle.x + rectangle.width, rectangle.y + rectangle.height);
        Point2D.Double lb = new Point2D.Double(rectangle.x, rectangle.y + rectangle.height);

        Double topY = topY(rectangle);
        Double bottomY = bottomY(rectangle);

        Double leftX = leftX(rectangle);
        Double rightX = rightX(rectangle);

        if (!insideRectangle(inside, rectangle) || !outsideOfRectangle(outside, rectangle))
        {
            return false;
        }

        if (inside.x == outside.x)
        {
            // vertical
            if (outside.y < topY)
            {
                // intersects with top
                targetPoint.x = inside.x;
                targetPoint.y = topY;
                return true;
            }
            else if (outside.y > bottomY)
            {
                // intersects with bottom
                targetPoint.x = inside.x;
                targetPoint.y = bottomY;
                return true;
            }
        }
        else if (inside.y == outside.y)
        {
            // horizontal
            if (outside.x > rightX)
            {
                // intersects with right
                targetPoint.x = rightX;
                targetPoint.y = inside.y;
                return true;
            }
            else if (outside.x < leftX)
            {
                // intersects with left
                targetPoint.x = leftX;
                targetPoint.y = inside.y;
                return true;
            }
        }
        else
        {
            // determine which side intersecting
            // y = kx+b

            double[] doubles;

            double k;
            double b;

            double k1;
            double b1;

            double k2;
            double b2;

            doubles = lineKB(inside, outside);
            k = doubles[0];
            b = doubles[1];
            // y = kx+b - it's line from inside to outside

            // right side ?
            doubles = lineKB(rt, inside);
            k1 = doubles[0];
            b1 = doubles[1];

            doubles = lineKB(rb, inside);
            k2 = doubles[0];
            b2 = doubles[1];


            if (pointIsUnderLine(outside, k1, b1) && pointIsBelowLine(outside, k2, b2))
            {
                // right side
                targetPoint.x = rightX;
                targetPoint.y = k * rightX + b;
                return true;
            }

            // left side ?
            doubles = lineKB(lt, inside);

            k1 = doubles[0];
            b1 = doubles[1];

            doubles = lineKB(lb, inside);

            k2 = doubles[0];
            b2 = doubles[1];
            if (pointIsUnderLine(outside, k1, b1) && pointIsBelowLine(outside, k2, b2))
            {
                // left side
                targetPoint.x = leftX;
                targetPoint.y = k * leftX + b;
                return true;
            }

            // top side ?
            doubles = lineKB(lt, inside);
            k1 = doubles[0];
            b1 = doubles[1];

            doubles = lineKB(rt, inside);

            k2 = doubles[0];
            b2 = doubles[1];
            if (pointIsBelowLine(outside, k1, b1) && pointIsBelowLine(outside, k2, b2))
            {
                // top side
                targetPoint.y = topY;
                targetPoint.x = (topY - b) / k;
                return true;
            }

            // bottom side ?
            doubles = lineKB(lb, inside);
            k1 = doubles[0];
            b1 = doubles[1];

            doubles = lineKB(rb, inside);

            k2 = doubles[0];
            b2 = doubles[1];

            if (pointIsUnderLine(outside, k1, b1) && pointIsUnderLine(outside, k2, b2))
            {
                // bottom side
                targetPoint.y = bottomY;
                targetPoint.x = (bottomY - b) / k;
                return true;
            }
        }

        throw new IllegalStateException();
    }


    public static List<Point2D> findIntersection(Rectangle2D.Double rectangle, Point2D.Double inside, Point2D.Double outside)

    {
        List<Point2D> intersections = new ArrayList<>();

//        if (rectangle == null || inside == null || outside == null || targetPoint == null)
//        {
//            throw new IllegalArgumentException("Any argument shouldn't be null");
//        }

        Point2D.Double targetPoint;

        Point2D.Double lt = new Point2D.Double(rectangle.x, rectangle.y);
        Point2D.Double rt = new Point2D.Double(rectangle.x + rectangle.width, rectangle.y);

        Point2D.Double rb = new Point2D.Double(rectangle.x + rectangle.width, rectangle.y + rectangle.height);
        Point2D.Double lb = new Point2D.Double(rectangle.x, rectangle.y + rectangle.height);

        Double topY = topY(rectangle);
        Double bottomY = bottomY(rectangle);

        Double leftX = leftX(rectangle);
        Double rightX = rightX(rectangle);

//        if (!insideRectangle(inside, rectangle) || !outsideOfRectangle(outside, rectangle))
//        {
//            return false;
//        }

        if (inside.x == outside.x)
        {
            targetPoint = new Point2D.Double();

            // vertical
            if (outside.y < topY)
            {
                // intersects with top

                targetPoint.x = inside.x;
                targetPoint.y = topY;
                intersections.add(targetPoint);
//                return true;
            }
            else if (outside.y > bottomY)
            {
                // intersects with bottom
                targetPoint.x = inside.x;
                targetPoint.y = bottomY;
                intersections.add(targetPoint);
            }
        }
        else if (inside.y == outside.y)
        {
            targetPoint = new Point2D.Double();

            // horizontal
            if (outside.x > rightX)
            {
                // intersects with right
                targetPoint.x = rightX;
                targetPoint.y = inside.y;
                intersections.add(targetPoint);

            }
            else if (outside.x < leftX)
            {
                // intersects with left
                targetPoint.x = leftX;
                targetPoint.y = inside.y;
                intersections.add(targetPoint);
            }
        }
        else
        {
            // determine which side intersecting
            // y = kx+b

            double[] doubles;

            double k;
            double b;

            double k1;
            double b1;

            double k2;
            double b2;

            doubles = lineKB(inside, outside);
            k = doubles[0];
            b = doubles[1];
            // y = kx+b - it's line from inside to outside

            // right side ?
            doubles = lineKB(rt, inside);
            k1 = doubles[0];
            b1 = doubles[1];

            doubles = lineKB(rb, inside);
            k2 = doubles[0];
            b2 = doubles[1];


            if (pointIsUnderLine(outside, k1, b1) && pointIsBelowLine(outside, k2, b2))
            {
                // right side
                targetPoint = new Point2D.Double();
                targetPoint.x = rightX;
                targetPoint.y = k * rightX + b;
                intersections.add(targetPoint);
            }

            // left side ?
            doubles = lineKB(lt, inside);

            k1 = doubles[0];
            b1 = doubles[1];

            doubles = lineKB(lb, inside);

            k2 = doubles[0];
            b2 = doubles[1];
            if (pointIsUnderLine(outside, k1, b1) && pointIsBelowLine(outside, k2, b2))
            {
                // left side
                targetPoint = new Point2D.Double();
                targetPoint.x = leftX;
                targetPoint.y = k * leftX + b;
                intersections.add(targetPoint);
            }

            // top side ?
            doubles = lineKB(lt, inside);
            k1 = doubles[0];
            b1 = doubles[1];

            doubles = lineKB(rt, inside);

            k2 = doubles[0];
            b2 = doubles[1];
            if (pointIsBelowLine(outside, k1, b1) && pointIsBelowLine(outside, k2, b2))
            {
                // top side
                targetPoint = new Point2D.Double();
                targetPoint.y = topY;
                targetPoint.x = (topY - b) / k;
                intersections.add(targetPoint);
            }

            // bottom side ?
            doubles = lineKB(lb, inside);
            k1 = doubles[0];
            b1 = doubles[1];

            doubles = lineKB(rb, inside);

            k2 = doubles[0];
            b2 = doubles[1];

            if (pointIsUnderLine(outside, k1, b1) && pointIsUnderLine(outside, k2, b2))
            {
                // bottom side
                targetPoint = new Point2D.Double();
                targetPoint.y = bottomY;
                targetPoint.x = (bottomY - b) / k;
                intersections.add(targetPoint);
            }
        }

        return intersections;
    }

    public static boolean findRectangleLineIntersection(Rectangle2D.Double rectangle, double x, double y, double x1, double y1, Point2D.Double targetPoint)
    {
        return findRectangleLineIntersection(rectangle, new Point2D.Double(x, y), new Point2D.Double(x1, y1), targetPoint);
    }


    private static Double rightX(Rectangle2D.Double rect)
    {
        return rect.getX() + rect.getWidth();
    }

    private static Double bottomY(Rectangle2D.Double rect)
    {
        return rect.getY()+rect.getHeight();
    }

    private static Double leftX(Rectangle2D.Double rect)
    {
        return rect.getX();
    }

    private static Double topY(Rectangle2D.Double rect)
    {
        return rect.getY();
    }


    private static boolean outsideOfRectangle(Point2D.Double outside, Rectangle2D.Double rectangle)
    {
        boolean b = outside.x > rightX(rectangle) || outside.x < leftX(rectangle);
        boolean b1 = outside.y < topY(rectangle) || outside.y > bottomY(rectangle);
        return b || b1;
    }

    private static boolean insideRectangle(Point2D.Double inside, Rectangle2D.Double rectangle)
    {
        boolean b = inside.x < rightX(rectangle) && inside.x > leftX(rectangle);
        boolean b1 = inside.y > topY(rectangle) && inside.y < bottomY(rectangle);
        return b && b1;
    }

    public static boolean pointIsUnderLine(Point2D.Double point, double k, double b)
    {
        return point.y >= k * point.x + b;
    }

    public static boolean pointIsBelowLine(Point2D.Double point, double k, double b)
    {
        return point.y < k * point.x + b;
    }

    /**
     * y = kx+b
     * @param a
     * @param z
     * @return
     */
    private static double[] lineKB(Point2D.Double a, Point2D.Double z)
    {
        double yy = a.y - z.y;
        double xx = a.x - z.x;
        double k;
        double b;
        k = yy / xx;
        b = a.y - k * a.x;
        return new double[]{k, b};
    }



}