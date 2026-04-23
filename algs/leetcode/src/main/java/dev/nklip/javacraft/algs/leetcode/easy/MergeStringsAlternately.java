package dev.nklip.javacraft.algs.leetcode.easy;

/*
 * 1768. Merge Strings Alternately
 *
 * LeetCode: https://leetcode.com/problems/merge-strings-alternately/
 *
 * You are given two strings word1 and word2.
 * Merge the strings by adding letters in alternating order, starting with word1.
 * If a string is longer than the other, append the additional letters onto the end of the merged string.
 *
 * Return the merged string.
 */
public class MergeStringsAlternately {

    // Time - O(n); Space - O(n)
    public String mergeAlternately(String word1, String word2) {
        StringBuilder sb = new StringBuilder();

        int length = word1.length() + word2.length();
        int w1 = 0;
        int w2 = 0;

        for (int i = 0; i < length; i++) {

            if (w1 == w2 && w1 < word1.length() || w2 == word2.length()) {
                sb.append(word1.charAt(w1++));
            } else {
                sb.append(word2.charAt(w2++));
            }
        }
        return sb.toString();
    }

}
