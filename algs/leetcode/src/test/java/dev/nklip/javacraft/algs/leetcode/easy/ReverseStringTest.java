package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReverseStringTest {
    @Test
    void testCase1() {
        ReverseString solution = new ReverseString();

        char[] chars = new char[]{'h', 'e', 'l', 'l', 'o'};

        solution.reverseString(chars);

        Assertions.assertArrayEquals(new char[]{'o', 'l', 'l', 'e', 'h'}, chars);
    }

    @Test
    void testCase2() {
        ReverseString solution = new ReverseString();

        char[] chars = new char[]{'H', 'a', 'n', 'n', 'a', 'h'};

        solution.reverseString(chars);

        Assertions.assertEquals("hannaH", String.valueOf(chars));
    }
}
