package my.javacraft.mathparser.parser;

import org.junit.Assert;
import org.junit.Test;

public class RomanNumbersTest {

    @Test
    public void testToRoman() {
        Assert.assertEquals("I", RomanNumbers.toRoman(1));
        Assert.assertEquals("V", RomanNumbers.toRoman(5));
        Assert.assertEquals("X", RomanNumbers.toRoman(10));
        Assert.assertEquals("L", RomanNumbers.toRoman(50));
        Assert.assertEquals("C", RomanNumbers.toRoman(100));
        Assert.assertEquals("D", RomanNumbers.toRoman(500));
        Assert.assertEquals("M", RomanNumbers.toRoman(1000));
        Assert.assertEquals("XXV", RomanNumbers.toRoman(25));
        Assert.assertEquals("DXLII", RomanNumbers.toRoman(542));
        Assert.assertEquals("MXXIII", RomanNumbers.toRoman(1023));
        Assert.assertEquals("MMMCMXCIX", RomanNumbers.toRoman(3999));
    }

}
