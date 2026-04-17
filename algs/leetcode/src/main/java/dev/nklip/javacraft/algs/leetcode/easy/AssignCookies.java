package dev.nklip.javacraft.algs.leetcode.easy;

import java.util.Arrays;

/*
 * 455. Assign Cookies
 *
 * LeetCode: https://leetcode.com/problems/assign-cookies/
 *
 * Assume you are an awesome parent and want to give your children some cookies.
 * But, you should give each child at most one cookie.
 *
 * Each child i has a greed factor g[i], which is the minimum size of a cookie that the child will be content with;
 * and each cookie j has a size s[j]. If s[j] >= g[i], we can assign the cookie j to the child i,
 * and the child i will be content.
 * Your goal is to maximize the number of your content children and output the maximum number.
 */
public class AssignCookies {

    // O(n log n) time (sort-dominated), O(1) extra space
    public int findContentChildren(int[] childGreed, int[] cookieSize) {
        if (cookieSize == null || childGreed == null || cookieSize.length < 1) {
            return 0;
        }
        Arrays.sort(childGreed);
        Arrays.sort(cookieSize);

        int child = 0;
        int cookie = 0;

        while (child < childGreed.length && cookie < cookieSize.length) {
            if (childGreed[child] <= cookieSize[cookie]) {
                child++; // child satisfied, move to next
            }
            cookie++;    // always try next cookie
        }
        return child;
    }

}
