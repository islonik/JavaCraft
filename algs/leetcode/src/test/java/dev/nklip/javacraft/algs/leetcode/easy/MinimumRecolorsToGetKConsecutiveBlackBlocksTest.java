package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MinimumRecolorsToGetKConsecutiveBlackBlocksTest {

    @Test
    void testCase1() {
        MinimumRecolorsToGetKConsecutiveBlackBlocks solution = new MinimumRecolorsToGetKConsecutiveBlackBlocks();

        // Explanation:
        // One way to achieve 7 consecutive black blocks is to recolor the 0th, 3rd, and 4th blocks
        // so that blocks = "BBBBBBBWBW".
        // It can be shown that there is no way to achieve 7 consecutive black blocks in less than 3 operations.
        // Therefore, we return 3.
        Assertions.assertEquals(3, solution.minimumRecolors("WBBWWBBWBW", 7));
    }

    @Test
    void testCase2() {
        MinimumRecolorsToGetKConsecutiveBlackBlocks solution = new MinimumRecolorsToGetKConsecutiveBlackBlocks();

        // Explanation:
        // No changes need to be made, since 2 consecutive black blocks already exist.
        // Therefore, we return 0.
        Assertions.assertEquals(0, solution.minimumRecolors("WBWBBBW", 2));
    }
}
