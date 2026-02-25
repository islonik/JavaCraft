package my.javacraft.algs.leetcode.medium;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IntegerToRomanTest {

    @Test
    void testCase1() {
        IntegerToRoman solution = new IntegerToRoman();
        Assertions.assertEquals("MMMDCCXLIX", solution.intToRoman(3749));
    }

    @Test
    void testCase2() {
        IntegerToRoman solution = new IntegerToRoman();
        Assertions.assertEquals("LVIII", solution.intToRoman(58));
    }

    @Test
    void testCase3() {
        IntegerToRoman solution = new IntegerToRoman();
        Assertions.assertEquals("MCMXCIV", solution.intToRoman(1994));
    }

}
