package dev.nklip.javacraft.algs.leetcode.medium;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ThreeSumClosestTest {

    @Test
    void testCase1() {
        ThreeSumClosest solution = new ThreeSumClosest();

        // Explanation: The sum that is closest to the target is 2. (-1 + 2 + 1 = 2).
        Assertions.assertEquals(2, solution.threeSumClosest(new int[]{-1,2,1,-4}, 1));
    }

    @Test
    void testCase2() {
        ThreeSumClosest solution = new ThreeSumClosest();

        // Explanation: The sum that is closest to the target is 0. (0 + 0 + 0 = 0).
        Assertions.assertEquals(0, solution.threeSumClosest(new int[]{-0,0,0}, 1));
    }

    @Test
    void testCase3() {
        ThreeSumClosest solution = new ThreeSumClosest();

        Assertions.assertEquals(3, solution.threeSumClosest(new int[]{1,1,1,0}, 100));
    }

    @Test
    void testCase4() {
        ThreeSumClosest solution = new ThreeSumClosest();

        Assertions.assertEquals(-2, solution.threeSumClosest(new int[]{4,0,5,-5,3,3,0,-4,-5}, -2));
    }
}
