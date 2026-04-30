package dev.nklip.javacraft.algs.leetcode.easy;

/*
 * 2269. Find the K-Beauty of a Number
 *
 * LeetCode: https://leetcode.com/problems/find-the-k-beauty-of-a-number/
 *
 * The k-beauty of an integer num is defined as the number of substrings of num when it is read as a string
 * that meet the following conditions:
 *
 * 1) It has a length of k.
 * 2) It is a divisor of num.
 *
 * Given integers num and k, return the k-beauty of num.
 *
 * Note:
 *
 * 1) Leading zeros are allowed.
 * 2) 0 is not a divisor of any value.
 *
 * A substring is a contiguous sequence of characters in a string.
 */
public class FindTheKBeautyOfNumber {

    // Time - O(n); Space - O(1)
    public int divisorSubstrings(int num, int k) {
        String numStr = String.valueOf(num);

        int count = 0;
        for (int i = k; i <= numStr.length(); i++) {
            String substring = numStr.substring(i-k, i);
            int subInt = Integer.parseInt(substring);

            if (subInt != 0 && num % subInt == 0) {
                count++;
            }
        }
        return count;
    }

}
