package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SquaresOfSorterArrayTest {
    @Test
    void testCase1() {
        SquaresOfSorterArray solution = new SquaresOfSorterArray();

        // Explanation: After squaring, the array becomes [16,1,0,9,100].
        // After sorting, it becomes [0,1,9,16,100].
        Assertions.assertArrayEquals(
                new int[] {0,1,9,16,100},
                solution.sortedSquares(new int[] {-4,-1,0,3,10})
        );
    }

    @Test
    void testCase2() {
        SquaresOfSorterArray solution = new SquaresOfSorterArray();

        // Explanation: After squaring, the array becomes [16,1,0,9,100].
        // After sorting, it becomes [0,1,9,16,100].
        Assertions.assertArrayEquals(
                new int[] {4,9,9,49,121},
                solution.sortedSquares(new int[] {-7,-3,2,3,11})
        );
    }
}
