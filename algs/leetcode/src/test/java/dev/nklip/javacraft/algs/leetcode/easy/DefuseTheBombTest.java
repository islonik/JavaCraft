package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DefuseTheBombTest {

    @Test
    void testCase1() {
        DefuseTheBomb solution = new DefuseTheBomb();

        // Explanation: Each number is replaced by the sum of the next 3 numbers.
        // The decrypted code is [7+1+4, 1+4+5, 4+5+7, 5+7+1]. Notice that the numbers wrap around.
        Assertions.assertArrayEquals(new int[]{12,10,16,13}, solution.decrypt(new int[]{5,7,1,4}, 3));
    }

    @Test
    void testCase2() {
        DefuseTheBomb solution = new DefuseTheBomb();

        // Explanation: When k is zero, the numbers are replaced by 0.
        Assertions.assertArrayEquals(new int[]{0,0,0,0}, solution.decrypt(new int[]{1,2,3,4}, 0));
    }

    @Test
    void testCase3() {
        DefuseTheBomb solution = new DefuseTheBomb();

        // Explanation: The decrypted code is [3+9, 2+3, 4+2, 9+4].
        // Notice that the numbers wrap around again. If k is negative, the sum is of the previous numbers.
        Assertions.assertArrayEquals(new int[]{12,5,6,13}, solution.decrypt(new int[]{2,4,9,3}, -2));
    }
}
