import java.util.Comparator;

public class Solver {
    private final Comparator<SearchNode> hammingComparator = new HammingComparator();
    private final Comparator<SearchNode> manhattanComparator = new ManhattanComparator();
    private final Comparator<SearchNode> manhattanAndHammingComparator = new ManhattanAndHammingComparator();

    private MinPQ<SearchNode> pq;
    private MinPQ<SearchNode> pqTwin;

    private SearchNode goal = null;
    private boolean solvable = true;

    /**
     * find a solution to the initial board (using the A* algorithm)
     * @param initial
     */
    public Solver(Board initial) {
        pq = new MinPQ<SearchNode>(manhattanComparator);
        pqTwin = new MinPQ<SearchNode>(hammingComparator);

        SearchNode initNode = new SearchNode(null, initial, (short) 0);
        pq.insert(initNode);

        tryToSolve();
    }

    private class HammingComparator implements Comparator<SearchNode> {
        public int compare(SearchNode n1, SearchNode n2) {
            return n1.board.hamming()
                    - n2.board.hamming();
        }
    }

    private class ManhattanComparator implements Comparator<SearchNode> {
        public int compare(SearchNode n1, SearchNode n2) {
            return n1.board.manhattan()
                    + n1.moves
                    - n2.board.manhattan()
                    - n2.moves;
        }
    }

    private class ManhattanAndHammingComparator implements Comparator<SearchNode> {
        public int compare(SearchNode n1, SearchNode n2) {
            return n1.board.manhattan()
                    + n1.moves
                    - n2.board.manhattan()
                    - n2.moves
                    + n1.board.hamming()
                    - n2.board.hamming();
        }
    }

    // we cannot move prev and moves values to Board class
    private class SearchNode {
        private SearchNode prev;
        private Board board;
        private short moves;

        public SearchNode(SearchNode p, Board b, short m) {
            prev = p;
            board = b;
            moves = m;
        }

        public boolean equals(Object y) {
            if (y == this) {
                return true;
            }
            if (y == null) {
                return false;
            }
            if (y.getClass() != this.getClass()) {
                return false;
            }

            SearchNode that = (SearchNode) y;
            if (that.board.equals(board)) {
                return true;
            }
            return false;
        }

    }

    private void tryToSolve() {
        boolean isChangedComparator = false;
        long startTime = System.currentTimeMillis();
        while (true) {
            if (!isChangedComparator) {
                if (((System.currentTimeMillis() - startTime) / 1000) > 20) {
                    MinPQ<SearchNode> tempPq = new MinPQ<SearchNode>(manhattanAndHammingComparator);
                    while (pq.size() > 0) {
                        tempPq.insert(pq.delMin());
                    }
                    pq = tempPq;
                    isChangedComparator = true;
                }
            }
            // solve pq
            SearchNode node = pq.delMin();
            if (node.board.isGoal()) {
                goal = node; // solved
                break;
            }

            for (Board board : node.board.neighbors()) {
                SearchNode n = new SearchNode(node, board, (short) (node.moves + 1));
                if (n.equals(node.prev)) {
                    continue;
                }

                pq.insert(n);
            }

            // solve twin
            SearchNode twinNode = new SearchNode(null, node.board.twin(), (short) 0);
            pqTwin.insert(twinNode);

            twinNode = pqTwin.delMin();
            if (twinNode.board.isGoal()) {
                solvable = false; // impossible to solve
                break;
            }

            for (Board board : twinNode.board.neighbors()) {

                SearchNode n = new SearchNode(node, board, (short) (node.moves + 1));
                if (n.equals(twinNode.prev)) {
                    continue;
                }

                pqTwin.insert(n);
            }
        }
    }

    /**
     * @return is the initial board solvable?
     */
    public boolean isSolvable() {
        return solvable;
    }

    /**
     * @return min number of moves to solve initial board; -1 if no solution
     */
    public int moves() {
        if (!solvable) {
            return -1;
        }
        return goal.moves;
    }

    /**
     * @return sequence of boards in a shortest solution; null if no solution
     */
    public Iterable<Board> solution() {
        if (goal == null) {
            return null;
        }

        Stack<Board> stack = new Stack<Board>();
        SearchNode node = goal;
        stack.push(node.board);
        while (node.prev != null) {
            stack.push(node.prev.board);
            node = node.prev;
        }

        return stack;
    }

    /**
     * solve a slider puzzle 
     * @param args
     */
    public static void main(String[] args) {
        //String pathname = "puzzle04.txt";
        //String pathname = "puzzle4x4-78.txt";
        //String pathname = "puzzle36.txt";
        String pathname = "puzzle46.txt";
        //String pathname = "puzzle49.txt";
        //String pathname = "puzzle4x4-unsolvable.txt";
        //String pathname = "puzzle4x4-hard2.txt";
        if (args != null && args.length > 0) {
            pathname = args[0];
        }

        // create initial board from file
        In in = new In(pathname);
        int N = in.readInt();
        int[][] blocks = new int[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                blocks[i][j] = in.readInt();
            }
        }
        Board initial = new Board(blocks);

        // solve the puzzle
        long startDate = System.currentTimeMillis();
        Solver solver = new Solver(initial);
        long endDate = System.currentTimeMillis();
        System.out.println("Difference = " + (endDate - startDate));

        // print solution to standard output
        if (!solver.isSolvable()) {
            StdOut.println("No solution possible");
        } else {
            StdOut.println("Minimum number of moves = " + solver.moves());
            for (Board board : solver.solution()) {
                StdOut.println(board);
            }
        }
    }
}