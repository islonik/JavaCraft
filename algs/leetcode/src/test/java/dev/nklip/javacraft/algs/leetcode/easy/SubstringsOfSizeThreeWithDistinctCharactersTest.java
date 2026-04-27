package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SubstringsOfSizeThreeWithDistinctCharactersTest {

    @Test
    void testCase1() {
        SubstringsOfSizeThreeWithDistinctCharacters solution = new SubstringsOfSizeThreeWithDistinctCharacters();

        // Explanation: There are 4 substrings of size 3: "xyz", "yzz", "zza", and "zaz".
        // The only good substring of length 3 is "xyz".
        Assertions.assertEquals(1, solution.countGoodSubstrings("xyzzaz"));
    }

    @Test
    void testCase2() {
        SubstringsOfSizeThreeWithDistinctCharacters solution = new SubstringsOfSizeThreeWithDistinctCharacters();

        // Explanation: There are 7 substrings of size 3: "aab", "aba", "bab", "abc", "bca", "cab", and "abc".
        // The good substrings are "abc", "bca", "cab", and "abc".
        Assertions.assertEquals(4, solution.countGoodSubstrings("aababcabc"));
    }
}
