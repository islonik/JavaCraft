package dev.nklip.javacraft.algs.leetcode.easy;

/*
 * 977. Squares of a Sorted Array
 *
 * LeetCode: https://leetcode.com/problems/squares-of-a-sorted-array/
 *
 * Given an integer array nums sorted in non-decreasing order,
 * return an array of the squares of each number sorted in non-decreasing order.
 */
public class SquaresOfSorterArray {
    // Time - O(n); Space - O(n)
    public int[] sortedSquares(int[] nums) {
        int[] result = new int[nums.length];
        int resultIndex = nums.length - 1;

        int left = 0;
        int right = nums.length - 1;

        while (left <= right) {
            int leftValue = nums[left];
            int rightValue = nums[right];

            int leftSquare = leftValue * leftValue;
            int rightSquare = rightValue * rightValue;

            if (leftSquare >= rightSquare) {
                result[resultIndex--] = leftSquare;
                left++;
            } else {
                result[resultIndex--] = rightSquare;
                right--;
            }
        }
        return result;
    }
}
