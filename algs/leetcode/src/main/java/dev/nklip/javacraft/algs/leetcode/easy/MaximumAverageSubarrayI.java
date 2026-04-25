package dev.nklip.javacraft.algs.leetcode.easy;

/*
 * 643. Maximum Average Subarray I
 *
 * LeetCode: https://leetcode.com/problems/maximum-average-subarray-i/description/
 *
 * You are given an integer array nums consisting of n elements, and an integer k.
 *
 * Find a contiguous subarray whose length is equal to k that has the maximum average value and return this value.
 * Any answer with a calculation error less than 10-5 will be accepted.
 */
public class MaximumAverageSubarrayI {

    // Time - O(n); Space (1)
    public double findMaxAverage(int[] nums, int k) {
        int kSum = 0;
        double maxAverage = 0.0;
        boolean isAverageSet = false;

        for (int i = 0; i < nums.length; i++) {
            int value = nums[i];
            if (i >= k) {
                kSum -= nums[i - k];
            }
            kSum += value;

            if (i + 1 >= k) {
                double average = (double) kSum / k;

                if (!isAverageSet || maxAverage < average) {
                    isAverageSet = true;
                    maxAverage = average;
                }
            }
        }

        return maxAverage;
    }

}
