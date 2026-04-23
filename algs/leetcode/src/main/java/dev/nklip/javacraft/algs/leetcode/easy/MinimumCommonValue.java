package dev.nklip.javacraft.algs.leetcode.easy;

/*
 * 2540. Minimum Common Value
 *
 * LeetCode: https://leetcode.com/problems/minimum-common-value/
 *
 * Given two integer arrays nums1 and nums2, sorted in non-decreasing order, return the minimum integer common to both arrays.
 * If there is no common integer amongst nums1 and nums2, return -1.
 *
 * Note that an integer is said to be common to nums1 and nums2 if both arrays have at least one occurrence of that integer.
 */
public class MinimumCommonValue {

    // Time - O(n); Space O(1)
    public int getCommon(int[] nums1, int[] nums2) {
        if (nums1[nums1.length - 1] < nums2[0]) {
            return -1;
        }
        if (nums2[nums2.length - 1] < nums1[0]) {
            return -1;
        }
        int n1 = 0;
        int n2 = 0;
        int length = nums1.length + nums2.length;
        for (int i = 0; i < length; i++) {
            int num1Val = nums1[n1];
            int num2Val = nums2[n2];
            if (num1Val == num2Val) {
                return num1Val;
            } else if (num1Val < num2Val) {
                if (n1 + 1 != nums1.length) {
                    n1++;
                }
            } else { // num1Val > num2Val
                if (n2 + 1 != nums2.length) {
                    n2++;
                }
            }
        }
        return -1;
    }
}
