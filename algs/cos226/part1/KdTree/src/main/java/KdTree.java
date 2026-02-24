import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Lipatov Nikita
 */
public class KdTree {
    private static final boolean IS_VERTICAL = true;
    private static final int IS_RIGHT =  1;
    private static final int IS_LEFT  = -1;
    private static final int IS_ZERO  =  0;

    private int size = 0;
    private Node root;
    private Point2D nearest;
    private double shortestWay = 2.0;

    public KdTree() {
    }

    public boolean isEmpty() {
        if (root == null) {
            return true;
        }
        return false;
    }

    public int size() {
        return size;
    }

    public void insert(Point2D p) {
        if (isEmpty()) {
            root = new Node(p, new RectHV(0, 0, 1, 1));
            size++;
        } else if (!contains(p)) {
            insertTree(root, root, p, IS_VERTICAL, IS_ZERO);
            size++;
        }
    }

    private static Node insertTree(Node root, Node parent, Point2D point, boolean isVertical, int order) {
        if (parent == null) {
            RectHV rect = createRectHV(root, !isVertical, order);
            return new Node(point, rect);
        }

        int cmp = compare(isVertical, parent.p, point);
        if (cmp < 0) {
            parent.left  = insertTree(parent, parent.left,  point, !isVertical, IS_LEFT);
        } else if (cmp > 0) {
            parent.right = insertTree(parent, parent.right, point, !isVertical, IS_RIGHT);
        }
        return parent;
    }

    private static RectHV createRectHV(Node root, boolean isVertical, int order) {
        RectHV rect = null;
        if (isVertical) {
            if (IS_LEFT == order) {
                rect = new RectHV(root.rect.xmin(), root.rect.ymin(), root.p.x(), root.rect.ymax());
            } else {
                rect = new RectHV(root.p.x(), root.rect.ymin(), root.rect.xmax(), root.rect.ymax());
            }
        } else { // isHorizontal
            if (IS_LEFT == order) {
                rect = new RectHV(root.rect.xmin(), root.rect.ymin(), root.rect.xmax(), root.p.y());
            } else {
                rect = new RectHV(root.rect.xmin(), root.p.y(), root.rect.xmax(), root.rect.ymax());
            }
        }
        return rect;
    }

    private static int compare(boolean isVertical, Point2D p1, Point2D p2) {
        if (isVertical) {
            double xdif = p2.x() - p1.x();
            if (xdif < 0) {
                return -1;
            } else if (xdif > 0) {
                return  1;
            } else {
                double ydif = p2.y() - p1.y();
                if (ydif < 0) {
                    return -1;
                } else if (ydif > 0) {
                    return  1;
                } else {
                    return  0;
                }
            }
        } else {
            double ydif = p2.y() - p1.y();
            if (ydif < 0) {
                return -1;
            } else if (ydif > 0) {
                return  1;
            } else {
                double xdif = p2.x() - p1.x();
                if (xdif < 0) {
                    return -1;
                } else if (xdif > 0) {
                    return  1;
                } else {
                    return  0;
                }
            }
        }
    }

    public boolean contains(Point2D p) {
        return contains(root, p, IS_VERTICAL);
    }

    private static boolean contains(Node parent, Point2D point, boolean isVertical) {
        if (parent == null) {
            return false;
        } else if (parent.p.equals(point)) {
            return true;
        }
        int cmp = compare(isVertical, parent.p, point);
        if (cmp < 0) {
            return contains(parent.left,  point, !isVertical);
        } else if (cmp > 0) {
            return contains(parent.right, point, !isVertical);
        }
        return false;
    }

    public void draw() {
        draw(root, IS_VERTICAL);
    }

    private static void draw(Node parent, boolean isVertical) {
        StdDraw.setPenRadius(0.005);
        StdDraw.setPenColor(StdDraw.BLACK);  // point
        parent.p.draw();
        if (isVertical) {
            StdDraw.setPenRadius(0.001);
            StdDraw.setPenColor(StdDraw.RED);  // vertical
            StdDraw.line(parent.p.x(), parent.rect.ymin(), parent.p.x(), parent.rect.ymax());
        } else {
            StdDraw.setPenRadius(0.001);
            StdDraw.setPenColor(StdDraw.BLUE); // horizontal
            StdDraw.line(parent.rect.xmin(), parent.p.y(), parent.rect.xmax(), parent.p.y());
        }

        if (parent.left != null) {
            draw(parent.left, !isVertical);
        }
        if (parent.right != null) {
            draw(parent.right, !isVertical);
        }
    }

    public Iterable<Point2D> range(RectHV rectHV) {
        final List<Point2D> rangePoints = new ArrayList<Point2D>();
        if (!isEmpty()) {
            range(rangePoints, root, rectHV, IS_VERTICAL);
        }
        return new Iterable<Point2D>() {
            public Iterator<Point2D> iterator() {
                return rangePoints.iterator();
            }
        };
    }

    private static void range(List<Point2D> list, Node parent, RectHV rect, boolean isVertical) {
        if (!list.contains(parent.p) && parent.rect.intersects(rect)) {
            if (rect.contains(parent.p)) {
                list.add(parent.p);
            }
            if (parent.left != null) {
                range(list, parent.left,  rect, !isVertical);
            }
            if (parent.right != null) {
                range(list, parent.right, rect, !isVertical);
            }
        }
    }

    public Point2D nearest(Point2D p) {
        shortestWay = 2.0;
        nearest = null;

        findNearest(root, p);

        return nearest;
    }

    private void findNearest(Node parent, Point2D point) {
        if (parent == null) {
            return;
        }

        double currentDistance = point.distanceTo(parent.p);
        if (shortestWay > currentDistance) {
            nearest = parent.p;
            shortestWay = currentDistance;
        }
        double tempShortestWay = shortestWay;

        if (parent.left != null  && tempShortestWay >= parent.left.rect.distanceTo(point)) {
            findNearest(parent.left, point);
        }

        if (parent.right != null && tempShortestWay >= parent.right.rect.distanceTo(point)) {
            findNearest(parent.right, point);
        }
    }

    private static class Node {
        private Point2D p;      // the point
        private RectHV rect;    // the axis-aligned rectangle corresponding to this node
        private Node left;      // the left/bottom subtree
        private Node right;     // the right/top subtree

        public Node(Point2D p) {
            this.p = p;
        }

        public Node(Point2D p, RectHV rect) {
            this.p = p;
            this.rect = rect;
        }
    }

    // unit testing of the methods (optional)
    public static void main(String[] args) {

    }


}
