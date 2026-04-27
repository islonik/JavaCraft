package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MinimumDifferenceBetweenHighestAndLowestOfKScoresTest {
    @Test
    void testCase1() {
        MinimumDifferenceBetweenHighestAndLowestOfKScores solution = new MinimumDifferenceBetweenHighestAndLowestOfKScores();
        // Explanation: There is one way to pick score(s) of one student:
        // - [90]. The difference between the highest and lowest score is 90 - 90 = 0.
        // The minimum possible difference is 0.
        Assertions.assertEquals(0, solution.minimumDifference(new int[]{90}, 1));
    }

    @Test
    void testCase2() {
        MinimumDifferenceBetweenHighestAndLowestOfKScores solution = new MinimumDifferenceBetweenHighestAndLowestOfKScores();
        // Explanation: There are six ways to pick score(s) of two students:
        // - [9,4,1,7]. The difference between the highest and lowest score is 9 - 4 = 5.
        // - [9,4,1,7]. The difference between the highest and lowest score is 9 - 1 = 8.
        // - [9,4,1,7]. The difference between the highest and lowest score is 9 - 7 = 2.
        // - [9,4,1,7]. The difference between the highest and lowest score is 4 - 1 = 3.
        // - [9,4,1,7]. The difference between the highest and lowest score is 7 - 4 = 3.
        // - [9,4,1,7]. The difference between the highest and lowest score is 7 - 1 = 6.
        // The minimum possible difference is 2.
        Assertions.assertEquals(2, solution.minimumDifference(new int[]{9,4,1,7}, 2));
    }
}
