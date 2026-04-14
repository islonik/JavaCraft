package dev.nklip.javacraft.algs.leetcode.easy;

/*
 * 26. Remove Duplicates from Sorted Array
 *
 * LeetCode: https://leetcode.com/problems/remove-duplicates-from-sorted-array/
 *
 * Given an integer array nums sorted in non-decreasing order, remove the duplicates in-place such
 * that each unique element appears only once. The relative order of the elements should be kept the same.
 *
 * Consider the number of unique elements in nums to be k.
 * After removing duplicates, return the number of unique elements k.
 *
 * The first k elements of nums should contain the unique numbers in sorted order.
 * The remaining elements beyond index k - 1 can be ignored.
 *
 * Custom Judge:
 *
 * The judge will test your solution with the following code:
 *
 * int[] nums = [...]; // Input array
 * int[] expectedNums = [...]; // The expected answer with correct length
 *
 * int k = removeDuplicates(nums); // Calls your implementation
 *
 * assert k == expectedNums.length;
 * for (int i = 0; i < k; i++) {
 *     assert nums[i] == expectedNums[i];
 * }
 *
 * If all assertions pass, then your solution will be accepted.
 */
public class RemoveDuplicatesFromSortedArray {

    public int removeDuplicates(int[] nums) {
        int idUniquePos = 1;
        for (int i = 1; i < nums.length; i++) {
            int value = nums[i];

            int uniqueValue = nums[idUniquePos - 1];
            if (uniqueValue != value) {
                nums[idUniquePos++] = value;
            }
        }
        return idUniquePos;
    }
}
