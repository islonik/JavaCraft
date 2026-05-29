package dev.nklip.javacraft.algs.leetcode.medium;

import java.util.Arrays;

/*
 * 16. 3Sum Closest
 *
 * LeetCode: https://leetcode.com/problems/3sum-closest/
 *
 * Given an integer array nums of length n and an integer target,
 * find three integers at distinct indices in nums such that the sum is closest to target.
 *
 * Return the sum of the three integers.
 *
 * You may assume that each input would have exactly one solution.
 */
public class ThreeSumClosest {

    // Time - O(n^2); Space - O(1)
    public int threeSumClosest(int[] nums, int target) {
        Arrays.sort(nums);

        int closest = nums[0] + nums[1] + nums[2];

        for (int i = 0; i < nums.length - 2; i++) {
            int left = i + 1;
            int right = nums.length - 1;

            while (left < right) {
                int temp = nums[i] + nums[left] + nums[right];

                closest = Math.abs(target - temp) <= Math.abs(target - closest) ? temp : closest;

                if (temp == target) {
                    return closest;
                } else if (temp > target) {
                    right--;
                } else  {
                    left++;
                }
            }
        }

        return closest;
    }

}
