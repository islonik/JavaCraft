package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AssignCookiesTest {

    @Test
    void testCase1() {
        AssignCookies solution = new AssignCookies();

        // Explanation: You have 3 children and 2 cookies. The greed factors of 3 children are 1, 2, 3.
        // And even though you have 2 cookies, since their size is both 1, you could only make the child whose greed factor is 1 content.
        // You need to output 1.
        Assertions.assertEquals(1, solution.findContentChildren(new int[]{1,2,3}, new int[]{1,1}));
    }

    @Test
    void testCase2() {
        AssignCookies solution = new AssignCookies();

        // Explanation: You have 2 children and 3 cookies. The greed factors of 2 children are 1, 2.
        // You have 3 cookies and their sizes are big enough to gratify all of the children,
        // You need to output 2.
        Assertions.assertEquals(2, solution.findContentChildren(new int[]{1,2}, new int[]{1,2,3}));
    }

    @Test
    void testCase3() {
        AssignCookies solution = new AssignCookies();

        Assertions.assertEquals(0, solution.findContentChildren(new int[]{1,2,3}, new int[]{}));
    }

    @Test
    void testCase4() {
        AssignCookies solution = new AssignCookies();

        Assertions.assertEquals(1, solution.findContentChildren(new int[]{1,2,3}, new int[]{3}));
    }

    @Test
    void testCase5() {
        AssignCookies solution = new AssignCookies();

        Assertions.assertEquals(2, solution.findContentChildren(new int[]{10,9,8,7}, new int[]{5,6,7,8}));
    }

}
