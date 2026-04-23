package dev.nklip.javacraft.algs.leetcode.easy;

import java.util.*;

/*
 * 2824. Count Pairs Whose Sum is Less than Target
 *
 * LeetCode: https://leetcode.com/problems/count-pairs-whose-sum-is-less-than-target/
 *
 * Given a 0-indexed integer array nums of length n and an integer target,
 * return the number of pairs (i, j) where 0 <= i < j < n and nums[i] + nums[j] < target.
 */
public class CountPairsWhoseSumIsLessThanTarget {

    // Time - O(n^2); Space - O(1)
    public int countPairs(List<Integer> nums, int target) {
        int count = 0;
        for (int i = 0; i < nums.size(); i++) {
            int iValue = nums.get(i);
            for (int j = i + 1; j < nums.size(); j++) {
                int jValue = nums.get(j);

                int sum = iValue + jValue;
                if (sum < target) {
                    count++;
                }
            }
        }
        return count;
    }

}
