package dev.nklip.javacraft.algs.leetcode.easy;

import java.util.*;

/*
 * 594. Longest Harmonious Subsequence
 *
 * LeetCode: https://leetcode.com/problems/longest-harmonious-subsequence/
 *
 * We define a harmonious array as an array where the difference between its maximum value and its minimum value is exactly 1.
 *
 * Given an integer array nums, return the length of its longest harmonious subsequence among all its possible subsequences.
 */
public class LongestHarmoniousSubsequence {

    // Time - O(n); Space - O(n)
    // use map - frequency counting
    public int findLHS(int[] nums) {
        // O(n)
        Map<Integer, Integer> map = new LinkedHashMap<>();
        for (int key : nums) {
            Integer count = map.getOrDefault(key, 0);
            map.put(key, ++count);
        }

        int maxLength = 0;
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            Integer startKey = entry.getKey();
            Integer endKey = startKey + 1;

            if (map.containsKey(endKey)) {
                int length = entry.getValue() + map.get(endKey);
                if (length > maxLength) {
                    maxLength = length;
                }
            }
        }
        return maxLength;
    }


}
