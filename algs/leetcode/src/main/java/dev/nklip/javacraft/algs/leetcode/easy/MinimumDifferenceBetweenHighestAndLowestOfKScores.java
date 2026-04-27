package dev.nklip.javacraft.algs.leetcode.easy;

import java.util.Arrays;

/*
 * 1984. Minimum Difference Between Highest and Lowest of K Scores
 *
 * LeetCode: https://leetcode.com/problems/minimum-difference-between-highest-and-lowest-of-k-scores/
 *
 * You are given a 0-indexed integer array nums, where nums[i] represents the score of the ith student.
 * You are also given an integer k.
 *
 * Pick the scores of any k students from the array so that the difference between
 * the highest and the lowest of the k scores is minimized.
 *
 * Return the minimum possible difference.
 */
public class MinimumDifferenceBetweenHighestAndLowestOfKScores {

    // Time - O(n log (n)); Space - O(1)
    public int minimumDifference(int[] nums, int k) {
        Arrays.sort(nums);

        int minDifference = Integer.MAX_VALUE;
        for (int i = k - 1; i < nums.length; i++) {
            minDifference = Math.min(minDifference, nums[i] - nums[i - k + 1]);
        }
        return minDifference;
    }

}
