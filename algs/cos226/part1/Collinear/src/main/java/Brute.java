import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Lipatov Nikita
 */
public class Brute {

    public static void main(String[] args) {
        String filename = "rs1423.txt";
        //String filename = "input8.txt";
        if (args != null && args.length >= 1) {
            filename = args[0];
        }

        StdDraw.setXscale(0, 32768);
        StdDraw.setYscale(0, 32768);
        StdDraw.show(0);
        StdDraw.setPenRadius(0.01);  // make the points a bit larger

        // read in the input
        In in = new In(filename);
        int N = in.readInt();
        ArrayList<Point> points = new ArrayList<Point>(N);
        for (int i = 0; i < N; i++) {
            int x = in.readInt();
            int y = in.readInt();
            Point p = new Point(x, y);
            p.draw();
            points.add(p);
        }

        Collections.sort(points);

        drawLine(points);

        // display to screen all at once
        StdDraw.show(0);

        // reset the pen radius
        StdDraw.setPenRadius();
    }

    private static void drawLine(List<Point> points) {
        int size = points.size();
        for (int i = 0; i < size - 3; i++) {
            for (int y = i + 1; y < size - 2; y++) {
                double slope1 = points.get(i).slopeTo(points.get(y));
                for (int m = y + 1; m < size - 1; m++) {
                    double slope2 = points.get(y).slopeTo(points.get(m));
                    if (slope1 == slope2) {
                        for (int n = m + 1; n < size; n++) {
                            double slope3 = points.get(m).slopeTo(points.get(n));
                            if (slope1 == slope3) {
                                drawLine(points, i, y, m, n);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void drawLine(List<Point> points, int i, int y, int m, int n) {
        Point p1 = points.get(i);
        Point p2 = points.get(y);
        Point p3 = points.get(m);
        Point p4 = points.get(n);
        p1.drawTo(p4);
        StdOut.print(p1.toString() + " -> " + p2.toString() + " -> " + p3.toString() + " -> " + p4.toString() + "\n");
    }
}
