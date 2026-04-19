package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BackspaceStringCompareTest {

    @Test
    void testCase1() {
        BackspaceStringCompare solution = new BackspaceStringCompare();

        // Explanation: Both s and t become "ac".
        Assertions.assertTrue(solution.backspaceCompare("ab#c", "ad#c"));
    }

    @Test
    void testCase2() {
        BackspaceStringCompare solution = new BackspaceStringCompare();

        // Explanation: Both s and t become "".
        Assertions.assertTrue(solution.backspaceCompare("ab##", "c#d#"));
    }

    @Test
    void testCase3() {
        BackspaceStringCompare solution = new BackspaceStringCompare();

        // Explanation: s becomes "c" while t becomes "b".
        Assertions.assertFalse(solution.backspaceCompare("a#c", "b"));
    }

    @Test
    void testCase4() {
        BackspaceStringCompare solution = new BackspaceStringCompare();

        // Explanation: btw vs tw
        Assertions.assertFalse(solution.backspaceCompare("bxj##tw", "bxj###tw"));
    }
}
