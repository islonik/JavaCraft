import org.junit.*;
import org.junit.Test;

import java.util.Iterator;

/**
 * @author Lipatov Nikita
 */
public class BoardTest {

    /**
     8  1  3
     4  0  2
     7  6  5
     */
    private int[][] blocks3() {
        int N = 3;
        int [][]blocks = new int[N][N];
        blocks[0][0] = 8;
        blocks[0][1] = 1;
        blocks[0][2] = 3;
        blocks[1][0] = 4;
        blocks[1][1] = 0;
        blocks[1][2] = 2;
        blocks[2][0] = 7;
        blocks[2][1] = 6;
        blocks[2][2] = 5;

        return blocks;
    }

    /**
     0  1  3
     4  8  2
     7  6  5
     */
    private int[][] blocksCorner1() {
        int N = 3;
        int [][]blocks = new int[N][N];
        blocks[0][0] = 0;
        blocks[0][1] = 1;
        blocks[0][2] = 3;
        blocks[1][0] = 4;
        blocks[1][1] = 8;
        blocks[1][2] = 2;
        blocks[2][0] = 7;
        blocks[2][1] = 6;
        blocks[2][2] = 5;

        return blocks;
    }

    /**
     8  1  3
     4  5  2
     7  6  0
     */
    private int[][] blocksCorner2() {
        int N = 3;
        int [][]blocks = new int[N][N];
        blocks[0][0] = 8;
        blocks[0][1] = 1;
        blocks[0][2] = 3;
        blocks[1][0] = 4;
        blocks[1][1] = 5;
        blocks[1][2] = 2;
        blocks[2][0] = 7;
        blocks[2][1] = 6;
        blocks[2][2] = 0;

        return blocks;
    }

    @Test
    public void testDimension() {
        Board board = new Board(blocks3());
        Assert.assertEquals(3, board.dimension());
    }

    @Test
    public void testToString() {
        Board board = new Board(blocks3());
        Assert.assertEquals(
                "3\n" +
                        " 8  1  3 \n" +
                        " 4  0  2 \n" +
                        " 7  6  5 \n",
                board.toString()
        );
    }

    @Test
    public void testHamming() {
        Board board = new Board(blocks3());
        Assert.assertEquals(5, board.hamming());
    }

    @Test
    public void testManhattan() {
        Board board = new Board(blocks3());
        Assert.assertEquals(10, board.manhattan());
    }

    /**
     Case 1:          Case 2:          Case 3:          Case 4:          Case 5:          Case 6:
    priority  = 4    priority  = 6    priority  = 6    priority  = 6    priority  = 6    priority  = 6
    moves     = 4    moves     = 3    moves     = 4    moves     = 2    moves     = 3    moves     = 1
    manhattan = 0    manhattan = 3    manhattan = 2    manhattan = 4    manhattan = 3    manhattan = 5

    3                3                3                3                3                3
      1  2  3          1  2  3          1  2  0          1  3  0          1  2  3          4  1  3
      4  5  6          0  4  5          4  5  3          4  2  5          4  8  5          0  2  5
      7  8  0          7  8  6          7  8  6          7  8  6          7  0  6          7  8  6
    **/

    @Test
    public void testCase1() {
        int N = 3;
        int [][]blocks = new int[N][N];
        blocks[0][0] = 1;
        blocks[0][1] = 2;
        blocks[0][2] = 3;
        blocks[1][0] = 4;
        blocks[1][1] = 5;
        blocks[1][2] = 6;
        blocks[2][0] = 7;
        blocks[2][1] = 8;
        blocks[2][2] = 0;

        Board board = new Board(blocks);
        Assert.assertEquals(0, board.hamming());
        Assert.assertEquals(0, board.manhattan());
    }

    @Test
    public void testCase2() {
        int N = 3;
        int [][]blocks = new int[N][N];
        blocks[0][0] = 1;
        blocks[0][1] = 2;
        blocks[0][2] = 3;
        blocks[1][0] = 0;
        blocks[1][1] = 4;
        blocks[1][2] = 5;
        blocks[2][0] = 7;
        blocks[2][1] = 8;
        blocks[2][2] = 6;

        Board board = new Board(blocks);
        Assert.assertEquals(3, board.hamming());
        Assert.assertEquals(3, board.manhattan());

    }

    @Test
    public void testCase3() {
        int N = 3;
        int [][]blocks = new int[N][N];
        blocks[0][0] = 1;
        blocks[0][1] = 2;
        blocks[0][2] = 0;
        blocks[1][0] = 4;
        blocks[1][1] = 5;
        blocks[1][2] = 3;
        blocks[2][0] = 7;
        blocks[2][1] = 8;
        blocks[2][2] = 6;

        Board board = new Board(blocks);
        Assert.assertEquals(2, board.hamming());
        Assert.assertEquals(2, board.manhattan());
    }

    @Test
    public void testCase4() {
        int N = 3;
        int [][]blocks = new int[N][N];
        blocks[0][0] = 1;
        blocks[0][1] = 2;
        blocks[0][2] = 3;
        blocks[1][0] = 4;
        blocks[1][1] = 8;
        blocks[1][2] = 5;
        blocks[2][0] = 7;
        blocks[2][1] = 0;
        blocks[2][2] = 6;

        Board board = new Board(blocks);
        Assert.assertEquals(3, board.hamming());
        Assert.assertEquals(3, board.manhattan());
    }

    @Test
    public void testCase5() {
        int N = 3;
        int [][]blocks = new int[N][N];
        blocks[0][0] = 4;
        blocks[0][1] = 1;
        blocks[0][2] = 3;
        blocks[1][0] = 0;
        blocks[1][1] = 2;
        blocks[1][2] = 5;
        blocks[2][0] = 7;
        blocks[2][1] = 8;
        blocks[2][2] = 6;

        Board board = new Board(blocks);
        Assert.assertEquals(5, board.hamming());
        Assert.assertEquals(5, board.manhattan());
    }

    @Test
    public void testTwin() {
        Board board = new Board(blocks3());
        Board twin = board.twin();
        Assert.assertFalse(board.equals(twin));
        twin = board.twin();
        Assert.assertFalse(board.equals(twin));
        twin = board.twin();
        Assert.assertFalse(board.equals(twin));
        twin = board.twin();
        Assert.assertFalse(board.equals(twin));
        twin = board.twin();
        Assert.assertFalse(board.equals(twin));
        twin = board.twin();
        Assert.assertFalse(board.equals(twin));


        /*
        long sumTime = 0;
        for(int attempt = 0; attempt < 100; attempt++) {
            long startTime = System.currentTimeMillis();
            for(int i = 0; i < 100000; i++) {
                twin = board.twin();
                Assert.assertFalse(board.equals(twin));
            }
            long endTime = System.currentTimeMillis();
            sumTime += (endTime - startTime);
        }
        System.out.println("Average dif time = " + (sumTime / 100 ));
        */

    }

    @Test
    public void testNeighbors() {
        Board board = new Board(blocksCorner2());
        Iterator<Board> moves = board.neighbors().iterator();
        while(moves.hasNext()) {
            Board move = moves.next();
            System.out.println(move.toString());
        }
    }

    @Test
    public void testSpecific() {
        int N = 4;
        int [][]blocks = new int[N][N];
        blocks[0][0] = 12;
        blocks[0][1] = 15;
        blocks[0][2] = 14;
        blocks[0][3] = 13;
        blocks[1][0] = 8;
        blocks[1][1] = 11;
        blocks[1][2] = 10;
        blocks[1][3] = 9;
        blocks[2][0] = 4;
        blocks[2][1] = 7;
        blocks[2][2] = 6;
        blocks[2][3] = 5;
        blocks[3][0] = 0;
        blocks[3][1] = 3;
        blocks[3][2] = 2;
        blocks[3][3] = 1;

        Board board = new Board(blocks);
        Assert.assertEquals(15, board.hamming());
        Assert.assertEquals(57, board.manhattan());

        int [][]blocks2 = new int[N][N];
        blocks2[0][0] = 12;
        blocks2[0][1] = 15;
        blocks2[0][2] = 14;
        blocks2[0][3] = 13;
        blocks2[1][0] = 8;
        blocks2[1][1] = 11;
        blocks2[1][2] = 10;
        blocks2[1][3] = 9;
        blocks2[2][0] = 0;
        blocks2[2][1] = 7;
        blocks2[2][2] = 6;
        blocks2[2][3] = 5;
        blocks2[3][0] = 4;
        blocks2[3][1] = 3;
        blocks2[3][2] = 2;
        blocks2[3][3] = 1;

        Board board2 = new Board(blocks2);
        Assert.assertEquals(15, board2.hamming());
        Assert.assertEquals(58, board2.manhattan());

        int [][]blocks3 = new int[N][N];
        blocks3[0][0] = 12;
        blocks3[0][1] = 15;
        blocks3[0][2] = 14;
        blocks3[0][3] = 13;
        blocks3[1][0] = 8;
        blocks3[1][1] = 11;
        blocks3[1][2] = 10;
        blocks3[1][3] = 9;
        blocks3[2][0] = 4;
        blocks3[2][1] = 7;
        blocks3[2][2] = 6;
        blocks3[2][3] = 5;
        blocks3[3][0] = 3;
        blocks3[3][1] = 0;
        blocks3[3][2] = 2;
        blocks3[3][3] = 1;

        Board board3 = new Board(blocks3);
        Assert.assertEquals(15, board3.hamming());
        Assert.assertEquals(58, board3.manhattan());
    }

    @Test
    public void testIsGoal100by100() {
        int N = 100;
        int[][] blocks = new int[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                blocks[i][j] = i + j;
            }
        }
        Board initial = new Board(blocks);
        Assert.assertFalse(initial.isGoal());
    }

    @Test
    public void testIsTwinImmutable() {
        int N = 100;
        int[][] blocks = new int[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                blocks[i][j] = i + j;
            }
        }
        Board initial = new Board(blocks);
        Board twin1 = initial.twin();
        Board twin2 = initial.twin();
        Assert.assertTrue(twin1.equals(twin2));
    }


    @Test
    public void testIsTwin2x2() {
        int N = 2;
        int[][] blocks = new int[N][N];
        blocks[0][0] = 0;
        blocks[0][1] = 2;
        blocks[1][0] = 1;
        blocks[1][1] = 3;

        Board initial = new Board(blocks);
        Board twin1 = initial.twin();
        Assert.assertFalse(twin1.equals(initial));
    }




}
