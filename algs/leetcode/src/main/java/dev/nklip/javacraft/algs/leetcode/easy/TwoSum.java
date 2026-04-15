package dev.nklip.javacraft.algs.leetcode.easy;

import java.util.*;

/*
 * 1. Two Sum
 *
 * LeetCode: https://leetcode.com/problems/two-sum
 *
 * Given an array of integers nums and an integer target,
 * return indices of the two numbers such that they add up to target.
 *
 * You may assume that each input would have exactly one solution, and you may not use the same element twice.
 *
 * You can return the answer in any order.
 */
public class TwoSum {

    // canonical solution - O(n) time, O(n) space
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> seen = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            if (seen.containsKey(complement)) {
                return new int[] { seen.get(complement), i };
            }
            seen.put(nums[i], i);
        }
        return new int[0];
    }

    // simple brute force - O(n2) time complexity
    public int[] twoSumUseBruteForce(int[] nums, int target) {
        int []output = new int[2];

        for (int i = 0; i < nums.length; i++) {
            int first = nums[i];
            for (int y = i + 1; y < nums.length; y++) {
                int second = nums[y];

                int sum = first + second;
                if (sum == target) {
                    output[0] = i;
                    output[1] = y;
                    break;
                }
            }

        }

        return output;
    }

}
