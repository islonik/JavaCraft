package my.javacraft.mathparser.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RomanNumbersTest {

    @Test
    public void testToRoman() {
        Assertions.assertEquals("", RomanNumbers.toRoman(0));
        Assertions.assertEquals("I", RomanNumbers.toRoman(1));
        Assertions.assertEquals("V", RomanNumbers.toRoman(5));
        Assertions.assertEquals("X", RomanNumbers.toRoman(10));
        Assertions.assertEquals("XLIX", RomanNumbers.toRoman(49));
        Assertions.assertEquals("L", RomanNumbers.toRoman(50));
        Assertions.assertEquals("C", RomanNumbers.toRoman(100));
        Assertions.assertEquals("D", RomanNumbers.toRoman(500));
        Assertions.assertEquals("CMXCIX", RomanNumbers.toRoman(999));
        Assertions.assertEquals("M", RomanNumbers.toRoman(1000));
        Assertions.assertEquals("XXV", RomanNumbers.toRoman(25));
        Assertions.assertEquals("DXLII", RomanNumbers.toRoman(542));
        Assertions.assertEquals("MXXIII", RomanNumbers.toRoman(1023));
        Assertions.assertEquals("MMMCMXCIX", RomanNumbers.toRoman(3999));
        Assertions.assertEquals("MMMMCMXCIX", RomanNumbers.toRoman(4999));
    }

    @Test
    public void testFroRoman() {
        Assertions.assertEquals(0, RomanNumbers.fromRoman(""));
        Assertions.assertEquals(1, RomanNumbers.fromRoman("I"));
        Assertions.assertEquals(5, RomanNumbers.fromRoman("V"));
        Assertions.assertEquals(10, RomanNumbers.fromRoman("X"));
        Assertions.assertEquals(49, RomanNumbers.fromRoman("XLIX"));
        Assertions.assertEquals(50, RomanNumbers.fromRoman("L"));
        Assertions.assertEquals(100, RomanNumbers.fromRoman("C"));
        Assertions.assertEquals(500, RomanNumbers.fromRoman("D"));
        Assertions.assertEquals(999, RomanNumbers.fromRoman("CMXCIX"));
        Assertions.assertEquals(1000, RomanNumbers.fromRoman("M"));
        Assertions.assertEquals(25, RomanNumbers.fromRoman("XXV"));
        Assertions.assertEquals(542, RomanNumbers.fromRoman("DXLII"));
        Assertions.assertEquals(1023, RomanNumbers.fromRoman("MXXIII"));
        Assertions.assertEquals(3999, RomanNumbers.fromRoman("MMMCMXCIX"));
        Assertions.assertEquals(4999, RomanNumbers.fromRoman("MMMMCMXCIX"));
    }
}
