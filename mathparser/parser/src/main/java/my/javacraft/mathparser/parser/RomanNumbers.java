package my.javacraft.mathparser.parser;

/**
 * The Roman numeral system uses only seven symbols: I, V, X, L, C, D, and M.
 *
 * I is 1
 * V is 5
 * X is 10
 * L is 50
 * C is 100
 * D is 500
 * M is 1000
 */
public class RomanNumbers {

    private final static String[] THOUSANDS = {"", "M", "MM", "MMM", "MMMM"};
    private final static String[] HUNDREDS = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
    private final static String[] TENS = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
    private final static String[] UNITS = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

    static String toRoman(int number) {
        StringBuilder roman = new StringBuilder();
        roman.append(THOUSANDS[number / 1000]);
        roman.append(HUNDREDS[(number % 1000) / 100]);
        roman.append(TENS[(number % 100) / 10]);
        roman.append(UNITS[number % 10]);
        return roman.toString();
    }

    static int fromRoman(String romanNumber) {
        int number = 0;

        for (int i = THOUSANDS.length - 1; i >= 0; i--) {
            if (romanNumber.startsWith(THOUSANDS[i])) {
                number += i * 1000;
                romanNumber = romanNumber.substring(THOUSANDS[i].length());
                break;
            }
        }

        for (int i = HUNDREDS.length - 1; i >= 0; i--) {
            if (romanNumber.startsWith(HUNDREDS[i])) {
                number += i * 100;
                romanNumber = romanNumber.substring(HUNDREDS[i].length());
                break;
            }
        }

        for (int i = TENS.length - 1; i >= 0; i--) {
            if (romanNumber.startsWith(TENS[i])) {
                number += i * 10;
                romanNumber = romanNumber.substring(TENS[i].length());
                break;
            }
        }

        for (int i = UNITS.length - 1; i >= 0; i--) {
            if (romanNumber.startsWith(UNITS[i])) {
                number += i;
                break;
            }
        }

        return number;
    }

}
