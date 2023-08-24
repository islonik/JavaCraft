package my.javacraft.mathparser.parser;

import my.javacraft.mathparser.parser.Parser;
import my.javacraft.mathparser.parser.ParserException;
import my.javacraft.mathparser.parser.ParserType;
import org.junit.Assert;
import org.junit.Test;

/**
 * User: Lipatov Nikita
 */
public class ParserTest {

    @Test
    public void testParser_testCase01() {
        Parser parser = new Parser();
        Assert.assertEquals("6.0", parser.calculate(" 1 + 2 + 3"));
    }

    @Test
    public void testParser_testCase02() {
        Parser parser = new Parser();
        Assert.assertEquals("31.0", parser.calculate("40 - 6 - 3"));
    }

    @Test
    public void testParser_testCase03() {
        Parser parser = new Parser();
        Assert.assertEquals("16.0", parser.calculate("2 * 2 * 2 * 2"));
    }

    @Test
    public void testParser_testCase04() {
        Parser parser = new Parser();
        Assert.assertEquals("8.0", parser.calculate("64.0 / 2 / 4"));
    }

    @Test
    public void testParser_testCase05() {
        Parser parser = new Parser();
        Assert.assertEquals("2.0", parser.calculate("12 % 5"));
    }

    @Test
    public void testParser_testCase06() {
        Parser parser = new Parser();
        Assert.assertEquals("1024.0", parser.calculate("2^10"));
    }

    @Test
    public void testParser_testCase07() {
        Parser parser = new Parser();
        Assert.assertEquals("28.0", parser.calculate("(2+5) * 4"));
    }

    @Test
    public void testParser_testCase08() {
        Parser parser = new Parser();
        Assert.assertEquals("12.0", parser.calculate("(-2+5) * 4"));
    }

    @Test
    public void testParser_testCase09() {
        Parser parser = new Parser();
        Assert.assertEquals(Double.toString(Math.E * 4), parser.calculate("e * 4"));
    }

    @Test
    public void testParser_testCase10() {
        Parser parser = new Parser();
        Assert.assertEquals(Double.toString(Math.PI * 4), parser.calculate("pi * 4"));
    }

    @Test
    public void testParser_testCase11() {
        Parser parser = new Parser();
        Assert.assertEquals("1.0", parser.calculate("sin(90)"));
    }

    @Test
    public void testParser_testCase12() {
        Parser parser = new Parser();
        Assert.assertEquals("1.0", parser.calculate("cos(0)"));
    }

    @Test
    public void testParser_testCase13() {
        Parser parser = new Parser();
        Assert.assertEquals("5.0", parser.calculate("max(2,3,5)"));
    }

    @Test
    public void testParser_testCase14() {
        Parser parser = new Parser();
        Assert.assertEquals("2.0", parser.calculate("min(2,3,5)"));
    }

    @Test
    public void testParser_testCase15() {
        Parser parser = new Parser();
        Assert.assertEquals("6.0", parser.calculate("avg(3,6,9)"));
    }

    @Test
    public void testParser_testCase16() {
        Parser parser = new Parser();
        Assert.assertEquals("1024.0", parser.calculate("pow(2,10)"));
    }

    @Test
    public void testParser_testCase17() {
        Parser parser = new Parser();
        Assert.assertEquals("3.0", parser.calculate("round(log(10,1000))"));
    }

    @Test
    public void testParser_testCase18() {
        Parser parser = new Parser();
        Assert.assertEquals("3.0", parser.calculate("round(log10(1000))"));
    }

    @Test
    public void testParser_testCase19() {
        Parser parser = new Parser();
        Assert.assertEquals("100.0", parser.calculate("abs(-100)"));
    }

    @Test
    public void testParser_testCase20() {
        Parser parser = new Parser();
        Assert.assertEquals("12.0", parser.calculate("sqrt(144)"));
    }

    @Test
    public void testParser_testCase21() {
        Parser parser = new Parser();
        Assert.assertEquals(Double.toString(Math.PI), parser.calculate("2*acos(0)"));
    }

    @Test
    public void testParser_testCase22() {
        Parser parser = new Parser();
        Assert.assertEquals("0.0", parser.calculate("2*asin(0)"));
    }

    @Test
    public void testParser_testCase23() {
        Parser parser = new Parser();
        Assert.assertEquals("0.0", parser.calculate("atan(0)"));
    }

    @Test
    public void testParser_testCase24() {
        Parser parser = new Parser();
        Assert.assertEquals("1.0", parser.calculate("round(tan(45))"));
    }

    @Test
    public void testParser_testCase25() {
        Parser parser = new Parser();
        parser.calculate("var = 45");
        Assert.assertEquals("180.0", parser.calculate("var * 4"));
    }

    @Test
    public void testParser_testCase26() {
        Parser parser = new Parser();
        parser.calculate("var = 45");
        parser.calculate("var = 10");
        Assert.assertEquals("40.0", parser.calculate("var * 4"));
    }

    @Test
    public void testParser_testCase27() {
        Parser parser = new Parser();
        parser.setTangentUnit(ParserType.GRADUS);

        Assert.assertEquals("0.0", parser.calculate("round(sin(200))"));
    }

    @Test
    public void testParser_testCase28() {
        Parser parser = new Parser();
        parser.setTangentUnit(ParserType.GRADUS);

        Assert.assertEquals("1.0", parser.calculate("sin(100)"));
    }

    @Test
    public void testParser_testCase29() {
        Parser parser = new Parser();
        parser.setTangentUnit(ParserType.RADIAN);

        Assert.assertEquals("-1.0", parser.calculate("round(sin(30))"));
    }

    @Test
    public void testParser_testCase30() {
        Parser parser = new Parser();
        parser.setTangentUnit(ParserType.RADIAN);

        Assert.assertEquals("180.0", parser.calculate("sum(30, 60, 90)"));
    }

    @Test
    public void testParser_errors_testCase01() {
        Parser parser = new Parser();
        Assert.assertEquals(new ParserException(ParserException.Error.DIVISION_BY_ZERO).toString(), parser.calculate("10 / 0"));
    }

    @Test
    public void testParser_errors_testCase02() {
        Parser parser = new Parser();
        Assert.assertEquals(new ParserException(ParserException.Error.DIVISION_BY_ZERO).toString(), parser.calculate("10 % 0"));
    }

    @Test
    public void testParser_errors_testCase03() {
        Parser parser = new Parser();
        Assert.assertEquals(new ParserException(ParserException.Error.NO_EXPRESSION).toString(), parser.calculate(""));
    }

    @Test
    public void testParser_errors_testCase04() {
        Parser parser = new Parser();
        Assert.assertEquals(new ParserException(ParserException.Error.UNBAL_PARENTS).toString(), parser.calculate("((2+5) * 3"));
    }

    @Test
    public void testParser_errors_testCase05() {
        Parser parser = new Parser();
        Assert.assertEquals(new ParserException(ParserException.Error.SYNTAX).toString(), parser.calculate("((2+5) * 3))"));
    }

    @Test
    public void testParser_errors_testCase06() {
        Parser parser = new Parser();
        Assert.assertEquals(new ParserException(ParserException.Error.SYNTAX).toString(), parser.calculate("rtg(2 * 5)"));
    }


}
