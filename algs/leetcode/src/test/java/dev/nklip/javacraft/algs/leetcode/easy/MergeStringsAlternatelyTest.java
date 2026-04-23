package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MergeStringsAlternatelyTest {

    @Test
    void testCase1() {
        MergeStringsAlternately solution = new MergeStringsAlternately();

        // Explanation: The merged string will be merged as so:
        // word1:  a   b   c
        // word2:    p   q   r
        // merged: a p b q c r
        Assertions.assertEquals("apbqcr", solution.mergeAlternately("abc", "pqr"));
    }

    @Test
    void testCase2() {
        MergeStringsAlternately solution = new MergeStringsAlternately();

        // Explanation: Notice that as word2 is longer, "rs" is appended to the end.
        // word1:  a   b
        // word2:    p   q   r   s
        // merged: a p b q   r   s
        Assertions.assertEquals("apbqrs", solution.mergeAlternately("ab", "pqrs"));
    }

    @Test
    void testCase3() {
        MergeStringsAlternately solution = new MergeStringsAlternately();

        // Explanation: Notice that as word1 is longer, "cd" is appended to the end.
        // word1:  a   b   c   d
        // word2:    p   q
        // merged: a p b q c   d
        Assertions.assertEquals("apbqcd", solution.mergeAlternately("abcd", "pq"));
    }
}
