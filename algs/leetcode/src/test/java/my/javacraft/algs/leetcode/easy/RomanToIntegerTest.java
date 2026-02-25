package my.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RomanToIntegerTest {

    @Test
    void test1() {
        RomanToInteger solution = new RomanToInteger();
        Assertions.assertEquals(3, solution.romanToInt("III"));
    }

    @Test
    void test2() {
        RomanToInteger solution = new RomanToInteger();
        Assertions.assertEquals(58, solution.romanToInt("LVIII"));
    }

    @Test
    void test3() {
        RomanToInteger solution = new RomanToInteger();
        Assertions.assertEquals(1994, solution.romanToInt("MCMXCIV"));
    }

}
