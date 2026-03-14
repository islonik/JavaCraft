package my.javacraft.mathparser.parser;

import java.util.Locale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * User: Lipatov Nikita
 */
public class ParserTest {

    @Test
    public void testParser_testCase01() {
        Parser parser = new Parser();
        Assertions.assertEquals("6.0", parser.calculate(" 1 + 2 + 3"));
    }

    @Test
    public void testParser_testCase02() {
        Parser parser = new Parser();
        Assertions.assertEquals("31.0", parser.calculate("40 - 6 - 3"));
    }

    @Test
    public void testParser_testCase03() {
        Parser parser = new Parser();
        Assertions.assertEquals("16.0", parser.calculate("2 * 2 * 2 * 2"));
    }

    @Test
    public void testParser_testCase04() {
        Parser parser = new Parser();
        Assertions.assertEquals("8.0", parser.calculate("64.0 / 2 / 4"));
    }

    @Test
    public void testParser_testCase05() {
        Parser parser = new Parser();
        Assertions.assertEquals("2.0", parser.calculate("12 % 5"));
    }

    @Test
    public void testParser_testCase06() {
        Parser parser = new Parser();
        Assertions.assertEquals("1024.0", parser.calculate("2^10"));
    }

    @Test
    public void testParser_testCase07() {
        Parser parser = new Parser();
        Assertions.assertEquals("28.0", parser.calculate("(2+5) * 4"));
    }

    @Test
    public void testParser_testCase08() {
        Parser parser = new Parser();
        Assertions.assertEquals("12.0", parser.calculate("(-2+5) * 4"));
    }

    @Test
    public void testParser_testCase09() {
        Parser parser = new Parser();
        Assertions.assertEquals(Double.toString(Math.E * 4), parser.calculate("e * 4"));
    }

    @Test
    public void testParser_testCase10() {
        Parser parser = new Parser();
        Assertions.assertEquals(Double.toString(Math.PI * 4), parser.calculate("pi * 4"));
    }

    @Test
    public void testParser_testCase11() {
        Parser parser = new Parser();
        Assertions.assertEquals("1.0", parser.calculate("sin(90)"));
    }

    @Test
    public void testParser_testCase12() {
        Parser parser = new Parser();
        Assertions.assertEquals("1.0", parser.calculate("cos(0)"));
    }

    @Test
    public void testParser_testCase13() {
        Parser parser = new Parser();
        Assertions.assertEquals("5.0", parser.calculate("max(2,3,5)"));
    }

    @Test
    public void testParser_testCase14() {
        Parser parser = new Parser();
        Assertions.assertEquals("2.0", parser.calculate("min(2,3,5)"));
    }

    @Test
    public void testParser_testCase15() {
        Parser parser = new Parser();
        Assertions.assertEquals("6.0", parser.calculate("avg(3,6,9)"));
    }

    @Test
    public void testParser_testCase16() {
        Parser parser = new Parser();
        Assertions.assertEquals("1024.0", parser.calculate("pow(2,10)"));
    }

    @Test
    public void testParser_testCase17() {
        Parser parser = new Parser();
        Assertions.assertEquals("3.0", parser.calculate("round(log(10,1000))"));
    }

    @Test
    public void testParser_testCase18() {
        Parser parser = new Parser();
        Assertions.assertEquals("3.0", parser.calculate("round(log10(1000))"));
    }

    @Test
    public void testParser_testCase18a() {
        Parser parser = new Parser();
        Assertions.assertEquals("3.0", parser.calculate("round(ln(e^3))"));
    }

    @Test
    public void testParser_testCase18b() {
        Parser parser = new Parser();
        Assertions.assertEquals("0.0", parser.calculate("ln(1)"));
    }

    @Test
    public void testParser_testCase19() {
        Parser parser = new Parser();
        Assertions.assertEquals("100.0", parser.calculate("abs(-100)"));
    }

    @Test
    public void testParser_testCase20() {
        Parser parser = new Parser();
        Assertions.assertEquals("12.0", parser.calculate("sqrt(144)"));
    }

    @Test
    public void testParser_testCase21() {
        Parser parser = new Parser();
        Assertions.assertEquals("2.0", parser.calculate("ceil(1.2)"));
    }

    @Test
    public void testParser_testCase22() {
        Parser parser = new Parser();
        Assertions.assertEquals("1.0", parser.calculate("floor(1.8)"));
    }

    @Test
    public void testParser_testCase23() {
        Parser parser = new Parser();
        Assertions.assertEquals(Math.exp(1), Double.parseDouble(parser.calculate("exp(1)")), 1.0e-12);
    }

    @Test
    public void testParser_testCase24() {
        Parser parser = new Parser();
        Assertions.assertEquals("3.0", parser.calculate("cbrt(27)"));
    }

    @Test
    public void testParser_testCase25() {
        Parser parser = new Parser();
        Assertions.assertEquals("120.0", parser.calculate("factorial(5)"));
    }

    @Test
    public void testParser_testCase26() {
        Parser parser = new Parser();
        Assertions.assertEquals("180.0", parser.calculate("2*acos(0)"));
    }

    @Test
    public void testParser_testCase27() {
        Parser parser = new Parser();
        Assertions.assertEquals("0.0", parser.calculate("2*asin(0)"));
    }

    @Test
    public void testParser_testCase28() {
        Parser parser = new Parser();
        Assertions.assertEquals("0.0", parser.calculate("atan(0)"));
    }

    @Test
    public void testParser_testCase29() {
        Parser parser = new Parser();
        Assertions.assertEquals("1.0", parser.calculate("round(tan(45))"));
    }

    @Test
    public void testParser_testCase30() {
        Parser parser = new Parser();
        parser.calculate("var = 45");
        Assertions.assertEquals("180.0", parser.calculate("var * 4"));
    }

    @Test
    public void testParser_testCase31() {
        Parser parser = new Parser();
        parser.calculate("var = 45");
        parser.calculate("var = 10");
        Assertions.assertEquals("40.0", parser.calculate("var * 4"));
    }

    @Test
    public void testParser_testCase32() {
        Parser parser = new Parser();
        parser.setTangentUnit(ParserType.GRADIAN);

        Assertions.assertEquals("0.0", parser.calculate("round(sin(200))"));
    }

    @Test
    public void testParser_testCase33() {
        Parser parser = new Parser();
        parser.setTangentUnit(ParserType.GRADIAN);

        Assertions.assertEquals("1.0", parser.calculate("sin(100)"));
    }

    @Test
    public void testParser_testCase34() {
        Parser parser = new Parser();
        parser.setTangentUnit(ParserType.RADIAN);

        Assertions.assertEquals("-1.0", parser.calculate("round(sin(30))"));
    }

    @Test
    public void testParser_testCase35() {
        Parser parser = new Parser();
        parser.setTangentUnit(ParserType.RADIAN);

        Assertions.assertEquals("180.0", parser.calculate("sum(30, 60, 90)"));
    }

    @Test
    public void testParser_testCase36() {
        Parser parser = new Parser();
        Assertions.assertEquals("14.0", parser.calculate("2(3+4)"));
    }

    @Test
    public void testParser_testCase37() {
        Parser parser = new Parser();
        Assertions.assertEquals(Double.toString(2 * Math.PI), parser.calculate("2pi"));
    }

    @Test
    public void testParser_testCase38() {
        Parser parser = new Parser();
        Assertions.assertEquals("45.0", parser.calculate("(2+3)(4+5)"));
    }

    @Test
    public void testParser_testCase39() {
        Parser parser = new Parser();
        Assertions.assertEquals("3.0", parser.calculate("3sin(90)"));
    }

    @Test
    public void testParser_whitespaceNormalization_testCase01() {
        Parser parser = new Parser();
        Assertions.assertEquals("3.0", parser.calculate("\t1 +\n2\r"));
    }

    @Test
    public void testParser_whitespaceNormalization_testCase02() {
        Parser parser = new Parser();
        Assertions.assertEquals("3.0", parser.calculate("\u00A01\u00A0+\u00A02\u00A0"));
    }

    @Test
    public void testParserType_values() {
        Assertions.assertArrayEquals(
                new ParserType[]{ParserType.DEGREE, ParserType.GRADIAN, ParserType.RADIAN},
                ParserType.values()
        );
    }

    @Test
    public void testParser_inverseTrig_testCase01() {
        Parser parser = new Parser(ParserType.DEGREE);
        double asinResult = Double.parseDouble(parser.calculate("asin(1)"));
        double acosResult = Double.parseDouble(parser.calculate("acos(0)"));
        double atanResult = Double.parseDouble(parser.calculate("atan(1)"));

        Assertions.assertEquals(90.0, asinResult, 1.0e-9);
        Assertions.assertEquals(90.0, acosResult, 1.0e-9);
        Assertions.assertEquals(45.0, atanResult, 1.0e-9);
    }

    @Test
    public void testParser_inverseTrig_testCase02() {
        Parser parser = new Parser(ParserType.RADIAN);
        double asinResult = Double.parseDouble(parser.calculate("asin(1)"));
        double acosResult = Double.parseDouble(parser.calculate("acos(0)"));
        double atanResult = Double.parseDouble(parser.calculate("atan(1)"));

        Assertions.assertEquals(Math.PI / 2, asinResult, 1.0e-9);
        Assertions.assertEquals(Math.PI / 2, acosResult, 1.0e-9);
        Assertions.assertEquals(Math.PI / 4, atanResult, 1.0e-9);
    }

    @Test
    public void testParser_localeSafeLowerCaseWithTurkishLocale() {
        Locale originalLocale = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));
        try {
            Parser parser = new Parser();
            Assertions.assertEquals("1.0", parser.calculate("SIN(90)"));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

}
