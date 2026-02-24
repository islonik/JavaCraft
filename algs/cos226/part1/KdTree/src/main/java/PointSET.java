import java.util.Set;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

/**
 * @author Lipatov Nikita
 */
public class PointSET {
    private static final double DEGREE = 2.0;

    private Set<Point2D> points;

    // construct an empty set of points
    public PointSET() {
        points = new TreeSet<Point2D>();
    }

    // is the set empty?
    public boolean isEmpty() {
        return points.isEmpty();
    }

    // number of points in the set
    public int size() {
        return points.size();
    }

    // add the point to the set (if it is not already in the set)
    public void insert(Point2D p) {
        points.add(p);
    }

    // does the set contain point p?
    public boolean contains(Point2D p) {
        return points.contains(p);
    }

    // draw all points to standard draw
    public void draw() {
        Iterator<Point2D> point2DIterator = points.iterator();
        while (point2DIterator.hasNext()) {
            Point2D point2D = point2DIterator.next();
            point2D.draw();
        }
    }

    // all points that are inside the rectangle
    public Iterable<Point2D> range(RectHV rect) {
        final List<Point2D> rangePoints = new LinkedList<Point2D>();

        Iterator<Point2D> point2DIterator = points.iterator();
        while (point2DIterator.hasNext()) {
            Point2D tempPoint2D = point2DIterator.next();
            if (tempPoint2D.x() <= rect.xmax() && tempPoint2D.x() >= rect.xmin() && tempPoint2D.y() <= rect.ymax() && tempPoint2D.y() >= rect.ymin()) {
                rangePoints.add(tempPoint2D);
            }
        }

        return new Iterable<Point2D>() {
            public Iterator<Point2D> iterator() {
                return rangePoints.iterator();
            }
        };
    }

    // a nearest neighbor in the set to point p; null if the set is empty
    public Point2D nearest(Point2D p) {
        if (isEmpty()) {
            return null;
        }

        Point2D nearestNeighbor = null;
        double shortestHypotenuse = 2.0;

        Iterator<Point2D> point2DIterator = points.iterator();
        while (point2DIterator.hasNext()) {
            Point2D tempPoint2D = point2DIterator.next();
            double tempHypotenuse = calcDistance(p, tempPoint2D);
            if (nearestNeighbor == null || tempHypotenuse < shortestHypotenuse) {
                nearestNeighbor = tempPoint2D;
                shortestHypotenuse = tempHypotenuse;
            }
        }
        return nearestNeighbor;
    }

    private static double calcDistance(Point2D that, Point2D temp) {
        return Math.sqrt(Math.pow(Math.abs(that.x() - temp.x()), DEGREE) + Math.pow(Math.abs(that.y() - temp.y()), DEGREE));
    }

    // unit testing of the methods (optional)
    public static void main(String[] args) {

    }
}
