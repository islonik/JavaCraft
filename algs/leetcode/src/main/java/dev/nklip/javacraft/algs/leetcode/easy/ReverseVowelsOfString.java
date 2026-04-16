package dev.nklip.javacraft.algs.leetcode.easy;

import java.util.HashSet;
import java.util.Set;

/*
 * 345. Reverse Vowels of a String
 *
 * LeetCode: https://leetcode.com/problems/reverse-vowels-of-a-string/
 *
 * Given a string s, reverse only all the vowels in the string and return it.
 *
 * The vowels are 'a', 'e', 'i', 'o', and 'u', and they can appear in both lower and upper cases, more than once.
 */
public class ReverseVowelsOfString {

    // O(n) time, O(1) space (the Set is fixed-size — 10 elements).
    public String reverseVowels(String input) {
        Set<Character> vowels = getVowels();

        int left = 0;
        int right = input.length() - 1;

        char[] array = input.toCharArray();

        while (left < right) {
            Character chLeft = array[left];
            Character chRight = array[right];

            if (vowels.contains(chLeft) && vowels.contains(chRight)) {
                array[left] = chRight;
                array[right] = chLeft;

                left++;
                right--;
            } else if (!vowels.contains(chLeft)) {
                left++;
            } else if (!vowels.contains(chRight)) {
                right--;
            }
        }
        return new String(array);
    }

    private static Set<Character> getVowels() {
        Set<Character> vowels = new HashSet<>();
        vowels.add('a');
        vowels.add('e');
        vowels.add('i');
        vowels.add('o');
        vowels.add('u');
        vowels.add('A');
        vowels.add('E');
        vowels.add('I');
        vowels.add('O');
        vowels.add('U');
        return vowels;
    }
}
