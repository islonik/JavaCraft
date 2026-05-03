package dev.nklip.javacraft.algs.leetcode.easy;

/*
 * 704. Binary Search
 *
 * LeetCode: https://leetcode.com/problems/binary-search/
 *
 * Given an array of integers nums which is sorted in ascending order,
 * and an integer target, write a function to search target in nums.
 * If target exists, then return its index. Otherwise, return -1.
 *
 * You must write an algorithm with O(log n) runtime complexity.
 */
public class BinarySearch {

    // Time - O(log n); Space - O(1)
    public int search(int[] nums, int target) {
        int leftIndex = 0;
        int rightIndex = nums.length - 1;

        while (leftIndex <= rightIndex) {
            int midIndex = rightIndex - (rightIndex - leftIndex) / 2;

            int value = nums[midIndex];
            if (value == target) {
                return midIndex;
            } else if (value > target) {
                leftIndex = 0;
                rightIndex = midIndex - 1;
            } else {
                leftIndex = midIndex + 1;
            }
        }
        return -1;
    }

    // Time - O(log n); Space - O(log n) because of the call stack
    public int searchRecursive(int[] nums, int target) {
        int midIndex = nums.length / 2;

        if (nums[midIndex] == target) {
            return midIndex;
        } else if (nums[midIndex] > target) {
            return search(nums, 0, midIndex - 1, target);
        } else {
            return search(nums, midIndex + 1, nums.length - 1, target);
        }
    }

    private int search(int[] nums, int minIndex, int maxIndex, int target) {
        if (maxIndex - minIndex < 0) {
            return -1;
        }
        int midIndex = maxIndex - (maxIndex - minIndex) / 2;

        if (nums[midIndex] == target) {
            return midIndex;
        } else if (nums[midIndex] > target) {
            return search(nums, minIndex, midIndex - 1, target);
        } else  {
            return search(nums, midIndex + 1, maxIndex, target);
        }
    }

}
