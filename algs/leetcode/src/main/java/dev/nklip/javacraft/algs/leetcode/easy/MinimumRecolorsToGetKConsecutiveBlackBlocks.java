package dev.nklip.javacraft.algs.leetcode.easy;

/*
 * 2379. Minimum Recolors to Get K Consecutive Black Blocks
 *
 * LeetCode: https://leetcode.com/problems/minimum-recolors-to-get-k-consecutive-black-blocks/
 *
 * You are given a 0-indexed string blocks of length n, where blocks[i] is either 'W' or 'B',
 * representing the color of the ith block. The characters 'W' and 'B' denote the colors white and black, respectively.
 *
 * You are also given an integer k, which is the desired number of consecutive black blocks.
 *
 * In one operation, you can recolor a white block such that it becomes a black block.
 *
 * Return the minimum number of operations needed such that there is at least one occurrence of k consecutive black blocks.
 */
public class MinimumRecolorsToGetKConsecutiveBlackBlocks {

    // Time - O(n); Space - O(1)
    public int minimumRecolors(String blocks, int k) {

        int blacks = 0;
        int maxBlacks = 0;
        for (int i = 0; i < blocks.length(); i++) {
            char addingChar = blocks.charAt(i);

            if (i >= k) {
                char removingChar = blocks.charAt(i - k);
                if (removingChar == 'B') {
                    blacks--;
                }
            }

            if (addingChar == 'B') {
                blacks++;
                if (maxBlacks < blacks) {
                    maxBlacks = blacks;
                }
            }
        }
        return Math.max(k - maxBlacks, 0);
    }

}
