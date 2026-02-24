import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * @author Lipatov Nikita
 */
public class Fast {

    public static void main(String[] args) {
        String filename = "rs1423.txt";
        //String filename = "input20.txt";
        //String filename = "equidistant.txt";
        if (args != null && args.length >= 1) {
            filename = args[0];
        }

        //int border = 1000;
        // rescale coordinates and turn on animation mode
        //StdDraw.setXscale(0 - border, 32768 + border);
        //StdDraw.setYscale(0 - border, 32768 + border);
        StdDraw.setXscale(0, 32768);
        StdDraw.setYscale(0, 32768);
        StdDraw.show(0);
        StdDraw.setPenRadius(0.01);  // make the points a bit larger

        // read in the input
        In in = new In(filename);
        int N = in.readInt();
        Point []points = new Point[N];
        for (int i = 0; i < N; i++) {
            int x = in.readInt();
            int y = in.readInt();
            Point p = new Point(x, y);
            p.draw();
            points[i] = p;
        }

        Arrays.sort(points);

        drawLine(points);

        PrintLine.nullify();

        // display to screen all at once
        StdDraw.show(0);

        // reset the pen radius
        StdDraw.setPenRadius();
    }

    /**
     1) Think of p as the origin.
     2) For each other point q, determine the slope it makes with p.
     3) Sort the points according to the slopes they makes with p.
     4) Check if any 3 (or more) adjacent points in the sorted order have equal slopes with respect to p. If so, these points, together with p, are collinear.
     */
    private static void drawLine(Point []points) {
        int n = points.length;
        PrintLine printLine = new PrintLine();
        for (int i = 0; i < n; i++) {
            Point p = points[i]; // 1 point

            Point []temp = new Point[n-1];
            int index = 0;
            for (int k = 0; k < n; k++) { // 2 point
                if (i != k) {
                    temp[index] = points[k];
                    index++;
                }
            }

            Arrays.sort(temp, i, n-1, p.SLOPE_ORDER); // 3 point

            double lastSlope = p.slopeTo(p);
            int changeSlopeIterator = 1;
            for (int m = 0; m < temp.length; m++) { // 4 point
                double currentSlope = p.slopeTo(temp[m]);

                if (currentSlope == lastSlope) {
                    changeSlopeIterator++;
                } else { // always first attempt, because we should change slope level
                    changeSlopeIterator = 1;
                    lastSlope = currentSlope;
                }

                // 4 - min line on map; 3 min line in array
                if (changeSlopeIterator >= 3) {
                    printLine.add(p);
                    for (int j = m - changeSlopeIterator + 1; j <= m; j++) {
                        printLine.add(temp[j]);
                    }
                }
            }
            printLine.print();
            printLine = new PrintLine();
        }
    }

    private static class PrintLine {
        private static List<PrintLine> alreadyWasPrinted = new ArrayList<PrintLine>();
        private ArrayList<Point> line = new ArrayList<Point>();

        public PrintLine() {
        }

        public PrintLine(PrintLine that) {
            this.line = new ArrayList<Point>(that.line);
        }

        public static void nullify() {
            alreadyWasPrinted = new ArrayList<PrintLine>();
        }

        public void add(Point point) {
            if (!line.contains(point)) {
                if (line.size() > 3) {
                    Point first = line.get(0);
                    Point last  = line.get(line.size() - 1);

                    double psq = first.slopeTo(last);
                    double psr = first.slopeTo(point);

                    if (psq != psr) {
                        print();
                        line = new ArrayList<Point>();
                        line.add(first);
                    }
                }
                line.add(point);
            }
        }

        public void print() {
            int lineSize = line.size();
            if (lineSize < 4) {
                return;
            }

            if (!alreadyWasPrinted.contains(this)) {
                for (int a = 0; a < lineSize; a++) {
                    Point p1 = line.get(a);

                    StdOut.print(p1.toString());
                    if (a + 1 < lineSize) {
                        StdOut.print(" -> ");
                    } else {
                        StdOut.print("\n");
                    }
                }
                // draw end points
                line.get(0).drawTo(line.get(lineSize - 1));
                alreadyWasPrinted.add(new PrintLine(this));
            }
        }

        /**
         input20.txt:
         (4096, 20992) -> (5120, 20992) -> (6144, 20992) -> (7168, 20992) -> (8128, 20992)
         (4096, 20992) -> (4096, 22016) -> (4096, 23040) -> (4096, 24064) -> (4096, 25088)
         (4096, 25088) -> (5120, 25088) -> (7168, 25088) -> (8192, 25088)
         (8192, 25088) -> (8192, 26112) -> (8192, 27136) -> (8192, 28160) -> (8192, 29184)
         (4160, 29184) -> (5120, 29184) -> (6144, 29184) -> (7168, 29184) -> (8192, 29184)
         */
        public boolean equals(Object o1) {
            PrintLine that = (PrintLine) o1;
            int index = 0; // similar points
            if (this.line.size() >= that.line.size()) {
                int minSize = that.line.size();
                for (int i = 0; i < minSize; i++) {
                    if (this.line.contains(that.line.get(i))) {
                        index++;
                        if (index >= 2) {
                            return true;
                        }
                    }
                }
            } else {
                int minSize = this.line.size();
                for (int i = 0; i < minSize; i++) {
                    if (that.line.contains(this.line.get(i))) {
                        index++;
                        if (index >= 2) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

}
