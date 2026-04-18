package dev.nklip.javacraft.algs.leetcode.easy;

/*
 * 680. Valid Palindrome II
 *
 * LeetCode: https://leetcode.com/problems/valid-palindrome-ii/
 *
 * Given a string s, return true if the s can be palindrome after deleting at most one character from it.
 */
public class ValidPalindromeII {
    public boolean validPalindrome(String s) {
        int left = 0;
        int right = s.length() - 1;

        while (left <= right) {
            char leftCh = s.charAt(left);
            char rightCh = s.charAt(right);

            if (leftCh != rightCh) {
                String leftStr = excludeIndexOf(s, left, right, left);
                String rightStr = excludeIndexOf(s, left, right, right);

                boolean isPalindrome = isPalindrome(leftStr);
                if (isPalindrome) {
                    return true;
                }
                return isPalindrome(rightStr);
            }
            left++;
            right--;
        }
        return true;
    }

    private boolean isPalindrome(String s) {
        int left = 0;
        int right = s.length() - 1;

        while (left <= right) {
            if (s.charAt(left++) != s.charAt(right--)) {
                return false;
            }
        }
        return true;
    }

    String excludeIndexOf(String s, int left, int right, int exclude) {
        return s.substring(left, exclude) + s.substring(exclude + 1, right + 1);
    }
}
