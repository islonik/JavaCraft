package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IsSubsequenceTest {

    @Test
    void testCase1() {
        IsSubsequence solution = new IsSubsequence();

        Assertions.assertTrue(solution.isSubsequence("abc", "ahbgdc"));
        Assertions.assertTrue(solution.isSubsequence("aaa", "aaa"));
        Assertions.assertTrue(solution.isSubsequence("aaa", "bbbbaaa"));
        Assertions.assertTrue(solution.isSubsequence("", "ahbgdc"));
    }

    @Test
    void testCase2() {
        IsSubsequence solution = new IsSubsequence();

        Assertions.assertFalse(solution.isSubsequence("axc", "ahbgdc"));
        Assertions.assertFalse(solution.isSubsequence("a", ""));
    }

}
