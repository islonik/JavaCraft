package dev.nklip.javacraft.algs.leetcode.easy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/*
 * 1346. Check If N and Its Double Exist
 *
 * LeetCode: https://leetcode.com/problems/check-if-n-and-its-double-exist/
 *
 * Given an array arr of integers, check if there exist two indices i and j such that :
 *
 * 1) i != j
 * 2) 0 <= i, j < arr.length
 * 3) arr[i] == 2 * arr[j]
 */
public class CheckIfNAndItsDoubleExist {

    // Time: O(n); Space: O(n)
    // Map
    public boolean checkIfExist(int[] arr) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < arr.length; i++) {
            int num = arr[i];
            map.put(num, i);
        }
        for (int i = 0; i < arr.length; i++) {
            int num = arr[i];

            int doubleValue = 2 * num;

            if (map.containsKey(doubleValue)) {
                int doubleIndex = map.get(doubleValue);
                if (doubleIndex != i) {
                    return true;
                }
            }
        }
        return false;
    }

    // Time: O(n (log n)); Space: O(1)
    // Two pointers
    public boolean checkIfExistTwoPointers(int[] arr) {
        Arrays.sort(arr);

        int left = arr.length - 2;
        int right = arr.length - 1;

        // for positive numbers
        while (left >= 0 && arr[left] >= 0) {
            int leftVal = arr[left];
            int rightVal = arr[right];

            int doubleVal = 2 * leftVal;

            if (doubleVal == rightVal) {
                return true;
            } else if (doubleVal > rightVal) {
                left--;
            } else { // doubleVal < rightVal
                right--;
                if (right == left) {
                    left--;
                }
            }
        }

        left = 0;
        right = 1;
        // for negative numbers
        while (right < arr.length && arr[right] <= 0) {
            int leftVal = arr[left];
            int rightVal = arr[right];

            int doubleVal = 2 * rightVal;

            if (doubleVal == leftVal) {
                return true;
            } else if (doubleVal < leftVal) {
                right++;
            } else { // doubleVal > leftVal
                left++;
                if (right == left) {
                    right++;
                }
            }
        }
        return false;
    }

}
