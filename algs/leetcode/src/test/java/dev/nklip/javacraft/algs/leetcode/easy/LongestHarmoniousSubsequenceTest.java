package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LongestHarmoniousSubsequenceTest {

    @Test
    void testCase1() {
        LongestHarmoniousSubsequence solution = new LongestHarmoniousSubsequence();

        // Explanation:
        // The longest harmonious subsequence is [3,2,2,2,3].
        Assertions.assertEquals(5, solution.findLHS(new int[]{1,3,2,2,5,2,3,7}));
    }

    @Test
    void testCase2() {
        LongestHarmoniousSubsequence solution = new LongestHarmoniousSubsequence();

        // Explanation:
        // The longest harmonious subsequences are [1,2], [2,3], and [3,4], all of which have a length of 2.
        Assertions.assertEquals(2, solution.findLHS(new int[]{1,2,3,4}));
    }

    @Test
    void testCase3() {
        LongestHarmoniousSubsequence solution = new LongestHarmoniousSubsequence();

        // Explanation:
        // No harmonic subsequence exists.
        Assertions.assertEquals(0, solution.findLHS(new int[]{1,1,1,1}));
    }
}
