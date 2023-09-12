package my.javacraft.mathparser.parser;

import org.junit.Assert;
import org.junit.Test;

public class RomanNumbersTest {

    @Test
    public void testToRoman() {
        Assert.assertEquals("", RomanNumbers.toRoman(0));
        Assert.assertEquals("I", RomanNumbers.toRoman(1));
        Assert.assertEquals("V", RomanNumbers.toRoman(5));
        Assert.assertEquals("X", RomanNumbers.toRoman(10));
        Assert.assertEquals("XLIX", RomanNumbers.toRoman(49));
        Assert.assertEquals("L", RomanNumbers.toRoman(50));
        Assert.assertEquals("C", RomanNumbers.toRoman(100));
        Assert.assertEquals("D", RomanNumbers.toRoman(500));
        Assert.assertEquals("CMXCIX", RomanNumbers.toRoman(999));
        Assert.assertEquals("M", RomanNumbers.toRoman(1000));
        Assert.assertEquals("XXV", RomanNumbers.toRoman(25));
        Assert.assertEquals("DXLII", RomanNumbers.toRoman(542));
        Assert.assertEquals("MXXIII", RomanNumbers.toRoman(1023));
        Assert.assertEquals("MMMCMXCIX", RomanNumbers.toRoman(3999));
        Assert.assertEquals("MMMMCMXCIX", RomanNumbers.toRoman(4999));
    }

    @Test
    public void testFroRoman() {
        Assert.assertEquals(0, RomanNumbers.fromRoman(""));
        Assert.assertEquals(1, RomanNumbers.fromRoman("I"));
        Assert.assertEquals(5, RomanNumbers.fromRoman("V"));
        Assert.assertEquals(10, RomanNumbers.fromRoman("X"));
        Assert.assertEquals(49, RomanNumbers.fromRoman("XLIX"));
        Assert.assertEquals(50, RomanNumbers.fromRoman("L"));
        Assert.assertEquals(100, RomanNumbers.fromRoman("C"));
        Assert.assertEquals(500, RomanNumbers.fromRoman("D"));
        Assert.assertEquals(999, RomanNumbers.fromRoman("CMXCIX"));
        Assert.assertEquals(1000, RomanNumbers.fromRoman("M"));
        Assert.assertEquals(25, RomanNumbers.fromRoman("XXV"));
        Assert.assertEquals(542, RomanNumbers.fromRoman("DXLII"));
        Assert.assertEquals(1023, RomanNumbers.fromRoman("MXXIII"));
        Assert.assertEquals(3999, RomanNumbers.fromRoman("MMMCMXCIX"));
        Assert.assertEquals(4999, RomanNumbers.fromRoman("MMMMCMXCIX"));
    }
}
