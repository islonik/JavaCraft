package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DuplicateZerosTest {
    @Test
    void testCase1() {
        DuplicateZeros solution = new DuplicateZeros();

        int[] array = new int[]{ 1,0,2,3,0,4,5,0 };
        solution.duplicateZeros(array);

        // Explanation: After calling your function, the input array is modified to: [1,0,0,2,3,0,0,4]
        Assertions.assertArrayEquals(new int[]{1,0,0,2,3,0,0,4}, array);
    }

    @Test
    void testCase2() {
        DuplicateZeros solution = new DuplicateZeros();

        int[] array = new int[]{ 1,2,3 };
        solution.duplicateZeros(array);
        // Explanation: After calling your function, the input array is modified to: [1,2,3]
        Assertions.assertArrayEquals(new int[]{1,2,3}, array);
    }

    @Test
    void testCase3() {
        DuplicateZeros solution = new DuplicateZeros();

        int[] array = new int[]{ 8,4,5,0,0,0,0,7 };
        solution.duplicateZeros(array);
        Assertions.assertArrayEquals(new int[]{ 8,4,5,0,0,0,0,0}, array);
    }

    @Test
    void testCase4() {
        DuplicateZeros solution = new DuplicateZeros();

        int[] array = new int[]{ 1,5,2,0,6,8,0,6,0 };
        solution.duplicateZeros(array);
        Assertions.assertArrayEquals(new int[]{ 1,5,2,0,0,6,8,0,0}, array);
    }

    @Test
    void testCase5() {
        DuplicateZeros solution = new DuplicateZeros();

        int[] array = new int[]{ 0,1,0,2 };
        solution.duplicateZeros(array);
        Assertions.assertArrayEquals(new int[]{ 0,0,1,0}, array);
    }

    @Test
    void testCase6() {
        DuplicateZeros solution = new DuplicateZeros();

        int[] array = new int[]{ 1,0,0 };
        solution.duplicateZeros(array);
        Assertions.assertArrayEquals(new int[]{ 1,0,0 }, array);
    }
}
