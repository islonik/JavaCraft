import java.awt.Color;

/**
 * @author Lipatov Nikita
 */
public class SeamCarver {

    private static final double BORDER_ENERGY = 195075.0;

    private Picture picture;
    private double [][]energy;
    private double [][]distTo;
    private int [][]edgeTo;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        this.picture = picture;

        energy = new double[height()][width()];

        // 2d energy array using the energy()
        for (int row = 0; row < height(); row++) { // rows
            for (int col = 0; col < width(); col++) { // columns
                energy[row][col] = energy(col, row);
            }
        }
    }

    // current picture
    public Picture picture() {
        return picture;
    }

    // width of current picture
    public int width() {
        return picture.width();
    }

    // height of current picture
    public int height() {
        return picture.height();
    }

    // energy of pixel at column x and row y
    public double energy(int col, int row) {
        if (col < 0 || col > width() - 1 || row < 0 || row > height() - 1) {
            throw new IndexOutOfBoundsException();
        }

        if (col + 1 >= width() || col <= 0 || row + 1 >= height() || row <= 0) {
            return BORDER_ENERGY;
        }

        return calculateX(col, row) + calculateY(col, row);
    }

    private double calculateX(int col, int row) {
        Color colorRight = picture.get(col + 1, row);
        Color colorLeft  = picture.get(col - 1, row);

        return Math.pow(Math.abs(colorRight.getRed()   - colorLeft.getRed()),   2.0)
             + Math.pow(Math.abs(colorRight.getBlue()  - colorLeft.getBlue()),  2.0)
             + Math.pow(Math.abs(colorRight.getGreen() - colorLeft.getGreen()), 2.0);

    }

    private double calculateY(int col, int row) {
        Color colorDown = picture.get(col, row + 1);
        Color colorUp   = picture.get(col, row - 1);

        return Math.pow(Math.abs(colorDown.getRed()   - colorUp.getRed()),   2.0)
             + Math.pow(Math.abs(colorDown.getBlue()  - colorUp.getBlue()),  2.0)
             + Math.pow(Math.abs(colorDown.getGreen() - colorUp.getGreen()), 2.0);
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        int []seam = new int[height()];

        // distTo and edgeTo should calculate each time
        distTo = new double[height()][width()];
        edgeTo = new int[height()][width()];

        // init distTo
        for (int row = 0; row < height(); row++) { // rows
            for (int col = 0; col < width(); col++) { // columns
                distTo[row][col] = -1;
            }
        }

        // init distTo and edgeTo for first row
        for (int col = 0; col < width(); col++) { // columns
            distTo[0][col] = energy[0][col];
            edgeTo[0][col] = -1; // init value
        }

        // calculate best paths
        for (int row = 0; row < height() - 1; row++) { // rows
            for (int col = 0; col < width(); col++) { // columns
                verticalRelax(col, row);
            }
        }

        // find best cell
        int bestX = width() - 1;
        double best = distTo[height() - 2][bestX];
        for (int col = 0; col < width(); col++) {
            if (distTo[height() - 2][col] < best) {
                best = distTo[height() - 2][col];
                bestX = col;
            }
        }

        // build seam
        seam[height() - 1] = bestX - 1;
        seam[height() - 2] = bestX;
        int gap = edgeTo[height() - 2][bestX];
        for (int row = height() - 3; row >= 0; row--) {
            bestX = bestX + gap;
            seam[row] = bestX;
            gap = edgeTo[row][bestX];
        }

        return seam;
    }

    // column x and row y
    private void verticalRelax(int col, int row) {
        if (col == 0) {
            verticalEvaluate(col, row, col, row + 1);
            verticalEvaluate(col, row, col + 1, row + 1);
        } else if (col + 1 == width()) {
            verticalEvaluate(col, row, col - 1, row + 1);
            verticalEvaluate(col, row, col, row + 1);
        } else {
            verticalEvaluate(col, row, col - 1, row + 1);
            verticalEvaluate(col, row, col, row + 1);
            verticalEvaluate(col, row, col + 1, row + 1);
        }
    }

    // column x and row y
    private void verticalEvaluate(int parentX, int parentY, int col, int row) {

        double temp = distTo[parentY][parentX] + energy[row][col];
        if (distTo[row][col] == -1 || temp < distTo[row][col]) {
            distTo[row][col] = temp;
            if (col < parentX) {
                edgeTo[row][col] =  1;
            } else if (col == parentX) {
                edgeTo[row][col] =  0;
            } else {
                edgeTo[row][col] = -1;
            }
        }
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        int []seam = new int[width()];

        // distTo and edgeTo should calculate each time
        distTo = new double[height()][width()];
        edgeTo = new int[height()][width()];

        // init distTo
        for (int row = 0; row < height(); row++) { // rows
            for (int col = 0; col < width(); col++) { // columns
                distTo[row][col] = -1;
            }
        }

        // init distTo and edgeTo for first column
        for (int row = 0; row < height(); row++) { // rows
            distTo[row][0] = energy[row][0];
            edgeTo[row][0] = -1; // init value
        }

        // calculate best paths
        for (int col = 0; col < width() - 1; col++) { // columns
            for (int row = 0; row < height(); row++) { // rows
                horizontalRelax(col, row);
            }
        }

        // find best cell
        int bestY = width() - 1;
        double best = distTo[0][bestY];
        for (int row = 0; row < height(); row++) { // rows
            if (distTo[row][width() - 2] < best) {
                best = distTo[row][width() - 2];
                bestY = row;
            }
        }

        // build seam
        seam[width() - 1] = bestY - 1;
        seam[width() - 2] = bestY;
        int gap = edgeTo[bestY][width() - 2];
        for (int col = width() - 3; col >= 0; col--) {
            bestY = bestY + gap;
            seam[col] = bestY;
            gap = edgeTo[bestY][col];
        }

        return seam;
    }

    // column x and row y
    private void horizontalRelax(int x, int y) {
        if (y == 0) {
            horizontalEvaluate(x, y, x + 1, y);
            horizontalEvaluate(x, y, x + 1, y + 1);
        } else if (y + 1 == height()) {
            horizontalEvaluate(x, y, x + 1, y - 1);
            horizontalEvaluate(x, y, x + 1, y);
        } else {
            horizontalEvaluate(x, y, x + 1, y - 1);
            horizontalEvaluate(x, y, x + 1, y);
            horizontalEvaluate(x, y, x + 1, y + 1);
        }
    }

    // column x and row y
    private void horizontalEvaluate(int parentX, int parentY, int x, int y) {

        double temp = distTo[parentY][parentX] + energy[y][x];
        if (distTo[y][x] == -1 || temp < distTo[y][x]) {
            distTo[y][x] = temp;
            if (parentY < y) {
                edgeTo[y][x] = -1;
            } else if (y == parentY) {
                edgeTo[y][x] = 0;
            } else {
                edgeTo[y][x] = 1;
            }
        }
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (width() <= 1) {
            throw new IllegalArgumentException();
        }

        if (seam.length != height()) {
            throw new IllegalArgumentException();
        }

        Picture result = new Picture(width() - 1, height());
        for (int row = 0; row < height(); row++) {
            int prev = seam[row];

            if (seam[row] < 0 || seam[row] >= width()) {
                throw new IndexOutOfBoundsException();
            }

            if (seam[row] < prev - 1 || seam[row] > prev + 1) {
                throw new IllegalArgumentException();
            }

            for (int col = 0; col < width() - 1; col++) {
                if (col < prev) {
                    result.set(col, row, picture.get(col, row));
                } else {
                    result.set(col, row, picture.get(col + 1, row));
                }
            }
        }

        picture = result;

        // possible improvement - avoid recomputing the parts of the energy matrix that don't change.
        energy = new double[height()][width()];
        // 2d energy array using the energy()
        for (int row = 0; row < height(); row++) { // rows
            for (int col = 0; col < width(); col++) { // columns
                energy[row][col] = energy(col, row);
            }
        }
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (height() <= 1) {
            throw new IllegalArgumentException();
        }

        if (seam.length != width()) {
            throw new IllegalArgumentException();
        }

        Picture result = new Picture(width(), height() - 1);
        for (int col = 0; col < width(); col++) {
            int prev = seam[col];

            if (seam[col] < 0 || seam[col] >= height()) {
                throw new IndexOutOfBoundsException();
            }

            if (seam[col] < prev - 1 || seam[col] > prev + 1) {
                throw new IllegalArgumentException();
            }

            for (int row = 0; row < height() - 1; row++) {
                if (row < prev) {
                    result.set(col, row, picture.get(col, row));
                } else {
                    result.set(col, row, picture.get(col, row + 1));
                }
            }
        }

        picture = result;

        // possible improvement - avoid recomputing the parts of the energy matrix that don't change.
        energy = new double[height()][width()];
        // 2d energy array using the energy()
        for (int row = 0; row < height(); row++) { // rows
            for (int col = 0; col < width(); col++) { // columns
                energy[row][col] = energy(col, row);
            }
        }

    }

}
