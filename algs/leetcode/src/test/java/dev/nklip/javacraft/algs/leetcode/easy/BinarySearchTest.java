package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BinarySearchTest {
    @Test
    void testCase1() {
        BinarySearch solution = new BinarySearch();

        // Explanation: 9 exists in nums and its index is 4
        Assertions.assertEquals(4, solution.search(new int[]{-1,0,3,5,9,12}, 9));
        Assertions.assertEquals(4, solution.searchRecursive(new int[]{-1,0,3,5,9,12}, 9));
    }

    @Test
    void testCase2() {
        BinarySearch solution = new BinarySearch();

        // Explanation: 2 does not exist in nums so return -1
        Assertions.assertEquals(-1, solution.search(new int[]{-1,0,3,5,9,12}, 2));
        Assertions.assertEquals(-1, solution.searchRecursive(new int[]{-1,0,3,5,9,12}, 2));
    }

    @Test
    void testSingleElementFound() {
        BinarySearch solution = new BinarySearch();
        Assertions.assertEquals(0, solution.search(new int[]{5}, 5));
    }

    @Test
    void testSingleElementNotFound() {
        BinarySearch solution = new BinarySearch();
        Assertions.assertEquals(-1, solution.search(new int[]{5}, 3));
    }

    @Test
    void testTargetIsFirstElement() {
        BinarySearch solution = new BinarySearch();
        Assertions.assertEquals(0, solution.search(new int[]{-1,0,3,5,9,12}, -1));
    }

    @Test
    void testTargetIsLastElement() {
        BinarySearch solution = new BinarySearch();
        Assertions.assertEquals(5, solution.search(new int[]{-1,0,3,5,9,12}, 12));
    }

    @Test
    void testTargetSmallerThanAll() {
        BinarySearch solution = new BinarySearch();
        Assertions.assertEquals(-1, solution.search(new int[]{-1,0,3,5,9,12}, -10));
    }

    @Test
    void testTargetLargerThanAll() {
        BinarySearch solution = new BinarySearch();
        Assertions.assertEquals(-1, solution.search(new int[]{-1,0,3,5,9,12}, 100));
    }

    @Test
    void testTwoElementsFoundFirst() {
        BinarySearch solution = new BinarySearch();
        Assertions.assertEquals(0, solution.search(new int[]{1, 2}, 1));
    }

    @Test
    void testTwoElementsFoundSecond() {
        BinarySearch solution = new BinarySearch();
        Assertions.assertEquals(1, solution.search(new int[]{1, 2}, 2));
    }
}
