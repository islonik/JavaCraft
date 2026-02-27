package my.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FindFirstOccurrenceInStringTest {

    @Test
    void testCase1() {
        FindFirstOccurrenceInString solution = new FindFirstOccurrenceInString();
        Assertions.assertEquals(0, solution.strStr("sadbutsad", "sad"));
    }

    @Test
    void testCase2() {
        FindFirstOccurrenceInString solution = new FindFirstOccurrenceInString();
        Assertions.assertEquals(-1, solution.strStr("leetcode", "leeto"));
    }

    @Test
    void testCase3() {
        FindFirstOccurrenceInString solution = new FindFirstOccurrenceInString();
        Assertions.assertEquals(0, solution.strStr("a", "a"));
    }

    @Test
    void testCase4() {
        FindFirstOccurrenceInString solution = new FindFirstOccurrenceInString();
        Assertions.assertEquals(-1, solution.strStr("aaa", "aaaa"));
    }

    @Test
    void testCase5() {
        FindFirstOccurrenceInString solution = new FindFirstOccurrenceInString();
        Assertions.assertEquals(-1, solution.strStr("mississippi", "issipi"));
    }

    @Test
    void testCase6() {
        FindFirstOccurrenceInString solution = new FindFirstOccurrenceInString();
        Assertions.assertEquals(4, solution.strStr("mississippi", "issip"));
    }

}
