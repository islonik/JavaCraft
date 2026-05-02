package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GuessNumberHigherOrLowerTest {

    @Test
    void testCase1() {
        GuessNumberHigherOrLower solution = new GuessNumberHigherOrLower(6);

        Assertions.assertEquals(6, solution.guessNumber(10));
    }

    @Test
    void testCase2() {
        GuessNumberHigherOrLower solution = new GuessNumberHigherOrLower(1);

        Assertions.assertEquals(1, solution.guessNumber(1));
    }

    @Test
    void testCase3() {
        GuessNumberHigherOrLower solution = new GuessNumberHigherOrLower(1);

        Assertions.assertEquals(1, solution.guessNumber(2));
    }
}
