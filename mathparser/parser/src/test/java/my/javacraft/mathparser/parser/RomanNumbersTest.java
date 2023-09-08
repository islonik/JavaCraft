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

}
