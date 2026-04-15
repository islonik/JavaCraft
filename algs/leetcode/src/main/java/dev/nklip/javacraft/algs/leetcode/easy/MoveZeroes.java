package dev.nklip.javacraft.algs.leetcode.easy;

/*
 * 283. Move Zeroes
 *
 * LeetCode: https://leetcode.com/problems/move-zeroes/
 *
 * Given an integer array nums, move all 0's to the end of it while maintaining
 * the relative order of the non-zero elements.
 *
 * Note that you must do this in-place without making a copy of the array.
 */
public class MoveZeroes {

    public void moveZeroes(int[] nums) {
        int zeroIndex = 0;
        for (int i = 0; i < nums.length; i++) {
            int value = nums[i];
            if (value != 0) {
                // we swap zeroIndex any value on non zero value
                int anyValue = nums[zeroIndex];
                nums[zeroIndex] = value;
                nums[i] = anyValue;
                zeroIndex++;
            }
        }
    }

}
