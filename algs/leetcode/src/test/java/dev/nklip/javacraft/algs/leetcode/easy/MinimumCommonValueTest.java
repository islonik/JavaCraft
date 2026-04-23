package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MinimumCommonValueTest {

    @Test
    void testCase1() {
        MinimumCommonValue solution = new MinimumCommonValue();

        // Explanation: The smallest element common to both arrays is 2, so we return 2.
        Assertions.assertEquals(2, solution.getCommon(new int[]{1,2,3}, new int[]{2,4}));
    }

    @Test
    void testCase2() {
        MinimumCommonValue solution = new MinimumCommonValue();

        // Explanation: There are two common elements in the array 2 and 3 out of which 2 is the smallest, so 2 is returned.
        Assertions.assertEquals(2, solution.getCommon(new int[]{1,2,3,6}, new int[]{2,3,4,5}));
    }

    @Test
    void testCase3() {
        MinimumCommonValue solution = new MinimumCommonValue();

        // Explanation: There are two common elements in the array 2 and 3 out of which 2 is the smallest, so 2 is returned.
        Assertions.assertEquals(-1, solution.getCommon(
                new int[]{34,225,328,530,823,834,902,989},
                new int[]{24,30,115,121,160,173,239,265,335,362,449,557,597,624,697,766,775,881,898,919}
        ));
    }

}
