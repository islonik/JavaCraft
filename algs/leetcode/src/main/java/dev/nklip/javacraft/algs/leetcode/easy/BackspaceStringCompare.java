package dev.nklip.javacraft.algs.leetcode.easy;

/*
 * 844. Backspace String Compare
 *
 * LeetCode: https://leetcode.com/problems/backspace-string-compare/
 *
 * Given two strings s and t, return true if they are equal when both are typed into empty text editors.
 * '#' means a backspace character.
 *
 * Note that after backspacing an empty text, the text will continue empty.
 */
public class BackspaceStringCompare {

    public boolean backspaceCompare(String s, String t) {
        String s1 = replaceBackspace(s);
        String t1 = replaceBackspace(t);

        return s1.equals(t1);
    }

    String replaceBackspace(String s) {
        StringBuilder sb = new StringBuilder();
        int right = s.length() - 1;
        int countBack = 0;
        while (right >= 0) {
            char rightCh = s.charAt(right);
            if (rightCh == '#') {
                countBack++;
            } else if (countBack > 0) {
                countBack--;
            } else {
                sb.append(s.charAt(right));
            }
            right--;
        }
        return sb.toString();
    }

}
