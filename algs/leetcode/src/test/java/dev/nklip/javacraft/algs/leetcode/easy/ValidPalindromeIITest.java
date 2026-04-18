package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ValidPalindromeIITest {

    @Test
    void testCase1() {
        ValidPalindromeII solution = new ValidPalindromeII();

        Assertions.assertTrue(solution.validPalindrome("cbbcc"));
        Assertions.assertTrue(solution.validPalindrome("eceec"));
        Assertions.assertTrue(solution.validPalindrome("aba"));
        Assertions.assertTrue(solution.validPalindrome("abca"));
    }

    @Test
    void testCase2() {
        ValidPalindromeII solution = new ValidPalindromeII();

        Assertions.assertFalse(solution.validPalindrome("abc"));
    }

    @Test
    void testExcludeIndexOf() {
        ValidPalindromeII solution = new ValidPalindromeII();
        Assertions.assertEquals("c", solution.excludeIndexOf("abca", 1, 2, 1));
        Assertions.assertEquals("b", solution.excludeIndexOf("abca", 1, 2, 2));
    }
}
