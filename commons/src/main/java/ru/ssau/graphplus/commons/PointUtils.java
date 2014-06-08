package ru.ssau.graphplus.commons;

import com.sun.star.awt.Point;

import java.awt.geom.Point2D;

/**
 * Created by 1 on 31.05.14.
 */
public class PointUtils {
        public static Point2D subtract(Point2D a, Point2D b)
        {
            return new Point2D.Double(a.getX() - b.getX(), a.getY() - b.getY());
        }

        public static Point2D add(Point2D a, Point2D b)
        {
            return new Point2D.Double(a.getX() + b.getX(), a.getY() + b.getY());
        }

        public static void normalize(Point2D.Double point, int scale)
        {
            double norm = Math.sqrt(point.x * point.x + point.y * point.y);
            if (norm != 0)
            {
                point.x = scale * point.x / norm;
                point.y = scale * point.y / norm;
            }
        }

        public static Point2D interpolate(Point2D a, Point2D b, double p)
        {

            double x = p * a.getX() + (1 - p) * b.getX();
            double y = p * a.getY() + (1 - p) * b.getY();
            return new Point2D.Double(x, y);
        }

    public static Point interpolate(Point a, Point b, double p)
    {

        int x = (int) (p * a.X + (1 - p) * b.X);
        int y = (int) (p * a.Y + (1 - p) * b.Y);
        return new Point(x, y);
    }

        public static Double length(Point2D.Double point)
        {
            return new Point2D.Double(0, 0).distance(point);
        }

    public static double distance(Point position, Point point) {
        return Math.sqrt(Math.pow(position.X - point.X, 2)+Math.pow(position.Y - point.Y, 2));
    }

    public static java.awt.Point convert(Point point){
        return new java.awt.Point(point.X,point.Y);
    }

    public static Point2D convert2D(Point point){
        return new Point2D.Double(point.X,point.Y);
    }


    public static Point2D convert2D(java.awt.Point point){
        return new Point2D.Double(point.getX(),point.getY());
    }

}
