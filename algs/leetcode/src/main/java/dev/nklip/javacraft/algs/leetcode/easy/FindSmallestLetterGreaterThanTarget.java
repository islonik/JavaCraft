package dev.nklip.javacraft.algs.leetcode.easy;

/*
 * 744. Find Smallest Letter Greater Than Target
 *
 * LeetCode: https://leetcode.com/problems/find-smallest-letter-greater-than-target/
 *
 * You are given an array of characters letters that is sorted in non-decreasing order, and a character target.
 * There are at least two different characters in letters.
 *
 * Return the smallest character in letters that is lexicographically greater than target.
 * If such a character does not exist, return the first character in letters.
 */
public class FindSmallestLetterGreaterThanTarget {

    public char nextGreatestLetter(char[] letters, char target) {
        int leftIndex = 0;
        int rightIndex = letters.length - 1;

        char answer = letters[leftIndex];

        while (leftIndex <= rightIndex) {
            int midIndex = rightIndex - (rightIndex - leftIndex) / 2;

            if (letters[midIndex] > target) {
                answer = letters[midIndex];
                rightIndex = midIndex - 1;
            } else {
                leftIndex = midIndex + 1;
            }
        }
        return answer;
    }

}
