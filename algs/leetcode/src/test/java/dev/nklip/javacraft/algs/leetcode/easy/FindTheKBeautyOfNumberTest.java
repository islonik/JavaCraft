package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FindTheKBeautyOfNumberTest {

    @Test
    void testCase1() {
        FindTheKBeautyOfNumber solution = new FindTheKBeautyOfNumber();

        // Explanation: The following are the substrings of num of length k:
        // - "24" from "240": 24 is a divisor of 240.
        // - "40" from "240": 40 is a divisor of 240.
        // Therefore, the k-beauty is 2.
        Assertions.assertEquals(2, solution.divisorSubstrings(240, 2));
    }

    @Test
    void testCase2() {
        FindTheKBeautyOfNumber solution = new FindTheKBeautyOfNumber();

        // Input: num = 430043, k = 2
        // Output: 2
        // Explanation: The following are the substrings of num of length k:
        // - "43" from "430043": 43 is a divisor of 430043.
        // - "30" from "430043": 30 is not a divisor of 430043.
        // - "00" from "430043": 0 is not a divisor of 430043.
        // - "04" from "430043": 4 is not a divisor of 430043.
        // - "43" from "430043": 43 is a divisor of 430043.
        // Therefore, the k-beauty is 2.
        Assertions.assertEquals(2, solution.divisorSubstrings(430043, 2));
    }
}
