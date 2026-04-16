package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReverseVowelsOfStringTest {

    @Test
    void testCase1() {
        ReverseVowelsOfString solution = new ReverseVowelsOfString();

        Assertions.assertEquals("AceCreIm", solution.reverseVowels("IceCreAm"));
    }

    @Test
    void testCase2() {
        ReverseVowelsOfString solution = new ReverseVowelsOfString();

        Assertions.assertEquals("leotcede", solution.reverseVowels("leetcode"));
    }
}
