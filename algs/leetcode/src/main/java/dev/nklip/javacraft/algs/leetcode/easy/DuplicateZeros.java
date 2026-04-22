package dev.nklip.javacraft.algs.leetcode.easy;

/*
 * 1089. Duplicate Zeros
 *
 * LeetCode: https://leetcode.com/problems/duplicate-zeros/
 *
 * Given a fixed-length integer array arr, duplicate each occurrence of zero,
 * shifting the remaining elements to the right.
 *
 * Note that elements beyond the length of the original array are not written.
 * Do the above modifications to the input array in place and do not return anything.
 */
public class DuplicateZeros {

    public void duplicateZeros(int[] arr) {
        int zeros = 0;
        for (int value : arr) {
            if (value == 0) {
                zeros++;
            }
        }

        int readIndex = arr.length - 1;
        int writeIndex = arr.length + zeros - 1;

        while (readIndex >= 0) {
            int value = arr[readIndex];

            // copy any value
            if (writeIndex < arr.length) {
                arr[writeIndex] = value;
            }
            // if value is 0, add extra 0
            if (value == 0) {
                writeIndex--;
                if (writeIndex < arr.length) {
                    arr[writeIndex] = 0;
                }
            }

            readIndex--;
            writeIndex--;
        }
    }
}
