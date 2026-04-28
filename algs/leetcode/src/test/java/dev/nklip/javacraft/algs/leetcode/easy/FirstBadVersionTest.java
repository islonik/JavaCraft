package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FirstBadVersionTest {

    @Test
    void testCase1() {
        FirstBadVersion solution = new FirstBadVersion(4);

        // Explanation:
        // call isBadVersion(3) -> false
        // call isBadVersion(5) -> true
        // call isBadVersion(4) -> true
        // Then 4 is the first bad version.
        Assertions.assertEquals(4, solution.firstBadVersion(5));
    }

    @Test
    void testCase2() {
        FirstBadVersion solution = new FirstBadVersion(1);

        Assertions.assertEquals(1, solution.firstBadVersion(1));
    }

    @Test
    void testCase3() {
        FirstBadVersion solution = new FirstBadVersion(2);

        Assertions.assertEquals(2, solution.firstBadVersion(2));
    }

    @Test
    void testCase4() {
        FirstBadVersion solution = new FirstBadVersion(1);

        Assertions.assertEquals(1, solution.firstBadVersion(3));
    }

    @Test
    void testCase5() {
        FirstBadVersion solution = new FirstBadVersion(1);

        Assertions.assertEquals(1, solution.firstBadVersion(4));
    }
}
