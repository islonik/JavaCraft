package dev.nklip.javacraft.algs.leetcode.medium;

import java.util.HashMap;
import java.util.Map;

/*
 * 3. Longest Substring Without Repeating Characters
 *
 * LeetCode: https://leetcode.com/problems/longest-substring-without-repeating-characters
 *
 * Given a string s, find the length of the longest substring without duplicate characters.
 */
public class LongestSubstringWithoutRepeatingCharacters {

    // using sliding window with last seen chars - O(n)
    public int lengthOfLongestSubstring(String inputString) {
        Map<Character, Integer> lastSeen = new HashMap<>();
        int leftIndex = 0;
        int maxLength = 0;

        for (int i = 0; i < inputString.length(); i++) {
            char key = inputString.charAt(i);

            // if a char already was seen in the sliding window...
            if (lastSeen.containsKey(key) && lastSeen.get(key) >= leftIndex) {
                // shrink sliding window size
                leftIndex = lastSeen.get(key) + 1;
            }
            // increase sliding window size
            lastSeen.put(inputString.charAt(i), i);

            maxLength = Math.max(maxLength, i - leftIndex + 1);
        }
        return maxLength;
    }

    // using brute force with char array - O(n^2)
    public int lengthOfLongestSubstringBruteForce(String inputString) {
        int longest = 0;

        int current = 0;
        char[] charArray = new char[inputString.length()];
        for (int i = 0; i < inputString.length(); i++) {
            char ch = inputString.charAt(i);

            if (contains(charArray, current, ch)) {
                int index = indexOf(charArray, current, ch) + 1;
                delete(charArray, index, current);

                current -= index;
                charArray[current++] = ch;
            } else {
                charArray[current++] = ch;

                longest = Math.max(current, longest);
            }
        }
        return longest;
    }

    private boolean contains(char[] charArray, int current, char ch) {
        for (int i = 0; i < current; i++) {
            if (charArray[i] == ch) {
                return true;
            }
        }
        return false;
    }

    private int indexOf(char[] charArray, int current, char ch) {
        for (int i = 0; i < current; i++) {
            if (charArray[i] == ch) {
                return i;
            }
        }
        return -1;
    }

    // start - 1; end - 4
    private void delete(char[] charArray, int start, int current) {
        for (int i = 0; i < current; i++) {
            if (start + i >= charArray.length) {
                break;
            }
            charArray[i] = charArray[start + i];
        }
    }

    // using brute force with string builder - O(n^2)
    public int lengthOfLongestSubstringBruteForce2(String inputString) {
        int longest = 0;
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < inputString.length(); i++) {
            char ch = inputString.charAt(i);
            String chStr = Character.toString(ch);

            if (temp.toString().contains(chStr)) {
                int start = temp.indexOf(chStr);
                temp.delete(0, start + 1);
                temp.append(chStr);
            } else {
                temp.append(chStr);
                longest = Math.max(temp.length(), longest);
            }
        }
        return longest;
    }

}
