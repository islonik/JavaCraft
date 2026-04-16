package dev.nklip.javacraft.algs.leetcode.easy;

/*
 * 344. Reverse String
 *
 * LeetCode: https://leetcode.com/problems/reverse-string/
 *
 * Write a function that reverses a string. The input string is given as an array of characters s.
 *
 * You must do this by modifying the input array in-place with O(1) extra memory.
 */
public class ReverseString {

    // O(n) time, O(1) space.
    public void reverseString(char[] s) {
        int left = 0;
        int right = s.length - 1;

        while(left < right) {
            char temp = s[left];
            s[left] = s[right];
            s[right] = temp;

            left++;
            right--;
        }
    }

}
